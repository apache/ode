/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.evt.*;
import org.apache.ode.bpel.evtproc.ActivityStateDocumentBuilder;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.pmapi.*;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

/**
 * Implentation of the Process and InstanceManagement APIs.
 * 
 * TODO Pull up IM/PM methods from BpelManagementFacadeImpl
 */
class ProcessAndInstanceManagementImpl
        implements InstanceManagement, ProcessManagement {

    protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);
    protected static Log __log = LogFactory.getLog(BpelManagementFacadeImpl.class);
    protected static final ProcessStatusConverter __psc = new ProcessStatusConverter();
    protected BpelEngineImpl _engine;
    protected BpelServerImpl _server;
    protected BpelDatabase _db;
    protected ProcessStore _store;
    protected Calendar _calendar = Calendar.getInstance(); // Calendar can be expensive to initialize so we cache and clone it

    public ProcessAndInstanceManagementImpl(BpelDatabase db, BpelEngineImpl engine,
                                            BpelServerImpl server, ProcessStore store) {
        _db = db;
        _engine = engine;
        _server = server;
        _store = store;
    }

    public ProcessInfoListDocument listProcessesCustom(String filter, String orderKeys, final ProcessInfoCustomizer custom) {
        ProcessInfoListDocument ret = ProcessInfoListDocument.Factory.newInstance();
        final TProcessInfoList procInfoList = ret.addNewProcessInfoList();
        final ProcessFilter processFilter = new ProcessFilter(filter, orderKeys);

        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessDAO> processes = conn.processQuery(processFilter);
                    for (ProcessDAO proc : processes) {
                        fillProcessInfo(procInfoList.addNewProcessInfo(), proc, custom);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ProcessingException("Exception while listing processes",e);
        }

        return ret;
    }

    public ProcessInfoListDocument listProcesses(String filter, String orderKeys) {
        return listProcessesCustom(filter, orderKeys, ProcessInfoCustomizer.ALL);
    }

    public ProcessInfoListDocument listAllProcesses() {
        return listProcessesCustom(null, null, ProcessInfoCustomizer.ALL);
    }

    public ProcessInfoDocument getProcessInfoCustom(QName pid, ProcessInfoCustomizer custom) {
        return genProcessInfoDocument(pid, custom);
    }


    public ProcessInfoDocument getProcessInfo(QName pid) {
        return getProcessInfoCustom(pid, ProcessInfoCustomizer.ALL);
    }

    public ProcessInfoDocument activate(QName pid) {
        // TODO: Figure out how to deal with activation/retirement.
        return genProcessInfoDocument(pid, ProcessInfoCustomizer.NONE);
    }


    public ProcessInfoDocument setRetired(final QName pid, final boolean retired)
            throws ManagementException {
        try {
            _store.markActive(pid, !retired);
        } catch (BpelEngineException e) {
            throw new ProcessNotFoundException("ProcessNotFound:" + pid);
        }
        return genProcessInfoDocument(pid, ProcessInfoCustomizer.NONE);
    }

    public ProcessInfoDocument setProcessPropertyNode(final QName pid, final QName propertyName, final Node value)
            throws ManagementException {
        ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
        final TProcessInfo pi = ret.addNewProcessInfo();
        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws Exception {
                    ProcessDAO proc = conn.getProcess(pid);
                    if (proc == null)
                        throw new ProcessNotFoundException("ProcessNotFound:" + pid);
                    _store.setProperty(pid, propertyName.getLocalPart(), propertyName.getNamespaceURI(), value);
                    fillProcessInfo(pi, proc, new ProcessInfoCustomizer(ProcessInfoCustomizer.Item.PROPERTIES));
                    return null;
                }
            });
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            throw new ProcessingException("Exception while setting process property",e);
        }

        return ret;
    }

    public ProcessInfoDocument setProcessProperty(final QName pid, final QName propertyName, final String value)
            throws ManagementException {
        ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
        final TProcessInfo pi = ret.addNewProcessInfo();
        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws Exception {
                    ProcessDAO proc = conn.getProcess(pid);
                    if (proc == null)
                        throw new ProcessNotFoundException("ProcessNotFound:" + pid);
                    _store.setProperty(pid, propertyName.getLocalPart(), propertyName.getNamespaceURI(), value);
                    fillProcessInfo(pi, proc, new ProcessInfoCustomizer(ProcessInfoCustomizer.Item.PROPERTIES));
                    return null;
                }
            });
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            throw new ProcessingException("Exception while setting process property",e);
        }

        return ret;
    }

    public InstanceInfoListDocument listInstances(String filter, String order, int limit) {
        InstanceInfoListDocument ret = InstanceInfoListDocument.Factory.newInstance();
        final TInstanceInfoList infolist = ret.addNewInstanceInfoList();
        final InstanceFilter instanceFilter = new InstanceFilter(filter, order, limit);
        try {

            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
                    for (ProcessInstanceDAO instance : instances) {
                        fillInstanceInfo(infolist.addNewInstanceInfo(), instance);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ProcessingException("Exception while listing instances",e);
        }

        return ret;
    }

    public InstanceInfoListDocument listAllInstances() {
        return listInstances(null, null, Integer.MAX_VALUE);
    }

    public InstanceInfoListDocument listAllInstancesWithLimit(int limit) {
        return listInstances(null, null, limit);
    }

    public InstanceInfoDocument getInstanceInfo(Long iid) throws InstanceNotFoundException {
        return genInstanceInfoDocument(iid);
    }



    public ScopeInfoDocument getScopeInfo(String siid) {
        return getScopeInfoWithActivity(siid, false);
    }

    public ScopeInfoDocument getScopeInfoWithActivity(String siid, boolean includeActivityInfo) {
        return genScopeInfoDocument(siid, includeActivityInfo);
    }

    public VariableInfoDocument getVariableInfo(final String scopeId, final String varName)
            throws ManagementException {
        VariableInfoDocument ret = VariableInfoDocument.Factory.newInstance();
        final TVariableInfo vinf = ret.addNewVariableInfo();
        final TVariableRef sref = vinf.addNewSelf();
        dbexec(new BpelDatabase.Callable<Object>()  {
            public Object run(BpelDAOConnection session) throws Exception {
                ScopeDAO scope = session.getScope(new Long(scopeId));
                if (scope == null) {
                    throw new InvalidRequestException("ScopeNotFound:" + scopeId);
                }

                sref.setSiid(scopeId);
                sref.setIid(scope.getProcessInstance().getInstanceId().toString());
                sref.setName(varName );

                XmlDataDAO var = scope.getVariable(varName);
                if (var == null) {
                    throw new InvalidRequestException("VarNotFound:" + varName);
                }

                Node nval = var.get();
                if (nval != null) {
                    TVariableInfo.Value val = vinf.addNewValue();
                    val.getDomNode().appendChild(
                            val.getDomNode().getOwnerDocument().importNode(nval,true));
                }
                return null;
            }
        });
        return ret;
    }

    //
    // INSTANCE ACTIONS
    //
    public InstanceInfoDocument fault(Long iid, QName faultname, Element faultData) {
        // TODO: Implement
        return genInstanceInfoDocument(iid);
    }


    public InstanceInfoDocument resume(final Long iid) {
        // We need debugger support in order to resume (since we have to force
        // a reduction. If one is not available the getDebugger() method should
        // throw a ProcessingException
        getDebugger(iid).resume(iid);

        return genInstanceInfoDocument(iid);
    }

    public InstanceInfoDocument suspend(final Long iid)
            throws ManagementException {
        DebuggerSupport debugSupport = getDebugger(iid);
        assert debugSupport != null : "getDebugger(Long) returned NULL!";
        debugSupport.suspend(iid);

        return genInstanceInfoDocument(iid);
    }

    public InstanceInfoDocument terminate(final Long iid) throws ManagementException {
        DebuggerSupport debugSupport = getDebugger(iid);
        assert debugSupport != null : "getDebugger(Long) returned NULL!";
        debugSupport.terminate(iid);

        return genInstanceInfoDocument(iid);
    }

    public InstanceInfoDocument recoverActivity(final Long iid, final Long aid, final String action) {
        try {
            _db.exec(new BpelDatabase.Callable<QName>() {
                public QName run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    if (instance == null)
                        return null;
                    for (ActivityRecoveryDAO recovery: instance.getActivityRecoveries()) {
                        if (recovery.getActivityId() == aid) {
                            BpelProcess process = _engine._activeProcesses.get(instance.getProcess().getProcessId());
                            if (process != null) {
                                process.recoverActivity(instance, recovery.getChannel(), aid, action, null);
                                break;
                            }
                        }
                    }
                    return instance.getProcess().getProcessId();
                }
            });
        } catch (Exception e) {
            throw new ProcessingException("Exception during activity recovery", e);
        }
        return genInstanceInfoDocument(iid);
    }

    public Collection<Long> delete(String filter) {
        final InstanceFilter instanceFilter = new InstanceFilter(filter);

        List<Long> ret = new LinkedList<Long>();
        try {

            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
                    for (ProcessInstanceDAO instance :instances) {
                        instance.delete();
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ProcessingException("Exception during instance deletion",e);
        }

        return ret;
    }

    //
    // EVENT RETRIEVAL
    //
    public List<String> getEventTimeline(String instanceFilter, String eventFilter) {
        final InstanceFilter ifilter = new InstanceFilter(instanceFilter,null,0);
        final BpelEventFilter efilter = new BpelEventFilter(eventFilter,0);

        List<Date> tline = dbexec(new BpelDatabase.Callable<List<Date>>()  {
            public List<Date> run(BpelDAOConnection session) throws Exception {
                return session.bpelEventTimelineQuery(ifilter, efilter);
            }
        });

        ArrayList<String> ret = new ArrayList<String>(tline.size());
        CollectionsX.transform(ret,tline,new UnaryFunction<Date,String>() {
            public String apply(Date x) {
                return ISO8601DateParser.format(x);
            }
        });
        return ret;
    }

    public EventInfoListDocument listEvents(String instanceFilter, String eventFilter, int maxCount) {
        final InstanceFilter ifilter = new InstanceFilter(instanceFilter,null,0);
        final BpelEventFilter efilter = new BpelEventFilter(eventFilter,maxCount);
        EventInfoListDocument eid = EventInfoListDocument.Factory.newInstance();
        final TEventInfoList eil = eid.addNewEventInfoList();
        dbexec(new BpelDatabase.Callable<Object>()  {
            public Object run(BpelDAOConnection session) throws Exception {
                List<BpelEvent> events = session.bpelEventQuery(ifilter, efilter);
                for (BpelEvent event : events) {
                    TEventInfo tei = eil.addNewEventInfo();
                    fillEventInfo(tei, event);
                }
                return null;
            }
        });
        return eid;
    }

    public ActivityExtInfoListDocument getExtensibilityElements(QName pid, Integer[] aids) {
        ActivityExtInfoListDocument aeild = ActivityExtInfoListDocument.Factory.newInstance();
        TActivitytExtInfoList taeil = aeild.addNewActivityExtInfoList();
        OProcess oprocess = _engine.getOProcess(pid);

        for (int aid : aids) {
            OBase obase = oprocess.getChild(aid);
            if (obase != null && obase.debugInfo != null && obase.debugInfo.extensibilityElements != null) {
                for (Map.Entry<QName, Object> entry : obase.debugInfo.extensibilityElements.entrySet()) {
                    TActivityExtInfo taei = taeil.addNewActivityExtInfo();
                    taei.setAiid(""+aid);
                    Object extValue = entry.getValue();
                    if (extValue instanceof Element)
                        taei.getDomNode().appendChild(taei.getDomNode()
                                .getOwnerDocument().importNode((Element) extValue, true));
                    else if (extValue instanceof String) {
                        Element valueElmt = taei.getDomNode().getOwnerDocument().createElementNS(
                                entry.getKey().getNamespaceURI(), entry.getKey().getLocalPart());
                        valueElmt.appendChild(taei.getDomNode().getOwnerDocument().createTextNode((String) extValue));
                        taei.getDomNode().appendChild(valueElmt);
                    }
                }
            }
        }
        return aeild;
    }

    /**
     * Get the {@link DebuggerSupport} object for the given process identifier. Debugger
     * support is required for operations that resume execution in some way or manipulate
     * the breakpoints.
     * @param procid process identifier
     * @return associated debugger support object
     * @throws ManagementException
     */
    protected final DebuggerSupport getDebugger(QName procid) throws ManagementException {

        BpelProcess process = _engine._activeProcesses.get(procid);
        if (process == null)
            throw new InvalidRequestException("The process \"" + procid + "\" is available." );

        return process._debugger;
    }


    /**
     * Get the {@link DebuggerSupport} object for the given instance identifier. Debugger
     * support is required for operations that resume execution in some way or manipulate
     * the breakpoints.
     * @param iid instance identifier
     * @return associated debugger support object
     * @throws ManagementException
     */
    protected final DebuggerSupport getDebugger(final Long iid) {
        QName processId;

        try {
            processId = _db.exec(new BpelDatabase.Callable<QName>() {
                public QName run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    return instance == null ? null : instance.getProcess().getProcessId();
                }
            });
        } catch (Exception e) {
            throw new ProcessingException("Exception during instance retrieval", e);
        }

        return getDebugger(processId);
    }

    /**
     * Execute a database transaction, unwrapping nested {@link ManagementException}s.
     * @param runnable action to run
     * @return
     * @throws ManagementException
     */
    protected <T> T dbexec(BpelProcessDatabase.Callable<T> runnable) throws ManagementException {
        try {
            return runnable.exec();
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            throw new ManagementException("Exception during database operation", e);
        }
    }

    /**
     * Execute a database transaction, unwrapping nested {@link ManagementException}s.
     * @param callable action to run
     * @return
     * @throws ManagementException
     */
    protected <T> T dbexec(BpelDatabase.Callable<T> callable) throws ManagementException {
        try {
            return _db.exec(callable);
        } catch (ManagementException me) {
            // Passthrough.
            throw me;
        } catch (Exception ex) {
            throw new ManagementException("Exception during database operation",ex);
        }
    }

    private ProcessInfoDocument genProcessInfoDocument(final QName procid, final ProcessInfoCustomizer custom)
            throws ManagementException {
        ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
        final TProcessInfo pi = ret.addNewProcessInfo();
        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    ProcessDAO proc = conn.getProcess(procid);
                    if (proc == null)
                        throw new InvalidRequestException("ProcessNotFound:" + procid);
                    fillProcessInfo(pi, proc, custom);
                    return null;
                }
            });
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            throw new ProcessingException("Exception while retrieving process information",e);
        }

        return ret;
    }

    /**
     * Generate a {@link InstanceInfoDocument} for a given instance. This
     * document contains general information about the instance.
     * @param iid instance identifier
     * @return generated document
     */
    private InstanceInfoDocument genInstanceInfoDocument(final Long iid) {
        if (iid == null)
            throw new InvalidRequestException("Must specifiy instance id.");

        InstanceInfoDocument ret = InstanceInfoDocument.Factory.newInstance();
        final TInstanceInfo ii = ret.addNewInstanceInfo();

        ii.setIid(iid.toString());
        dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws Exception {
                ProcessInstanceDAO instance = conn.getInstance(iid);

                if (instance == null)
                    throw new InstanceNotFoundException("" + iid);
                // TODO: deal with "ERROR" state information.
                fillInstanceInfo(ii,instance);
                return null;
            }
        });


        return ret;
    }

    /**
     * Generate a {@link ScopeInfoDocument} for a given scope instance.
     * @param siid scope instance identifier
     * @param includeActivityInfo
     * @return generated document
     */
    private ScopeInfoDocument genScopeInfoDocument(final String siid, final boolean includeActivityInfo) {
        if (siid == null)
            throw new InvalidRequestException("Must specifiy scope instance id.");

        final Long siidl;
        try {
            siidl = new Long(siid);
        } catch (NumberFormatException nfe) {
            throw new InvalidRequestException("Invalid scope instance id.");
        }

        ScopeInfoDocument ret = ScopeInfoDocument.Factory.newInstance();
        final TScopeInfo ii = ret.addNewScopeInfo();

        ii.setSiid(siid);
        dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection conn) throws Exception {
                ScopeDAO instance = conn.getScope(siidl);
                if (instance == null)
                    throw new InvalidRequestException("Scope not found: " + siidl);
                // TODO: deal with "ERROR" state information.
                fillScopeInfo(ii,instance,includeActivityInfo);
                return null;
            }
        });
        return ret;
    }

    /**
     * Fill in the <code>process-info</code> element of the transfer object.
     * @param info destination XMLBean
     * @param proc source DAO object
     * @param custom used to customize the quantity of information produced in the info
     */
    private void fillProcessInfo(TProcessInfo info, ProcessDAO proc, ProcessInfoCustomizer custom) {
        ProcessConf pconf = _store.getProcessConfiguration(proc.getProcessId());
        info.setPid(proc.getProcessId().toString());
        // TODO: ACTIVE and RETIRED should be used separately.
        //Active process may be retired at the same time
        if(!pconf.isActive()) {
            info.setStatus(TProcessStatus.RETIRED);
        } else {
            info.setStatus(TProcessStatus.ACTIVE);
        }

        TDefinitionInfo definfo = info.addNewDefinitionInfo();
        definfo.setProcessName(proc.getType());

        TDeploymentInfo depinfo = info.addNewDeploymentInfo();
        depinfo.setDeployDate(toCalendar(pconf.getDeployDate()));
        depinfo.setDeployer(pconf.getDeployer());
        if (custom.includeInstanceSummary()) {
            TInstanceSummary isum = info.addNewInstanceSummary();
            genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.ACTIVE, proc);
            genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.COMPLETED, proc);
            genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.ERROR, proc);
            genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.FAILED, proc);
            genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.SUSPENDED, proc);
            genInstanceSummaryEntry(isum.addNewInstances(),TInstanceStatus.TERMINATED, proc);
        }

        TProcessInfo.Documents docinfo = info.addNewDocuments();
        File files[] = pconf.getFiles();
        if (files != null)
            genDocumentInfo(docinfo, _store.getDeploymentDir(), files, true);
        else if (__log.isDebugEnabled())
            __log.debug("fillProcessInfo: No files for " + _store.getDeploymentDir() + " !!!");

        if (custom.includeProcessProperties()) {
            TProcessProperties properties = info.addNewProperties();
            for (Map.Entry<QName, Node> propEntry : pconf.getProperties().entrySet()) {
                TProcessProperties.Property tprocProp = properties.addNewProperty();
                tprocProp.setName(new QName(propEntry.getKey().getNamespaceURI(), propEntry.getKey().getLocalPart()));
                Node propNode = tprocProp.getDomNode();
                Document processInfoDoc = propNode.getOwnerDocument();
                Node node2append = processInfoDoc.importNode(propEntry.getValue(), true);
                propNode.appendChild(node2append);
            }
        }

        OProcess oprocess = _engine.getOProcess(proc.getProcessId());
        if (custom.includeEndpoints() && oprocess != null) {
            TEndpointReferences eprs = info.addNewEndpoints();
            for (OPartnerLink oplink : oprocess.getAllPartnerLinks()) {
                if (oplink.hasPartnerRole() && oplink.initializePartnerRole) {
                    EndpointReference pepr = _engine._activeProcesses.get(proc.getProcessId())
                            .getInitialPartnerRoleEPR(oplink);
                    
                    if (pepr!= null) {
                        TEndpointReferences.EndpointRef epr = eprs.addNewEndpointRef();
                        Document eprNodeDoc = epr.getDomNode().getOwnerDocument();
                        epr.getDomNode().appendChild(eprNodeDoc.importNode(pepr.toXML().getDocumentElement(), true));
                    }
                }
            }
        }

        //TODO: add documents to the above data structure.
    }

    /**
     * Generate document information elements for a set of files.
     * @param docinfo target element
     * @param files files
     * @param recurse recurse down directories?
     */
    private void genDocumentInfo(TProcessInfo.Documents docinfo,  File rootdir, File[] files,boolean recurse) {
        if (files == null)
            return;
        for (File f : files) {
            if (f.isHidden())
                continue;

            if (f.isDirectory()) {
                if (recurse)
                    genDocumentInfo(docinfo, rootdir, f.listFiles(), true);
            } else if (f.isFile()) {
                genDocumentInfo(docinfo, rootdir, f);
            }
        }
    }

    private void genDocumentInfo(TProcessInfo.Documents docinfo, File rootDir, File f) {
        DocumentInfoGenerator dig = new DocumentInfoGenerator(rootDir,f);
        if (dig.isRecognized() && dig.isVisible()) {
            TDocumentInfo doc = docinfo.addNewDocument();

            doc.setName(dig.getName());
            doc.setSource(dig.getURL());
            doc.setType(dig.getType());
        }
    }

    private void genInstanceSummaryEntry(TInstanceSummary.Instances instances, TInstanceStatus.Enum state, ProcessDAO proc) {
        instances.setState(state);
        String queryStatus = InstanceFilter.StatusKeys.valueOf(state.toString()).toString().toLowerCase();
        InstanceFilter instanceFilter = new InstanceFilter("status=" + queryStatus
                +  " name=" + proc.getType().getLocalPart()
                +  " namespace=" + proc.getType().getNamespaceURI());
        int count = _db.getConnection().instanceQuery(instanceFilter).size();
        instances.setCount(count);
    }

    private void fillInstanceInfo(TInstanceInfo info, ProcessInstanceDAO instance) {
        info.setIid("" + instance.getInstanceId());
        // TODO: add process QName to instance-info schema
        ProcessDAO processDAO = instance.getProcess();
        info.setPid(processDAO.getProcessId().toString());
        info.setProcessName(processDAO.getType());
        if (instance.getRootScope() != null)
            info.setRootScope(genScopeRef(instance.getRootScope()));
        info.setDtStarted(toCalendar(instance.getCreateTime()));
        info.setDtLastActive(toCalendar(instance.getLastActiveTime()));
        info.setStatus(__psc.cvtInstanceStatus(instance.getState()));
        if (instance.getFault() != null) {
            TFaultInfo faultInfo = info.addNewFaultInfo();
            faultInfo.setName(instance.getFault().getName());
            faultInfo.setExplanation(instance.getFault().getExplanation());
            faultInfo.setAiid(instance.getFault().getActivityId());
            faultInfo.setLineNumber(instance.getFault().getLineNo());
        }

        ProcessInstanceDAO.EventsFirstLastCountTuple flc = instance.getEventsFirstLastCount();
        TInstanceInfo.EventInfo eventInfo = info.addNewEventInfo();

        // Setting valued correlation properties
        if (!instance.getCorrelationSets().isEmpty()) {
            TInstanceInfo.CorrelationProperties corrProperties = info.addNewCorrelationProperties();
            for (CorrelationSetDAO correlationSetDAO : instance.getCorrelationSets()) {
                for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
                    TCorrelationProperty tproperty = corrProperties.addNewCorrelationProperty();
                    tproperty.setCsetid(""+correlationSetDAO.getCorrelationSetId());
                    tproperty.setPropertyName(property.getKey());
                    tproperty.setStringValue(property.getValue());
                }
            }
        }

        eventInfo.setFirstDtime(toCalendar(flc.first));
        eventInfo.setLastDtime(toCalendar(flc.last));
        eventInfo.setCount(flc.count);

        if (instance.getActivityFailureCount() > 0) {
          TInstanceInfo.Failures failures = info.addNewFailures();
          failures.setDtFailure(toCalendar(instance.getActivityFailureDateTime()));
          failures.setCount(instance.getActivityFailureCount());
        }
    }

    private void fillScopeInfo(TScopeInfo scopeInfo, ScopeDAO scope, boolean includeActivityInfo) {
        scopeInfo.setSiid("" + scope.getScopeInstanceId());
        scopeInfo.setName(scope.getName());
        if (scope.getParentScope() != null)
            scopeInfo.setParentScopeRef(genScopeRef(scope.getParentScope()));

        scopeInfo.setStatus(__psc.cvtScopeStatus(scope.getState()));

        TScopeInfo.Children children = scopeInfo.addNewChildren();
        for (ScopeDAO i : scope.getChildScopes())
            fillScopeRef(children.addNewChildRef(), i);

        TScopeInfo.Variables vars = scopeInfo.addNewVariables();
        for (XmlDataDAO i : scope.getVariables())
            fillVariableRef(vars.addNewVariableRef(), i);

        // Setting correlations and their valued properties
        if (!scope.getCorrelationSets().isEmpty()) {
            TScopeInfo.CorrelationSets correlationSets = scopeInfo.addNewCorrelationSets();
            for (CorrelationSetDAO correlationSetDAO : scope.getCorrelationSets()) {
                TScopeInfo.CorrelationSets.CorrelationSet correlationSet =
                        correlationSets.addNewCorrelationSet();
                correlationSet.setCsetid(""+correlationSetDAO.getCorrelationSetId());
                correlationSet.setName(correlationSetDAO.getName());
                for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
                    TCorrelationProperty tproperty = correlationSet.addNewCorrelationProperty();
                    tproperty.setCsetid(""+correlationSetDAO.getCorrelationSetId());
                    tproperty.setPropertyName(property.getKey());
                    tproperty.setStringValue(property.getValue());
                }
            }

        }

        if (includeActivityInfo) {
           Collection<ActivityRecoveryDAO> recoveries = scope.getProcessInstance().getActivityRecoveries(); 

            TScopeInfo.Activities activities = scopeInfo.addNewActivities();
            List<BpelEvent> events = scope.listEvents(null);
            ActivityStateDocumentBuilder b = new ActivityStateDocumentBuilder();
            for (BpelEvent e : events) b.onEvent(e);
            for (ActivityInfoDocument ai : b.getActivities()) {
                for (ActivityRecoveryDAO recovery : recoveries) {
                  if (String.valueOf(recovery.getActivityId()).equals(ai.getActivityInfo().getAiid())) {
                    TActivityInfo.Failure failure = ai.getActivityInfo().addNewFailure();
                    failure.setReason(recovery.getReason());
                    failure.setDtFailure(toCalendar(recovery.getDateTime()));
                    failure.setActions(recovery.getActions());
                    failure.setRetries(recovery.getRetries());
                    ai.getActivityInfo().setStatus(TActivityStatus.FAILURE);
                  }
                }
                activities.addNewActivityInfo().set(ai.getActivityInfo());
            }
        }

        Collection<PartnerLinkDAO> plinks = scope.getPartnerLinks();
        if (plinks.size() > 0) {
            TEndpointReferences refs = scopeInfo.addNewEndpoints();
            for (PartnerLinkDAO plink : plinks) {
                if (plink.getPartnerRoleName() != null && plink.getPartnerRoleName().length() > 0) {
                    TEndpointReferences.EndpointRef ref = refs.addNewEndpointRef();
                    ref.setPartnerLink(plink.getPartnerLinkName());
                    ref.setPartnerRole(plink.getPartnerRoleName());
                    if (plink.getPartnerEPR() != null) {
                        Document eprNodeDoc = ref.getDomNode().getOwnerDocument();
                        ref.getDomNode().appendChild(eprNodeDoc.importNode(plink.getPartnerEPR(), true));
                    }
                }
            }
        }
    }

    private void fillVariableRef(TVariableRef ref, XmlDataDAO i) {
        ref.setIid(i.getScopeDAO().getProcessInstance().getInstanceId().toString());
        ref.setSiid(i.getScopeDAO().getScopeInstanceId().toString());
        ref.setName(i.getName());
    }

    private TScopeRef genScopeRef(ScopeDAO scope) {
        TScopeRef tref = TScopeRef.Factory.newInstance();
        fillScopeRef(tref, scope);
        return tref;
    }

    private void fillScopeRef(TScopeRef tref, ScopeDAO scope) {
        tref.setSiid(scope.getScopeInstanceId().toString());
        tref.setStatus(__psc.cvtScopeStatus(scope.getState()));
        tref.setName(scope.getName());
        tref.setModelId("" + scope.getModelId());
    }

    private void fillEventInfo(TEventInfo info, BpelEvent event) {
        info.setName(BpelEvent.eventName(event));
        info.setType(event.getType().toString());
        info.setLineNumber(event.getLineNo());
        info.setTimestamp(toCalendar(event.getTimestamp()));
        if (event instanceof ActivityEvent) {
            info.setActivityName(((ActivityEvent)event).getActivityName());
            info.setActivityId(((ActivityEvent)event).getActivityId());
            info.setActivityType(((ActivityEvent)event).getActivityType());
            info.setActivityDefinitionId(((ActivityEvent)event).getActivityDeclarationId());
        }
        if (event instanceof CorrelationEvent) {
            info.setPortType(((CorrelationEvent)event).getPortType());
            info.setOperation(((CorrelationEvent)event).getOperation());
            info.setMexId(((CorrelationEvent)event).getMessageExchangeId());
        }
        if (event instanceof CorrelationMatchEvent) {
            info.setPortType(((CorrelationMatchEvent)event).getPortType());
        }
        if (event instanceof CorrelationSetEvent) {
            info.setCorrelationSet(((CorrelationSetEvent)event).getCorrelationSetName());
        }
        if (event instanceof CorrelationSetWriteEvent) {
            info.setCorrelationKey(((CorrelationSetWriteEvent)event).getCorrelationSetName());
        }
        if (event instanceof ExpressionEvaluationEvent) {
            info.setExpression(((ExpressionEvaluationEvent)event).getExpression());
        }
        if (event instanceof ExpressionEvaluationFailedEvent) {
            info.setFault(((ExpressionEvaluationFailedEvent)event).getFault());
        }
        if (event instanceof NewProcessInstanceEvent) {
            if ((((NewProcessInstanceEvent)event).getRootScopeId()) != null)
                info.setRootScopeId(((NewProcessInstanceEvent)event).getRootScopeId());
            info.setScopeDefinitionId(((NewProcessInstanceEvent)event).getScopeDeclarationId());
        }
        if (event instanceof PartnerLinkEvent) {
            info.setPartnerLinkName(((PartnerLinkEvent)event).getpLinkName());
        }
        if (event instanceof ProcessCompletionEvent) {
            info.setFault(((ProcessCompletionEvent)event).getFault());
        }
        if (event instanceof ProcessEvent) {
            info.setProcessId(((ProcessEvent)event).getProcessId());
            info.setProcessType(((ProcessEvent)event).getProcessName());
        }
        if (event instanceof ProcessInstanceEvent) {
            info.setInstanceId(((ProcessInstanceEvent)event).getProcessInstanceId());
        }
        if (event instanceof ProcessInstanceStartedEvent) {
            info.setRootScopeId(((ProcessInstanceStartedEvent)event).getRootScopeId());
            info.setRootScopeDeclarationId(((ProcessInstanceStartedEvent)event).getScopeDeclarationId());
        }
        if (event instanceof ProcessInstanceStateChangeEvent) {
            info.setOldState(((ProcessInstanceStateChangeEvent)event).getOldState());
            info.setNewState(((ProcessInstanceStateChangeEvent)event).getNewState());
        }
        if (event instanceof ProcessMessageExchangeEvent) {
            info.setPortType(((ProcessMessageExchangeEvent)event).getPortType());
            info.setOperation(((ProcessMessageExchangeEvent)event).getOperation());
            info.setMexId(((ProcessMessageExchangeEvent)event).getMessageExchangeId());
        }
        if (event instanceof ScopeCompletionEvent) {
            info.setSuccess(((ScopeCompletionEvent)event).isSuccess());
            info.setFault(((ScopeCompletionEvent)event).getFault());
        }
        if (event instanceof ScopeEvent) {
            info.setScopeId(((ScopeEvent)event).getScopeId());
            if (((ScopeEvent)event).getParentScopeId() != null)
                info.setParentScopeId(((ScopeEvent)event).getParentScopeId());
            if (((ScopeEvent)event).getScopeName() != null)
                info.setScopeName(((ScopeEvent)event).getScopeName());
            info.setScopeDefinitionId(((ScopeEvent)event).getScopeDeclarationId());
        }
        if (event instanceof ScopeFaultEvent) {
            info.setFault(((ScopeFaultEvent)event).getFaultType());
            info.setFaultLineNumber(((ScopeFaultEvent)event).getFaultLineNo());
            info.setExplanation(((ScopeFaultEvent)event).getExplanation());
        }
        if (event instanceof VariableEvent) {
            info.setVariableName(((VariableEvent)event).getVarName());
        }
    }

    /**
     * Convert a {@link Date} to a {@link Calendar}.
     * @param dtime a {@link Date}
     * @return a {@link Calendar}
     */
    private Calendar toCalendar(Date dtime) {
        if (dtime == null)
            return null;

        Calendar c = (Calendar) _calendar.clone();
        c.setTime(dtime);
        return c;
    }


    /**
     * @see org.apache.ode.bpel.pmapi.InstanceManagement#queryInstances(java.lang.String)
     */
    public InstanceInfoListDocument queryInstances(final String query) {
        InstanceInfoListDocument ret = InstanceInfoListDocument.Factory.newInstance();
        final TInstanceInfoList infolist = ret.addNewInstanceInfoList();

        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(query);
                    for (ProcessInstanceDAO instance : instances) {
                        fillInstanceInfo(infolist.addNewInstanceInfo(), instance);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new ProcessingException("Exception while querying instances",e);
        }

        return ret;
    }

}
