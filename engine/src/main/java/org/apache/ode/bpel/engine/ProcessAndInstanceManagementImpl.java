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

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.Filter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.ActivityEvent;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.CorrelationEvent;
import org.apache.ode.bpel.evt.CorrelationMatchEvent;
import org.apache.ode.bpel.evt.CorrelationSetEvent;
import org.apache.ode.bpel.evt.CorrelationSetWriteEvent;
import org.apache.ode.bpel.evt.ExpressionEvaluationEvent;
import org.apache.ode.bpel.evt.ExpressionEvaluationFailedEvent;
import org.apache.ode.bpel.evt.NewProcessInstanceEvent;
import org.apache.ode.bpel.evt.PartnerLinkEvent;
import org.apache.ode.bpel.evt.ProcessCompletionEvent;
import org.apache.ode.bpel.evt.ProcessEvent;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.apache.ode.bpel.evt.ProcessMessageExchangeEvent;
import org.apache.ode.bpel.evt.ScopeCompletionEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.ScopeFaultEvent;
import org.apache.ode.bpel.evt.VariableEvent;
import org.apache.ode.bpel.evtproc.ActivityStateDocumentBuilder;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;
import org.apache.ode.bpel.pmapi.ActivityExtInfoListDocument;
import org.apache.ode.bpel.pmapi.ActivityInfoDocument;
import org.apache.ode.bpel.pmapi.EventInfoListDocument;
import org.apache.ode.bpel.pmapi.InstanceInfoDocument;
import org.apache.ode.bpel.pmapi.InstanceInfoListDocument;
import org.apache.ode.bpel.pmapi.InstanceManagement;
import org.apache.ode.bpel.pmapi.InstanceNotFoundException;
import org.apache.ode.bpel.pmapi.InvalidRequestException;
import org.apache.ode.bpel.pmapi.ManagementException;
import org.apache.ode.bpel.pmapi.ProcessInfoCustomizer;
import org.apache.ode.bpel.pmapi.ProcessInfoDocument;
import org.apache.ode.bpel.pmapi.ProcessInfoListDocument;
import org.apache.ode.bpel.pmapi.ProcessManagement;
import org.apache.ode.bpel.pmapi.ProcessNotFoundException;
import org.apache.ode.bpel.pmapi.ProcessingException;
import org.apache.ode.bpel.pmapi.ScopeInfoDocument;
import org.apache.ode.bpel.pmapi.TActivityExtInfo;
import org.apache.ode.bpel.pmapi.TActivityStatus;
import org.apache.ode.bpel.pmapi.TActivitytExtInfoList;
import org.apache.ode.bpel.pmapi.TCorrelationProperty;
import org.apache.ode.bpel.pmapi.TDefinitionInfo;
import org.apache.ode.bpel.pmapi.TDeploymentInfo;
import org.apache.ode.bpel.pmapi.TDocumentInfo;
import org.apache.ode.bpel.pmapi.TEndpointReferences;
import org.apache.ode.bpel.pmapi.TEventInfo;
import org.apache.ode.bpel.pmapi.TEventInfoList;
import org.apache.ode.bpel.pmapi.TFailureInfo;
import org.apache.ode.bpel.pmapi.TFailuresInfo;
import org.apache.ode.bpel.pmapi.TFaultInfo;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TInstanceInfoList;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.bpel.pmapi.TInstanceSummary;
import org.apache.ode.bpel.pmapi.TProcessInfo;
import org.apache.ode.bpel.pmapi.TProcessInfoList;
import org.apache.ode.bpel.pmapi.TProcessProperties;
import org.apache.ode.bpel.pmapi.TProcessStatus;
import org.apache.ode.bpel.pmapi.TScopeInfo;
import org.apache.ode.bpel.pmapi.TScopeRef;
import org.apache.ode.bpel.pmapi.TVariableInfo;
import org.apache.ode.bpel.pmapi.TVariableRef;
import org.apache.ode.bpel.pmapi.VariableInfoDocument;
import org.apache.ode.bpel.rapi.ActivityModel;
import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.ProcessModel;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.apache.ode.utils.stl.UnaryFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implentation of the Process and InstanceManagement APIs.
 * 
 * @todo Move this out of the engine, it no longer belongs here.
 */
public class ProcessAndInstanceManagementImpl implements InstanceManagement, ProcessManagement {

    protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    protected static Log __log = LogFactory.getLog(BpelManagementFacadeImpl.class);

    protected static final ProcessStatusConverter __psc = new ProcessStatusConverter();

    protected BpelDatabase _db;

    protected ProcessStore _store;

    // Calendar can be expensive to initialize so we cache and clone it
    protected Calendar _calendar = Calendar.getInstance(); 

    protected BpelServerImpl _server;

    public ProcessAndInstanceManagementImpl(BpelServer server, ProcessStore store) {
        _server = (BpelServerImpl) server;
        _db = _server._db;
        _store = store;
    }

    public ProcessInfoListDocument listProcessesCustom(String filter, String orderKeys,
            final ProcessInfoCustomizer custom) {
        ProcessInfoListDocument ret = ProcessInfoListDocument.Factory.newInstance();
        final TProcessInfoList procInfoList = ret.addNewProcessInfoList();
        final ProcessFilter processFilter = new ProcessFilter(filter, orderKeys);

        for (ProcessConf pconf : processQuery(processFilter)) {
            try {
                fillProcessInfo(procInfoList.addNewProcessInfo(), pconf, custom);
            } catch (Exception e) {
                __log.error("Exception when querying process " + pconf.getProcessId(), e);
            }
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
        try {
            _store.setState(pid, org.apache.ode.bpel.iapi.ProcessState.ACTIVE);
        } catch (Exception ex) {
            __log.error("Exception while setting process state", ex);
            throw new ManagementException("Error setting process state: " + ex.toString());
        }
        return genProcessInfoDocument(pid, ProcessInfoCustomizer.NONE);
    }

    public ProcessInfoDocument setRetired(final QName pid, final boolean retired) throws ManagementException {
        try {
            _store.setState(pid, retired ? ProcessState.RETIRED : ProcessState.ACTIVE);
        } catch (BpelEngineException e) {
            __log.error("Exception while setting process as retired", e);
            throw new ProcessNotFoundException("ProcessNotFound:" + pid);
        }
        return genProcessInfoDocument(pid, ProcessInfoCustomizer.NONE);
    }

    public void setPackageRetired(final String packageName, final boolean retired)
            throws ManagementException {
        try {
            _store.setRetiredPackage(packageName, retired);
        } catch (BpelEngineException e) {
            __log.error("Exception while setting process as retired", e);
            throw new ProcessNotFoundException("PackageNotFound:" + packageName);
        }
    }

    public ProcessInfoDocument setProcessPropertyNode(final QName pid, final QName propertyName, final Node value)
            throws ManagementException {
        ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
        final TProcessInfo pi = ret.addNewProcessInfo();
        try {
            try {
                _store.setProperty(pid, propertyName, value);
            } catch (Exception ex) {
                // Likely the process no longer exists in the store.
                __log.debug("Error setting property value for " + pid + "; " + propertyName, ex);
            }

            // We have to do this after we set the property, since the
            // ProcessConf object
            // is immutable.
            ProcessConf proc = _store.getProcessConfiguration(pid);
            if (proc == null)
                throw new ProcessNotFoundException("ProcessNotFound:" + pid);

            fillProcessInfo(pi, proc, new ProcessInfoCustomizer(ProcessInfoCustomizer.Item.PROPERTIES));

        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            __log.error("Exception while setting process property", e);
            throw new ProcessingException("Exception while setting process property: " + e.toString());
        }

        return ret;
    }

    public ProcessInfoDocument setProcessProperty(final QName pid, final QName propertyName, final String value)
            throws ManagementException {
        ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
        final TProcessInfo pi = ret.addNewProcessInfo();
        try {
            try {
                _store.setProperty(pid, propertyName, value);
            } catch (Exception ex) {
                // Likely the process no longer exists in the store.
                __log.debug("Error setting property value for " + pid + "; " + propertyName, ex);
            }

            // We have to do this after we set the property, since the
            // ProcessConf object
            // is immutable.
            ProcessConf proc = _store.getProcessConfiguration(pid);
            if (proc == null)
                throw new ProcessNotFoundException("ProcessNotFound:" + pid);

            fillProcessInfo(pi, proc, new ProcessInfoCustomizer(ProcessInfoCustomizer.Item.PROPERTIES));

        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            __log.error("Exception while setting process property", e);
            throw new ProcessingException("Exception while setting process property" + e.toString());
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
            __log.error("Exception while listing instances", e);
            throw new ProcessingException("Exception while listing instances: " + e.toString());
        }
        return ret;
    }

    public InstanceInfoListDocument listInstancesSummary(String filter, String order, int limit) {
        InstanceInfoListDocument ret = InstanceInfoListDocument.Factory.newInstance();
        final TInstanceInfoList infolist = ret.addNewInstanceInfoList();
        final InstanceFilter instanceFilter = new InstanceFilter(filter, order, limit);
        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
                    Map<Long, Collection<CorrelationSetDAO>> icsets = conn.getCorrelationSets(instances);
                    conn.getProcessManagement().prefetchActivityFailureCounts(instances);
                    for (ProcessInstanceDAO instance : instances) {
                        TInstanceInfo info = infolist.addNewInstanceInfo();
                        fillInstanceSummary(info, instance);
                        Collection<CorrelationSetDAO> csets = icsets.get(instance.getInstanceId());
                        if (csets != null) {
                            for (CorrelationSetDAO cset: csets) {
                                Map<QName, String> props = cset.getProperties();
                                fillProperties(info, instance, props);
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            __log.error("Exception while listing instances", e);
            throw new ProcessingException("Exception while listing instances: " + e.toString());
        }
        return ret;
    }

    public InstanceInfoListDocument listAllInstances() {
        return listInstancesSummary(null, null, Integer.MAX_VALUE);
    }

    public InstanceInfoListDocument listAllInstancesWithLimit(int limit) {
        return listInstancesSummary(null, null, limit);
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

    public VariableInfoDocument getVariableInfo(final String scopeId, final String varName) throws ManagementException {
        VariableInfoDocument ret = VariableInfoDocument.Factory.newInstance();
        final TVariableInfo vinf = ret.addNewVariableInfo();
        final TVariableRef sref = vinf.addNewSelf();
        dbexec(new BpelDatabase.Callable<Object>() {
            public Object run(BpelDAOConnection session) throws Exception {
                ScopeDAO scope = session.getScope(new Long(scopeId));
                if (scope == null) {
                    throw new InvalidRequestException("ScopeNotFound:" + scopeId);
                }

                sref.setSiid(scopeId);
                sref.setIid(scope.getProcessInstance().getInstanceId().toString());
                sref.setName(varName);

                XmlDataDAO var = scope.getVariable(varName);
                if (var == null) {
                    throw new InvalidRequestException("VarNotFound:" + varName);
                }

                Node nval = var.get();
                if (nval != null) {
                    TVariableInfo.Value val = vinf.addNewValue();
                    val.getDomNode().appendChild(val.getDomNode().getOwnerDocument().importNode(nval, true));
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
        // TODO: implement me!
        return genInstanceInfoDocument(iid);
    }

    public InstanceInfoDocument resume(final Long iid) {
        // We need debugger support in order to resume (since we have to force
        // a reduction. If one is not available the getDebugger() method should
        // throw a ProcessingException
        getDebugger(iid).resume(iid);

        return genInstanceInfoDocument(iid);
    }

    public InstanceInfoDocument suspend(final Long iid) throws ManagementException {
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
                    for (ActivityRecoveryDAO recovery : instance.getActivityRecoveries()) {
                        if (recovery.getActivityId() == aid) {
                            ODEProcess process = _server.getBpelProcess(instance.getProcess().getProcessId());
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
            __log.error("Exception during activity recovery", e);
            throw new ProcessingException("Exception during activity recovery" + e.toString());
        }
        return genInstanceInfoDocument(iid);
    }

    public Collection<Long> delete(String filter) {
        final InstanceFilter instanceFilter = new InstanceFilter(filter);

        final List<Long> ret = new LinkedList<Long>();
        try {

            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) {
                    Collection<ProcessInstanceDAO> instances = conn.instanceQuery(instanceFilter);
                    for (ProcessInstanceDAO instance : instances) {
                        instance.delete(EnumSet.allOf(CLEANUP_CATEGORY.class));
                        ret.add(instance.getInstanceId());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            __log.error("Exception during instance deletion", e);
            throw new ProcessingException("Exception during instance deletion: " + e.toString());
        }

        return ret;
    }

    //
    // EVENT RETRIEVAL
    //
    public List<String> getEventTimeline(String instanceFilter, String eventFilter) {
        final InstanceFilter ifilter = new InstanceFilter(instanceFilter, null, 0);
        final BpelEventFilter efilter = new BpelEventFilter(eventFilter, 0);

        List<Date> tline = dbexec(new BpelDatabase.Callable<List<Date>>() {
            public List<Date> run(BpelDAOConnection session) throws Exception {
                return session.bpelEventTimelineQuery(ifilter, efilter);
            }
        });

        ArrayList<String> ret = new ArrayList<String>(tline.size());
        CollectionsX.transform(ret, tline, new UnaryFunction<Date, String>() {
            public String apply(Date x) {
                return ISO8601DateParser.format(x);
            }
        });
        return ret;
    }

    public EventInfoListDocument listEvents(String instanceFilter, String eventFilter, int maxCount) {
        final InstanceFilter ifilter = new InstanceFilter(instanceFilter, null, 0);
        final BpelEventFilter efilter = new BpelEventFilter(eventFilter, maxCount);
        EventInfoListDocument eid = EventInfoListDocument.Factory.newInstance();
        final TEventInfoList eil = eid.addNewEventInfoList();
        dbexec(new BpelDatabase.Callable<Object>() {
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
        ProcessModel pmodel = _server.getProcessModel(pid);
        if (pmodel == null)
            throw new ProcessNotFoundException("The process \"" + pid + "\" does not exist.");

        for (int aid : aids) {
            ActivityModel amodel = pmodel.getChild(aid);
            if (amodel != null && amodel.getExtensibilityElements() != null) {
                for (Map.Entry<QName, Object> entry : amodel.getExtensibilityElements().entrySet()) {
                    TActivityExtInfo taei = taeil.addNewActivityExtInfo();
                    taei.setAiid("" + aid);
                    Object extValue = entry.getValue();
                    if (extValue instanceof Element)
                        taei.getDomNode().appendChild(
                                taei.getDomNode().getOwnerDocument().importNode((Element) extValue, true));
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
     * Get the {@link DebuggerSupport} object for the given process identifier.
     * Debugger support is required for operations that resume execution in some
     * way or manipulate the breakpoints.
     * 
     * @param procid
     *            process identifier
     * @return associated debugger support object
     * @throws ManagementException
     */
    protected final DebuggerSupport getDebugger(QName procid) throws ManagementException {

        ODEProcess process = _server.getBpelProcess(procid);
        if (process == null)
            throw new ProcessNotFoundException("The process \"" + procid + "\" does not exist.");

        return process._debugger;
    }

    /**
     * Get the {@link DebuggerSupport} object for the given instance identifier.
     * Debugger support is required for operations that resume execution in some
     * way or manipulate the breakpoints.
     * 
     * @param iid
     *            instance identifier
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
            __log.error("Exception during instance retrieval", e);
            throw new ProcessingException("Exception during instance retrieval: " + e.toString());
        }

        return getDebugger(processId);
    }

    /**
     * Execute a database transaction, unwrapping nested
     * {@link ManagementException}s.
     * 
     * @param runnable
     *            action to run
     * @return
     * @throws ManagementException
     */
    protected <T> T dbexec(BpelProcessDatabase.Callable<T> runnable) throws ManagementException {
        try {
            return runnable.exec();
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            __log.error("Exception during database operation", e);
            throw new ManagementException("Exception during database operation: " + e.toString());
        }
    }

    /**
     * Execute a database transaction, unwrapping nested
     * {@link ManagementException}s.
     * 
     * @param callable
     *            action to run
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
            __log.error("Exception during database operation", ex);
            throw new ManagementException("Exception during database operation" + ex.toString());
        }
    }

    private ProcessInfoDocument genProcessInfoDocument(final QName procid, final ProcessInfoCustomizer custom)
            throws ManagementException {
        if (procid == null) {
            throw new InvalidRequestException("Valid QName as process id expected.");
        }
        ProcessInfoDocument ret = ProcessInfoDocument.Factory.newInstance();
        final TProcessInfo pi = ret.addNewProcessInfo();
        try {
            ProcessConf pconf = _store.getProcessConfiguration(procid);
            if (pconf == null)
                throw new ProcessNotFoundException("ProcessNotFound:" + procid);
            fillProcessInfo(pi, pconf, custom);
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            __log.error("Exception while retrieving process information", e);
            throw new ProcessingException("Exception while retrieving process information: " + e.toString());
        }

        return ret;
    }

    /**
     * Generate a {@link InstanceInfoDocument} for a given instance. This
     * document contains general information about the instance.
     * 
     * @param iid
     *            instance identifier
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
                    throw new InstanceNotFoundException("InstanceNotFoundException " + iid);
                // TODO: deal with "ERROR" state information.
                fillInstanceInfo(ii, instance);
                return null;
            }
        });

        return ret;
    }

    /**
     * Generate a {@link ScopeInfoDocument} for a given scope instance.
     * 
     * @param siid
     *            scope instance identifier
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
                try {
                ScopeDAO instance = conn.getScope(siidl);
                if (instance == null)
                    throw new InvalidRequestException("Scope not found: " + siidl);
                // TODO: deal with "ERROR" state information.
                fillScopeInfo(ii, instance, includeActivityInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return ret;
    }

    /**
     * Fill in the <code>process-info</code> element of the transfer object.
     * 
     * @param info
     *            destination XMLBean
     * @param pconf
     *            process configuration object (from store)
     * @param custom
     *            used to customize the quantity of information produced in the
     *            info
     */
    private void fillProcessInfo(TProcessInfo info, ProcessConf pconf, ProcessInfoCustomizer custom) {
        if (pconf == null)
            throw new IllegalArgumentException("Null pconf.");

        info.setPid(pconf.getProcessId().toString());
        // TODO: ACTIVE and RETIRED should be used separately.
        // Active process may be retired at the same time
        if (pconf.getState() == ProcessState.RETIRED) {
            info.setStatus(TProcessStatus.RETIRED);
        } else {
            info.setStatus(TProcessStatus.ACTIVE);
        }
        info.setVersion(pconf.getVersion());

        TDefinitionInfo definfo = info.addNewDefinitionInfo();
        definfo.setProcessName(pconf.getType());

        TDeploymentInfo depinfo = info.addNewDeploymentInfo();
        depinfo.setPackage(pconf.getPackage());
        depinfo.setDocument(pconf.getBpelDocument());
        depinfo.setDeployDate(toCalendar(pconf.getDeployDate()));
        if (custom.includeInstanceSummary()) {
            TInstanceSummary isum = info.addNewInstanceSummary();
            genInstanceSummaryEntry(isum.addNewInstances(), TInstanceStatus.ACTIVE, pconf);
            genInstanceSummaryEntry(isum.addNewInstances(), TInstanceStatus.COMPLETED, pconf);
            genInstanceSummaryEntry(isum.addNewInstances(), TInstanceStatus.ERROR, pconf);
            genInstanceSummaryEntry(isum.addNewInstances(), TInstanceStatus.FAILED, pconf);
            genInstanceSummaryEntry(isum.addNewInstances(), TInstanceStatus.SUSPENDED, pconf);
            genInstanceSummaryEntry(isum.addNewInstances(), TInstanceStatus.TERMINATED, pconf);
            getInstanceSummaryActivityFailure(isum, pconf);
        }

        TProcessInfo.Documents docinfo = info.addNewDocuments();
        List<File> files = pconf.getFiles();
        if (files != null)
            genDocumentInfo(docinfo, files.toArray(new File[files.size()]), true);
        else if (__log.isDebugEnabled())
            __log.debug("fillProcessInfo: No files for " + pconf.getProcessId());

        TProcessProperties properties = info.addNewProperties();
        if (custom.includeProcessProperties()) {
            for (Map.Entry<QName, Node> propEntry : pconf.getProcessProperties().entrySet()) {
                TProcessProperties.Property tprocProp = properties.addNewProperty();
                tprocProp.setName(new QName(propEntry.getKey().getNamespaceURI(), propEntry.getKey().getLocalPart()));
                Node propNode = tprocProp.getDomNode();
                Document processInfoDoc = propNode.getOwnerDocument();
                Node node2append = processInfoDoc.importNode(propEntry.getValue(), true);
                propNode.appendChild(node2append);
            }
        }

        TEndpointReferences eprs = info.addNewEndpoints();
        ProcessModel pmodel = _server.getProcessModel(pconf.getProcessId());
        if (custom.includeEndpoints() && pmodel != null) {
            for (PartnerLinkModel oplink : pmodel.getAllPartnerLinks()) {
                if (oplink.hasPartnerRole() && oplink.isInitializePartnerRoleSet()) {
                    // TODO: this is very uncool.
                    EndpointReference pepr = ((ODEWSProcess)_server.getBpelProcess(pconf.getProcessId()))
                            .getInitialPartnerRoleEPR(oplink);
                    if (pepr != null) {
                        TEndpointReferences.EndpointRef epr = eprs.addNewEndpointRef();
                        Document eprNodeDoc = epr.getDomNode().getOwnerDocument();
                        epr.getDomNode().appendChild(eprNodeDoc.importNode(pepr.toXML().getDocumentElement(), true));
                    }
                }
            }
        }

    }

    /**
     * Generate document information elements for a set of files.
     * 
     * @param docinfo
     *            target element
     * @param files
     *            files
     * @param recurse
     *            recurse down directories?
     */
    private void genDocumentInfo(TProcessInfo.Documents docinfo, File[] files, boolean recurse) {
        if (files == null)
            return;
        for (File f : files) {
            if (f.isHidden())
                continue;

            if (f.isDirectory()) {
                if (recurse)
                    genDocumentInfo(docinfo, f.listFiles(), true);
            } else if (f.isFile()) {
                genDocumentInfo(docinfo, f);
            }
        }
    }

    private void genDocumentInfo(TProcessInfo.Documents docinfo, File f) {
        DocumentInfoGenerator dig = new DocumentInfoGenerator(f);
        if (dig.isRecognized() && dig.isVisible()) {
            TDocumentInfo doc = docinfo.addNewDocument();

            doc.setName(dig.getName());
            doc.setSource(dig.getURL());
            doc.setType(dig.getType());
        }
    }

    private void genInstanceSummaryEntry(TInstanceSummary.Instances instances, TInstanceStatus.Enum state,
            ProcessConf pconf) {
        instances.setState(state);
        String queryStatus = InstanceFilter.StatusKeys.valueOf(state.toString()).toString().toLowerCase();
        final InstanceFilter instanceFilter = new InstanceFilter("status=" + queryStatus 
                + " pid="+ pconf.getProcessId());
        
        int count = dbexec(new BpelDatabase.Callable<Integer>() {

            public Integer run(BpelDAOConnection conn) throws Exception {
                return conn.instanceQuery(instanceFilter).size();
            }
        });
        instances.setCount(count);
    }

    private void getInstanceSummaryActivityFailure(final TInstanceSummary summary, ProcessConf pconf) {
        String queryStatus = InstanceFilter.StatusKeys.valueOf(TInstanceStatus.ACTIVE.toString()).toString()
                .toLowerCase();
        final InstanceFilter instanceFilter = new InstanceFilter("status=" + queryStatus 
                + " pid="+ pconf.getProcessId());
        dbexec(new BpelDatabase.Callable<Void>() {

            public Void run(BpelDAOConnection conn) throws Exception {
                Date lastFailureDt = null;
                int failureInstances = 0;
                for (ProcessInstanceDAO instance : conn.instanceQuery(instanceFilter)) {
                    int count = instance.getActivityFailureCount();
                    if (count > 0) {
                        ++failureInstances;
                        Date failureDt = instance.getActivityFailureDateTime();
                        if (lastFailureDt == null || lastFailureDt.before(failureDt))
                            lastFailureDt = failureDt;
                    }
                }
                if (failureInstances > 0) {
                    TFailuresInfo failures = summary.addNewFailures();
                    failures.setDtFailure(toCalendar(lastFailureDt));
                    failures.setCount(failureInstances);
                }

                return null;
            }

        });
    }

    private void fillProperties(TInstanceInfo info, ProcessInstanceDAO instance, Map<QName, String> props) {
        TInstanceInfo.CorrelationProperties corrProperties = info.addNewCorrelationProperties();
        for (Map.Entry<QName, String> property : props.entrySet()) {
            TCorrelationProperty tproperty = corrProperties.addNewCorrelationProperty();
            // not setting correlation-set id here -- too inconvenient for performance
            // tproperty.setCsetid("" + cset.getCorrelationSetId());
            tproperty.setPropertyName(property.getKey());
            tproperty.setStringValue(property.getValue());
        }
    }

    private void fillInstanceSummary(TInstanceInfo info, ProcessInstanceDAO instance) {
        info.setIid("" + instance.getInstanceId());
        ProcessDAO processDAO = instance.getProcess();
        info.setPid(processDAO.getProcessId().toString());
        info.setProcessName(processDAO.getType());
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
        if (instance.getActivityFailureCount() > 0) {
            TFailuresInfo failures = info.addNewFailures();
            failures.setDtFailure(toCalendar(instance.getActivityFailureDateTime()));
            failures.setCount(instance.getActivityFailureCount());
        }
    }
    
    private void fillInstanceInfo(TInstanceInfo info, ProcessInstanceDAO instance) {
        fillInstanceSummary(info, instance);

        if (instance.getRootScope() != null)
            info.setRootScope(genScopeRef(instance.getRootScope()));

        ProcessInstanceDAO.EventsFirstLastCountTuple flc = instance.getEventsFirstLastCount();
        TInstanceInfo.EventInfo eventInfo = info.addNewEventInfo();
        if (flc != null) {
            eventInfo.setFirstDtime(toCalendar(flc.first));
            eventInfo.setLastDtime(toCalendar(flc.last));
            eventInfo.setCount(flc.count);
        }

        // Setting valued correlation properties
        if (!instance.getCorrelationSets().isEmpty()) {
            TInstanceInfo.CorrelationProperties corrProperties = info.addNewCorrelationProperties();
            for (CorrelationSetDAO correlationSetDAO : instance.getCorrelationSets()) {
                for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
                    TCorrelationProperty tproperty = corrProperties.addNewCorrelationProperty();
                    tproperty.setCsetid("" + correlationSetDAO.getCorrelationSetId());
                    tproperty.setPropertyName(property.getKey());
                    tproperty.setStringValue(property.getValue());
                }
            }
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
                TScopeInfo.CorrelationSets.CorrelationSet correlationSet = correlationSets.addNewCorrelationSet();
                correlationSet.setCsetid("" + correlationSetDAO.getCorrelationSetId());
                correlationSet.setName(correlationSetDAO.getName());
                for (Map.Entry<QName, String> property : correlationSetDAO.getProperties().entrySet()) {
                    TCorrelationProperty tproperty = correlationSet.addNewCorrelationProperty();
                    tproperty.setCsetid("" + correlationSetDAO.getCorrelationSetId());
                    tproperty.setPropertyName(property.getKey());
                    tproperty.setStringValue(property.getValue());
                }
            }

        }

        if (includeActivityInfo) {
            Collection<ActivityRecoveryDAO> recoveries = scope.getProcessInstance().getActivityRecoveries();

            TScopeInfo.Activities activities = scopeInfo.addNewActivities();
            List<BpelEvent> events = scope.listEvents();
            ActivityStateDocumentBuilder b = new ActivityStateDocumentBuilder();
            for (BpelEvent e : events)
                b.onEvent(e);
            for (ActivityInfoDocument ai : b.getActivities()) {
                for (ActivityRecoveryDAO recovery : recoveries) {
                    if (String.valueOf(recovery.getActivityId()).equals(ai.getActivityInfo().getAiid())) {
                        TFailureInfo failure = ai.getActivityInfo().addNewFailure();
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
            info.setActivityName(((ActivityEvent) event).getActivityName());
            info.setActivityId(((ActivityEvent) event).getActivityId());
            info.setActivityType(((ActivityEvent) event).getActivityType());
            info.setActivityDefinitionId(((ActivityEvent) event).getActivityDeclarationId());
        }
        if (event instanceof CorrelationEvent) {
            info.setPortType(((CorrelationEvent) event).getPortType());
            info.setOperation(((CorrelationEvent) event).getOperation());
            info.setMexId(((CorrelationEvent) event).getMessageExchangeId());
        }
        if (event instanceof CorrelationMatchEvent) {
            info.setPortType(((CorrelationMatchEvent) event).getPortType());
        }
        if (event instanceof CorrelationSetEvent) {
            info.setCorrelationSet(((CorrelationSetEvent) event).getCorrelationSetName());
        }
        if (event instanceof CorrelationSetWriteEvent) {
            info.setCorrelationKey(((CorrelationSetWriteEvent) event).getCorrelationSetName());
        }
        if (event instanceof ExpressionEvaluationEvent) {
            info.setExpression(((ExpressionEvaluationEvent) event).getExpression());
        }
        if (event instanceof ExpressionEvaluationFailedEvent) {
            info.setFault(((ExpressionEvaluationFailedEvent) event).getFault());
        }
        if (event instanceof NewProcessInstanceEvent) {
            if ((((NewProcessInstanceEvent) event).getRootScopeId()) != null)
                info.setRootScopeId(((NewProcessInstanceEvent) event).getRootScopeId());
            info.setScopeDefinitionId(((NewProcessInstanceEvent) event).getScopeDeclarationId());
        }
        if (event instanceof PartnerLinkEvent) {
            info.setPartnerLinkName(((PartnerLinkEvent) event).getpLinkName());
        }
        if (event instanceof ProcessCompletionEvent) {
            info.setFault(((ProcessCompletionEvent) event).getFault());
        }
        if (event instanceof ProcessEvent) {
            info.setProcessId(((ProcessEvent) event).getProcessId());
            info.setProcessType(((ProcessEvent) event).getProcessName());
        }
        if (event instanceof ProcessInstanceEvent) {
            info.setInstanceId(((ProcessInstanceEvent) event).getProcessInstanceId());
        }
        if (event instanceof ProcessInstanceStartedEvent) {
            info.setRootScopeId(((ProcessInstanceStartedEvent) event).getRootScopeId());
            info.setRootScopeDeclarationId(((ProcessInstanceStartedEvent) event).getScopeDeclarationId());
        }
        if (event instanceof ProcessInstanceStateChangeEvent) {
            info.setOldState(((ProcessInstanceStateChangeEvent) event).getOldState());
            info.setNewState(((ProcessInstanceStateChangeEvent) event).getNewState());
        }
        if (event instanceof ProcessMessageExchangeEvent) {
            info.setPortType(((ProcessMessageExchangeEvent) event).getPortType());
            info.setOperation(((ProcessMessageExchangeEvent) event).getOperation());
            info.setMexId(((ProcessMessageExchangeEvent) event).getMessageExchangeId());
        }
        if (event instanceof ScopeCompletionEvent) {
            info.setSuccess(((ScopeCompletionEvent) event).isSuccess());
            info.setFault(((ScopeCompletionEvent) event).getFault());
        }
        if (event instanceof ScopeEvent) {
            info.setScopeId(((ScopeEvent) event).getScopeId());
            if (((ScopeEvent) event).getParentScopeId() != null)
                info.setParentScopeId(((ScopeEvent) event).getParentScopeId());
            if (((ScopeEvent) event).getScopeName() != null)
                info.setScopeName(((ScopeEvent) event).getScopeName());
            info.setScopeDefinitionId(((ScopeEvent) event).getScopeDeclarationId());
        }
        if (event instanceof ScopeFaultEvent) {
            info.setFault(((ScopeFaultEvent) event).getFaultType());
            info.setFaultLineNumber(((ScopeFaultEvent) event).getFaultLineNo());
            info.setExplanation(((ScopeFaultEvent) event).getExplanation());
        }
        if (event instanceof VariableEvent) {
            info.setVariableName(((VariableEvent) event).getVarName());
        }
    }

    /**
     * Convert a {@link Date} to a {@link Calendar}.
     * 
     * @param dtime
     *            a {@link Date}
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
     * @deprecated use listInstances instead
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
            __log.error("Exception while querying instances", e);
            throw new ProcessingException("Exception while querying instances: " + e.toString());
        }

        return ret;
    }

    /**
     * Query processes based on a {@link ProcessFilter} criteria. This is
     * implemented in memory rather than via database calls since the processes
     * are managed by the {@link ProcessStore} object and we don't want to make
     * this needlessly complicated.
     * 
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    Collection<ProcessConf> processQuery(ProcessFilter filter) {

        List<QName> pids = _store.getProcesses();

        // Name filter can be implemented using only the PIDs.
        if (filter != null && filter.getNameFilter() != null) {
            final Pattern pattern = Pattern.compile(filter.getNameFilter().replace("*",".*") + "(-\\d*)?");
            CollectionsX.remove_if(pids, new MemberOfFunction<QName>() {
                @Override
                public boolean isMember(QName o) {
                    return !pattern.matcher(o.getLocalPart()).matches();
                }
            });
        }

        if (filter != null && filter.getNamespaceFilter() != null) {
            final Pattern pattern = Pattern.compile(filter.getNamespaceFilter().replace("*",".*"));
            CollectionsX.remove_if(pids, new MemberOfFunction<QName>() {
                @Override
                public boolean isMember(QName o) {
                    String ns = o.getNamespaceURI() == null ? "" : o.getNamespaceURI();
                    return !pattern.matcher(ns).matches();
                }

            });
        }

        // Now we need the process conf objects, we need to be
        // careful since someone could have deleted them by now
        List<ProcessConf> confs = new LinkedList<ProcessConf>();
        for (QName pid : pids) {
            ProcessConf pconf = _store.getProcessConfiguration(pid);
            confs.add(pconf);
        }

        if (filter != null) {
            // TODO Implement process status filtering when status will exist
            // Specific filter for deployment date.
            if (filter.getDeployedDateFilter() != null) {
                for (final String ddf : filter.getDeployedDateFilter()) {
                    final Date dd;
                    try {
                        dd = ISO8601DateParser.parse(Filter.getDateWithoutOp(ddf));
                    } catch (ParseException e) {
                        // Should never happen.
                        __log.error("Exception while parsing date", e);
                        throw new RuntimeException(e.toString());
                    }

                    CollectionsX.remove_if(confs, new MemberOfFunction<ProcessConf>() {
                        @Override
                        public boolean isMember(ProcessConf o) {

                            if (ddf.startsWith("="))
                                return !o.getDeployDate().equals(dd);

                            if (ddf.startsWith("<="))
                                return o.getDeployDate().getTime() > dd.getTime();

                            if (ddf.startsWith(">="))
                                return o.getDeployDate().getTime() < dd.getTime();

                            if (ddf.startsWith("<"))
                                return o.getDeployDate().getTime() >= dd.getTime();

                            if (ddf.startsWith(">"))
                                return o.getDeployDate().getTime() <= dd.getTime();

                            return false;
                        }

                    });

                }
            }

            // Ordering
            if (filter.getOrders() != null) {
                ComparatorChain cchain = new ComparatorChain();
                for (String key : filter.getOrders()) {
                    boolean ascending = true;
                    String orderKey = key;
                    if (key.startsWith("+") || key.startsWith("-")) {
                        orderKey = key.substring(1, key.length());
                        if (key.startsWith("-"))
                            ascending = false;
                    }

                    Comparator c;
                    if ("name".equals(orderKey))
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return o1.getProcessId().getLocalPart().compareTo(o2.getProcessId().getLocalPart());
                            }
                        };
                    else if ("namespace".equals(orderKey))
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                String ns1 = o1.getProcessId().getNamespaceURI() == null ? "" : o1.getProcessId()
                                        .getNamespaceURI();
                                String ns2 = o2.getProcessId().getNamespaceURI() == null ? "" : o2.getProcessId()
                                        .getNamespaceURI();
                                return ns1.compareTo(ns2);
                            }
                        };
                    else if ("version".equals(orderKey))
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return (int) (o1.getVersion() - o2.getVersion());
                            }
                        };
                    else if ("deployed".equals(orderKey))
                        c = new Comparator<ProcessConf>() {
                            public int compare(ProcessConf o1, ProcessConf o2) {
                                return o1.getDeployDate().compareTo(o2.getDeployDate());
                            }

                        };

                    else {
                        // unrecognized
                        __log.debug("unrecognized order key" + orderKey);
                        continue;
                    }

                    cchain.addComparator(c, !ascending);
                }

                Collections.sort(confs, cchain);
            }
            
        }

        return confs;
    }
}
