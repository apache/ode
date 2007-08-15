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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.bpel.evt.CorrelationSetWriteEvent;
import org.apache.ode.bpel.evt.ProcessCompletionEvent;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.apache.ode.bpel.evt.ProcessMessageExchangeEvent;
import org.apache.ode.bpel.evt.ProcessTerminationEvent;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.MessageExchange.AckType;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.runtime.BpelJacobRunnable;
import org.apache.ode.bpel.runtime.BpelRuntimeContext;
import org.apache.ode.bpel.runtime.CorrelationSetInstance;
import org.apache.ode.bpel.runtime.ExpressionLanguageRuntimeRegistry;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.bpel.runtime.Selector;
import org.apache.ode.bpel.runtime.VariableInstance;
import org.apache.ode.bpel.runtime.channels.ActivityRecoveryChannel;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.InvokeResponseChannel;
import org.apache.ode.bpel.runtime.channels.PickResponseChannel;
import org.apache.ode.bpel.runtime.channels.TimerResponseChannel;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.ObjectPrinter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * 
 * @author Maciej Szefler
 */
class BpelRuntimeContextImpl implements BpelRuntimeContext {

    private static final Log __log = LogFactory.getLog(BpelRuntimeContextImpl.class);

    /** Data-access object for process instance. */
    private ProcessInstanceDAO _dao;

    /** Process Instance ID */
    private final Long _iid;

    /** JACOB VPU */
    protected JacobVPU _vpu;

    /** JACOB ExecutionQueue (state) */
    protected ExecutionQueueImpl _soup;

    private MessageExchangeDAO _instantiatingMessageExchange;

    private BpelInstanceWorker _instanceWorker;

    private BpelProcess _bpelProcess;

    /** Five second maximum for continous execution. */
    private long _maxReductionTimeMs = 2000000;

    private Contexts _contexts;

    private boolean _executed;

    BpelRuntimeContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO dao) {
        this(instanceWorker, dao, new ExecutionQueueImpl(null));

        // The following allows us to skip deserialization of the soup if our execution state in memory is the same
        // as that in the database.
        Object cachedState = instanceWorker.getCachedState(dao.getExecutionStateCounter());
        if (cachedState != null) {
            if (__log.isDebugEnabled())
                __log.debug("CACHE HIT: Using cached state #" + dao.getExecutionStateCounter() + " to resume instance " + dao.getInstanceId());
            _soup = (ExecutionQueueImpl) cachedState; 
            _soup.setReplacementMap(_bpelProcess.getReplacementMap());
            _vpu.setContext(_soup);
        } else {
            if (__log.isDebugEnabled())
                __log.debug("CACHE MISS: Loading state to resume instance " + dao.getInstanceId() + " from database ");
            byte[] daoState = dao.getExecutionState();
            ByteArrayInputStream iis = new ByteArrayInputStream(daoState);
            try {
                _soup.read(iis);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    BpelRuntimeContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO dao, PROCESS PROCESS,
            MessageExchangeDAO instantiatingMessageExchange) {

        this(instanceWorker, dao, new ExecutionQueueImpl(null));

        if (PROCESS == null)
            throw new NullPointerException();
        if (instantiatingMessageExchange == null)
            throw new NullPointerException();
        _soup.setGlobalData(new OutstandingRequestManager());
        _instantiatingMessageExchange = instantiatingMessageExchange;
        _vpu.inject(PROCESS);

    }

    BpelRuntimeContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO dao, ExecutionQueueImpl soup) {
        _instanceWorker = instanceWorker;
        _bpelProcess = instanceWorker._process;
        _contexts = instanceWorker._contexts;
        _dao = dao;
        _iid = dao.getInstanceId();
        _vpu = new JacobVPU();
        _vpu.registerExtension(BpelRuntimeContext.class, this);
        _soup = soup;
        _soup.setReplacementMap(_bpelProcess.getReplacementMap());
        _vpu.setContext(_soup);
        if (BpelProcess.__log.isDebugEnabled()) {
            __log.debug("BpelRuntimeContextImpl created for instance " + _iid + ". INDEXED STATE=" + _soup.getIndex());
        }
    }

    public String toString() {
        return "{BpelRuntimeCtx PID=" + _bpelProcess.getPID() + ", IID=" + _iid + "}";
    }

    public Long getPid() {
        return _iid;
    }

    public long genId() {
        return _dao.genMonotonic();
    }

    /**
     * @see BpelRuntimeContext#isCorrelationInitialized(org.apache.ode.bpel.runtime.CorrelationSetInstance)
     */
    public boolean isCorrelationInitialized(CorrelationSetInstance correlationSet) {
        ScopeDAO scopeDAO = _dao.getScope(correlationSet.scopeInstance);
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(correlationSet.declaration.name);

        return cs.getValue() != null;
    }

    /**
     * @see BpelRuntimeContext#isVariableInitialized(org.apache.ode.bpel.runtime.VariableInstance)
     */
    public boolean isVariableInitialized(VariableInstance var) {
        ScopeDAO scopeDAO = _dao.getScope(var.scopeInstance);
        XmlDataDAO dataDAO = scopeDAO.getVariable(var.declaration.name);
        return !dataDAO.isNull();
    }

    public boolean isPartnerRoleEndpointInitialized(PartnerLinkInstance pLink) {
        PartnerLinkDAO spl = fetchPartnerLinkDAO(pLink);

        return spl.getPartnerEPR() != null || _bpelProcess.getInitialPartnerRoleEPR(pLink.partnerLink) != null;
    }

    /**
     * @see BpelRuntimeContext#completedFault(org.apache.ode.bpel.runtime.channels.FaultData)
     */
    public void completedFault(FaultData faultData) {
        if (BpelProcess.__log.isDebugEnabled()) {
            BpelProcess.__log.debug("ProcessImpl completed with fault '" + faultData.getFaultName() + "'");
        }

        _dao.setFault(faultData.getFaultName(), faultData.getExplanation(), faultData.getFaultLineNo(), faultData.getActivityId(),
                faultData.getFaultMessage());

        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_COMPLETED_WITH_FAULT);
        evt.setNewState(ProcessState.STATE_COMPLETED_WITH_FAULT);
        sendEvent(evt);

        sendEvent(new ProcessCompletionEvent(faultData.getFaultName()));
        _dao.finishCompletion();

        cleanupOutstandingMyRoleExchanges(faultData);
    }

    /**
     * @see BpelRuntimeContext#completedOk()
     */
    public void completedOk() {
        if (BpelProcess.__log.isDebugEnabled()) {
            BpelProcess.__log.debug("ProcessImpl " + _bpelProcess.getPID() + " completed OK.");
        }

        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_COMPLETED_OK);
        evt.setNewState(ProcessState.STATE_COMPLETED_OK);
        sendEvent(evt);

        sendEvent(new ProcessCompletionEvent(null));
        _dao.finishCompletion();

        cleanupOutstandingMyRoleExchanges();
    }

    /**
     * @see BpelRuntimeContext#createScopeInstance(Long, org.apache.ode.bpel.o.OScope)
     */
    public Long createScopeInstance(Long parentScopeId, OScope scope) {
        if (BpelProcess.__log.isTraceEnabled()) {
            BpelProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("createScopeInstance", new Object[] { "parentScopeId",
                    parentScopeId, "scope", scope }));
        }

        ScopeDAO parent = null;

        if (parentScopeId != null) {
            parent = _dao.getScope(parentScopeId);
        }

        ScopeDAO scopeDao = _dao.createScope(parent, scope.name, scope.getId());
        return scopeDao.getScopeInstanceId();
    }

    public void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks) {

        if (BpelProcess.__log.isTraceEnabled()) {
            BpelProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("initializeEndpointReferences", new Object[] {
                    "parentScopeId", parentScopeId, "partnerLinks", partnerLinks }));
        }

        ScopeDAO parent = _dao.getScope(parentScopeId);
        for (OPartnerLink partnerLink : partnerLinks) {
            PartnerLinkDAO pdao = parent.createPartnerLink(partnerLink.getId(), partnerLink.name, partnerLink.myRoleName,
                    partnerLink.partnerRoleName);
            // If there is a myrole on the link, initialize the session id so it
            // is always
            // available for opaque correlations. The myrole session id should
            // never be changed.
            if (partnerLink.hasMyRole())
                pdao.setMySessionId(new GUID().toString());
        }
    }

    public void select(PickResponseChannel pickResponseChannel, Date timeout, boolean createInstance, Selector[] selectors)
            throws FaultException {
        if (BpelProcess.__log.isTraceEnabled())
            BpelProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("select", new Object[] { "pickResponseChannel",
                    pickResponseChannel, "timeout", timeout, "createInstance", createInstance, "selectors", selectors }));

        ProcessDAO processDao = _dao.getProcess();

        // check if this is first pick
        if (_dao.getState() == ProcessState.STATE_NEW) {
            assert createInstance;
            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_NEW);
            _dao.setState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_READY);
            sendEvent(evt);
        }

        final String pickResponseChannelStr = pickResponseChannel.export();

        List<CorrelatorDAO> correlators = new ArrayList<CorrelatorDAO>(selectors.length);
        for (Selector selector : selectors) {
            String correlatorId = BpelProcess.genCorrelatorId(selector.plinkInstance.partnerLink, selector.opName);
            if (BpelProcess.__log.isDebugEnabled()) {
                BpelProcess.__log.debug("SELECT: " + pickResponseChannel + ": USING CORRELATOR " + correlatorId);
            }
            correlators.add(processDao.getCorrelator(correlatorId));
        }

        int conflict = getORM().findConflict(selectors);
        if (conflict != -1)
            throw new FaultException(_bpelProcess.getOProcess().constants.qnConflictingReceive, selectors[conflict].toString());

        getORM().register(pickResponseChannelStr, selectors);

        // TODO - ODE-58

        // First check if we match to a new instance.
        if (_instantiatingMessageExchange != null && _dao.getState() == ProcessState.STATE_READY) {
            if (BpelProcess.__log.isDebugEnabled()) {
                BpelProcess.__log.debug("SELECT: " + pickResponseChannel + ": CHECKING for NEW INSTANCE match");
            }
            for (int i = 0; i < correlators.size(); ++i) {
                CorrelatorDAO ci = correlators.get(i);
                if (ci.equals(_dao.getInstantiatingCorrelator())) {
                    injectMyRoleMessageExchange(pickResponseChannelStr, i, _instantiatingMessageExchange);
                    if (BpelProcess.__log.isDebugEnabled()) {
                        BpelProcess.__log.debug("SELECT: " + pickResponseChannel + ": FOUND match for NEW instance mexRef="
                                + _instantiatingMessageExchange);
                    }
                    return;
                }
            }
        }

        if (timeout != null) {
            registerTimer(pickResponseChannel, timeout);
            if (BpelProcess.__log.isDebugEnabled()) {
                BpelProcess.__log.debug("SELECT: " + pickResponseChannel + "REGISTERED TIMEOUT for " + timeout);
            }
        }

        for (int i = 0; i < selectors.length; ++i) {
            CorrelatorDAO correlator = correlators.get(i);
            Selector selector = selectors[i];

            correlator.addRoute(pickResponseChannel.export(), _dao, i, selector.correlationKey);
            scheduleCorrelatorMatcher(correlator.getCorrelatorId(), selector.correlationKey);

            if (BpelProcess.__log.isDebugEnabled()) {
                BpelProcess.__log.debug("SELECT: " + pickResponseChannel + ": ADDED ROUTE " + correlator.getCorrelatorId() + ": "
                        + selector.correlationKey + " --> " + _dao.getInstanceId());
            }
        }

    }

    /**
     * @see BpelRuntimeContext#readCorrelation(org.apache.ode.bpel.runtime.CorrelationSetInstance)
     */
    public CorrelationKey readCorrelation(CorrelationSetInstance cset) {
        ScopeDAO scopeDAO = _dao.getScope(cset.scopeInstance);
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(cset.declaration.name);
        return cs.getValue();
    }

    public Node fetchVariableData(VariableInstance variable, boolean forWriting) throws FaultException {
        ScopeDAO scopeDAO = _dao.getScope(variable.scopeInstance);
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.declaration.name);

        if (dataDAO.isNull()) {
            throw new FaultException(_bpelProcess.getOProcess().constants.qnUninitializedVariable, "The variable "
                    + variable.declaration.name + " isn't properly initialized.");
        }

        return dataDAO.get();
    }

    public Node fetchVariableData(VariableInstance var, OMessageVarType.Part part, boolean forWriting) throws FaultException {
        Node container = fetchVariableData(var, forWriting);

        // If we want a specific part, we will need to navigate through the
        // message/part structure
        if (var.declaration.type instanceof OMessageVarType && part != null) {
            container = getPartData((Element) container, part);
        }
        return container;
    }

    public Element fetchPartnerRoleEndpointReferenceData(PartnerLinkInstance pLink) throws FaultException {
        PartnerLinkDAO pl = fetchPartnerLinkDAO(pLink);
        Element epr = pl.getPartnerEPR();

        if (epr == null) {
            EndpointReference e = _bpelProcess.getInitialPartnerRoleEPR(pLink.partnerLink);
            if (e != null)
                epr = e.toXML().getDocumentElement();
        }

        if (epr == null) {
            throw new FaultException(_bpelProcess.getOProcess().constants.qnUninitializedPartnerRole);
        }

        return epr;
    }

    public Element fetchMyRoleEndpointReferenceData(PartnerLinkInstance pLink) {
        return _bpelProcess.getInitialMyRoleEPR(pLink.partnerLink).toXML().getDocumentElement();
    }

    private PartnerLinkDAO fetchPartnerLinkDAO(PartnerLinkInstance pLink) {
        ScopeDAO scopeDAO = _dao.getScope(pLink.scopeInstanceId);
        return scopeDAO.getPartnerLink(pLink.partnerLink.getId());
    }

    /**
     * Evaluate a property alias query expression against a variable, returning the normalized {@link String} representation of the
     * property value.
     * 
     * @param variable
     *            variable to read
     * @param property
     *            property to read
     * @return value of property for variable, in String form
     * @throws org.apache.ode.bpel.common.FaultException
     *             in case of selection or other fault
     */
    public String readProperty(VariableInstance variable, OProcess.OProperty property) throws FaultException {
        Node varData = fetchVariableData(variable, false);

        OProcess.OPropertyAlias alias = property.getAlias(variable.declaration.type);
        String val = _bpelProcess.extractProperty((Element) varData, alias, variable.declaration.getDescription());

        if (BpelProcess.__log.isTraceEnabled()) {
            BpelProcess.__log.trace("readPropertyAlias(variable=" + variable + ", alias=" + alias + ") = " + val.toString());
        }

        return val;
    }

    public Node initializeVariable(VariableInstance variable, Node initData) {
        ScopeDAO scopeDAO = _dao.getScope(variable.scopeInstance);
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.declaration.name);

        dataDAO.set(initData);

        writeProperties(variable, initData, dataDAO);

        return dataDAO.get();
    }

    public void writeEndpointReference(PartnerLinkInstance variable, Element data) throws FaultException {
        if (__log.isDebugEnabled()) {
            __log.debug("Writing endpoint reference " + variable.partnerLink.getName() + " with value "
                    + DOMUtils.domToString(data));
        }

        PartnerLinkDAO eprDAO = fetchPartnerLinkDAO(variable);
        eprDAO.setPartnerEPR(data);
    }

    public String fetchEndpointSessionId(PartnerLinkInstance pLink, boolean isMyEPR) throws FaultException {
        PartnerLinkDAO dao = fetchPartnerLinkDAO(pLink);
        return isMyEPR ? dao.getMySessionId() : dao.getPartnerSessionId();
    }

    public Node convertEndpointReference(Element sourceNode, Node targetNode) {
        QName nodeQName;
        if (targetNode.getNodeType() == Node.TEXT_NODE) {
            nodeQName = new QName(Namespaces.XML_SCHEMA, "string");
        } else {
            // We have an element
            nodeQName = new QName(targetNode.getNamespaceURI(), targetNode.getLocalName());
        }
        return _contexts.eprContext.convertEndpoint(nodeQName, sourceNode).toXML();
    }

    public void commitChanges(VariableInstance variable, Node changes) {
        ScopeDAO scopeDAO = _dao.getScope(variable.scopeInstance);
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.declaration.name);
        dataDAO.set(changes);

        writeProperties(variable, changes, dataDAO);
    }

    public void reply(final PartnerLinkInstance plinkInstnace, final String opName, final String mexId, Element msg, QName fault)
            throws FaultException {
        String mexRef = getORM().release(plinkInstnace, opName, mexId);

        if (mexRef == null) {
            throw new FaultException(_bpelProcess.getOProcess().constants.qnMissingRequest);
        }

        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setMexId(mexId);
        evt.setOperation(opName);
        evt.setPortType(plinkInstnace.partnerLink.myRolePortType.getQName());

        // Get the "my-role" mex from the DB.
        MessageExchangeDAO myrolemex = _dao.getConnection().getMessageExchange(mexRef);

        // TODO: add some checks here/could get npe
        MessageDAO message = myrolemex.createMessage(plinkInstnace.partnerLink.getMyRoleOperation(opName).getOutput().getMessage()
                .getQName());
        message.setData(msg);

        myrolemex.setResponse(message);

        AckType ackType;
        if (fault != null) {
            ackType = AckType.FAULT;
            myrolemex.setFault(fault);
            evt.setAspect(ProcessMessageExchangeEvent.PROCESS_FAULT);
        } else {
            ackType = AckType.RESPONSE;
            evt.setAspect(ProcessMessageExchangeEvent.PROCESS_OUTPUT);
        }

        Status previousStatus = myrolemex.getStatus();
        myrolemex.setStatus(Status.ACK);
        myrolemex.setAckType(ackType);
        _bpelProcess.onMyRoleMexAck(myrolemex, previousStatus);
        sendEvent(evt);
    }


    /**
     * @see BpelRuntimeContext#writeCorrelation(org.apache.ode.bpel.runtime.CorrelationSetInstance,
     *      org.apache.ode.bpel.common.CorrelationKey)
     */
    public void writeCorrelation(CorrelationSetInstance cset, CorrelationKey correlation) {
        ScopeDAO scopeDAO = _dao.getScope(cset.scopeInstance);
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(cset.declaration.name);
        OScope.CorrelationSet csetdef = (OScope.CorrelationSet) _bpelProcess.getOProcess().getChild(correlation.getCSetId());
        QName[] propNames = new QName[csetdef.properties.size()];
        for (int m = 0; m < csetdef.properties.size(); m++) {
            OProcess.OProperty oProperty = csetdef.properties.get(m);
            propNames[m] = oProperty.name;
        }
        cs.setValue(propNames, correlation);

        CorrelationSetWriteEvent cswe = new CorrelationSetWriteEvent(cset.declaration.name, correlation);
        cswe.setScopeId(cset.scopeInstance);
        sendEvent(cswe);

    }

    /**
     * Common functionality to initialize a correlation set based on data available in a variable.
     * 
     * @param cset
     *            the correlation set instance
     * @param variable
     *            variable instance
     * 
     * @throws IllegalStateException
     *             DOCUMENTME
     */
    public void initializeCorrelation(CorrelationSetInstance cset, VariableInstance variable) throws FaultException {
        if (BpelProcess.__log.isDebugEnabled()) {
            BpelProcess.__log.debug("Initializing correlation set " + cset.declaration.name);
        }
        // if correlation set is already initialized, then skip
        if (isCorrelationInitialized(cset)) {
            // if already set, we ignore
            if (BpelProcess.__log.isDebugEnabled()) {
                BpelProcess.__log.debug("OCorrelation set " + cset + " is already set: ignoring");
            }
            return;
        }

        String[] propNames = new String[cset.declaration.properties.size()];
        String[] propValues = new String[cset.declaration.properties.size()];

        for (int i = 0; i < cset.declaration.properties.size(); ++i) {
            OProcess.OProperty property = cset.declaration.properties.get(i);
            propValues[i] = readProperty(variable, property);
            propNames[i] = property.name.toString();
        }

        CorrelationKey ckeyVal = new CorrelationKey(cset.declaration.getId(), propValues);
        writeCorrelation(cset, ckeyVal);
    }

    public ExpressionLanguageRuntimeRegistry getExpLangRuntime() {
        return _bpelProcess._expLangRuntimeRegistry;
    }

    /**
     * @see BpelRuntimeContext#terminate()
     */
    public void terminate() {
        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_TERMINATED);
        evt.setNewState(ProcessState.STATE_TERMINATED);
        sendEvent(evt);
        sendEvent(new ProcessTerminationEvent());

        cleanupOutstandingMyRoleExchanges();
    }

    public void registerTimer(TimerResponseChannel timerChannel, Date timeToFire) {
        WorkEvent we = new WorkEvent();
        we.setIID(_dao.getInstanceId());
        we.setProcessId(_bpelProcess.getPID());
        we.setChannel(timerChannel.export());
        we.setType(WorkEvent.Type.TIMER);
        _bpelProcess.scheduleWorkEvent(we, timeToFire);
    }

    private void scheduleCorrelatorMatcher(String correlatorId, CorrelationKey key) {

        WorkEvent we = new WorkEvent();
        we.setIID(_dao.getInstanceId());
        we.setProcessId(_bpelProcess.getPID());
        we.setType(WorkEvent.Type.MATCHER);
        we.setCorrelatorId(correlatorId);
        we.setCorrelationKey(key);
        _bpelProcess.scheduleWorkEvent(we, null);
    }

    public String invoke(PartnerLinkInstance partnerLink, Operation operation, Element outgoingMessage,
            InvokeResponseChannel channel) throws FaultException {

        // TODO: move a lot of this into BpelProcess

        // TODO: think we should move the dao creation into bpelprocess --mbs
        MessageExchangeDAO mexDao = _dao.getConnection().createMessageExchange(new GUID().toString(),
                MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE);
        mexDao.setStatus(MessageExchange.Status.NEW);
        mexDao.setOperation(operation.getName());
        mexDao.setPortType(partnerLink.partnerLink.partnerRolePortType.getQName());
        mexDao.setPartnerLinkModelId(partnerLink.partnerLink.getId());

        PartnerRoleChannel partnerRoleChannel = _bpelProcess.getPartnerRoleChannel(partnerLink.partnerLink);
        PartnerLinkDAO plinkDAO = fetchPartnerLinkDAO(partnerLink);

        Element partnerEPR = plinkDAO.getPartnerEPR();

        EndpointReference partnerEpr;

        if (partnerEPR == null) {
            partnerEpr = partnerRoleChannel.getInitialEndpointReference();
            // In this case, the partner link has not been initialized.
            if (partnerEpr == null)
                throw new FaultException(partnerLink.partnerLink.getOwner().constants.qnUninitializedPartnerRole);
        } else {
            partnerEpr = _contexts.eprContext.resolveEndpointReference(partnerEPR);
        }
        
        mexDao.setEPR(partnerEpr.toXML().getDocumentElement());
        mexDao.setPartnerLink(plinkDAO);
        mexDao.setProcess(_dao.getProcess());
        mexDao.setInstance(_dao);
        mexDao.setPattern((operation.getOutput() != null ? MessageExchangePattern.REQUEST_RESPONSE
                : MessageExchangePattern.REQUEST_ONLY).toString());
        mexDao.setChannel(channel == null ? null : channel.export());

        MessageDAO message = mexDao.createMessage(operation.getInput().getMessage().getQName());
        mexDao.setRequest(message);
        mexDao.setTimeout(30000);
        message.setData(outgoingMessage);
        message.setType(operation.getInput().getMessage().getQName());

        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setOperation(operation.getName());
        evt.setPortType(partnerLink.partnerLink.partnerRolePortType.getQName());
        evt.setAspect(ProcessMessageExchangeEvent.PARTNER_INPUT);
        evt.setMexId(mexDao.getMessageExchangeId());
        sendEvent(evt);


        if (__log.isDebugEnabled()) {
            __log.debug("INVOKING PARTNER: partnerLink=" + partnerLink + ", op=" + operation.getName() + " channel="
                    + channel + ")");
        }

        _bpelProcess.invokePartner(mexDao);
       

        // In case a response/fault was available right away, which will happen for BLOCKING/TRANSACTED invocations,
        // we need to inject a message on the response channel, so that the process continues.
        switch (mexDao.getStatus()) {
        case ACK:
            injectPartnerResponse(mexDao.getMessageExchangeId(), mexDao.getChannel());
            break;
        case ASYNC:
            // we'll have to wait for the response.
            break;
        default:
            throw new AssertionError("Unexpected MEX status: " + mexDao.getStatus());
        }
        
        return mexDao.getMessageExchangeId();

    }

    void execute() {
        if (!_contexts.isTransacted())
            throw new BpelEngineException("MUST RUN IN TRANSACTION!");
        if (_executed)
            throw new IllegalStateException("cannot call execute() twice!");

        long maxTime = System.currentTimeMillis() + _maxReductionTimeMs;

        // Execute the process state reductions
        boolean canReduce = true;
        while (ProcessState.canExecute(_dao.getState()) && System.currentTimeMillis() < maxTime && canReduce) {
            canReduce = _vpu.execute();
        }

        _dao.setLastActiveTime(new Date());
        if (!ProcessState.isFinished(_dao.getState())) {
            saveState();

            if (ProcessState.canExecute(_dao.getState()) && canReduce) {
                // Max time exceeded (possibly an infinite loop).
                if (__log.isDebugEnabled())
                    __log.debug("MaxTime exceeded for instance # " + _iid);

                try {
                    WorkEvent we = new WorkEvent();
                    we.setIID(_iid);
                    we.setProcessId(_bpelProcess.getPID());
                    we.setType(WorkEvent.Type.RESUME);
                    _contexts.scheduler.schedulePersistedJob(we.getDetail(), new Date());
                } catch (ContextException e) {
                    __log.error("Failed to schedule resume task.", e);
                    throw new BpelEngineException(e);
                }
            }
        }
    }

    private void saveState() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
        try {
            _soup.write(bos);
            bos.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        int newcount = _dao.getExecutionStateCounter() + 1;
        _dao.setExecutionStateCounter(newcount);
        _dao.setExecutionState(bos.toByteArray());
        _instanceWorker.setCachedState(newcount, _soup);
        
        __log.debug("CACHE SAVE: #" + newcount + " for instance " + _dao.getInstanceId());
    }

    void injectMyRoleMessageExchange(final String responsechannel, final int idx, MessageExchangeDAO mexdao) {
        // if we have a message match, this instance should be marked
        // active if it isn't already
        if (_dao.getState() == ProcessState.STATE_READY) {
            if (BpelProcess.__log.isDebugEnabled()) {
                BpelProcess.__log.debug("INPUTMSGMATCH: Changing process instance state from ready to active");
            }

            _dao.setState(ProcessState.STATE_ACTIVE);

            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_ACTIVE);
            sendEvent(evt);
        }

        getORM().associate(responsechannel, mexdao.getMessageExchangeId());

        final String mexId = mexdao.getMessageExchangeId();
        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = 3168964409165899533L;

            public void run() {
                PickResponseChannel responseChannel = importChannel(responsechannel, PickResponseChannel.class);
                responseChannel.onRequestRcvd(idx, mexId);
            }
        });
    }

    boolean injectTimerEvent(final String timerResponseChannel) {
        // In case this is a pick event, we remove routes,
        // and cancel the outstanding requests.
        _dao.getProcess().removeRoutes(timerResponseChannel, _dao);
        getORM().cancel(timerResponseChannel);

        // Ignore timer events after the process is finished.
        if (ProcessState.isFinished(_dao.getState())) {
            return false;
        }

        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = -7767141033611036745L;

            public void run() {
                TimerResponseChannel responseChannel = importChannel(timerResponseChannel, TimerResponseChannel.class);
                responseChannel.onTimeout();
            }
        });
        
        return true;
    }

    public void cancel(final TimerResponseChannel timerResponseChannel) {
        // In case this is a pick response channel, we need to cancel routes and
        // receive/reply association.
        final String id = timerResponseChannel.export();
        _dao.getProcess().removeRoutes(id, _dao);
        getORM().cancel(id);

        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = 6157913683737696396L;

            public void run() {
                TimerResponseChannel responseChannel = importChannel(id, TimerResponseChannel.class);
                responseChannel.onCancel();
            }
        });
    }

    void injectPartnerResponse(final String mexid, final String responseChannelId) {
        if (responseChannelId == null)
            throw new NullPointerException("Null responseChannelId");
        if (mexid == null)
            throw new NullPointerException("Null mexId");

        if (BpelProcess.__log.isDebugEnabled()) {
            __log.debug("<invoke> response for mexid " + mexid + " and channel " + responseChannelId);
        }
        _vpu.inject(new BpelJacobRunnable() {
            private static final long serialVersionUID = -1095444335740879981L;

            public void run() {
                ((BpelRuntimeContextImpl) getBpelRuntimeContext()).invocationResponse2(mexid, importChannel(responseChannelId,
                        InvokeResponseChannel.class));
            }
        });
    }

    /**
     * Continuation of the above.
     * 
     * @param mexid
     * @param responseChannel
     */
    private void invocationResponse2(String mexid, InvokeResponseChannel responseChannel) {
        __log.debug("Triggering response");
        MessageExchangeDAO mex = _dao.getConnection().getMessageExchange(mexid);

        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setPortType(mex.getPortType());
        evt.setMexId(mexid);
        evt.setOperation(mex.getOperation());

        MessageExchange.Status status = mex.getStatus();

        switch (mex.getAckType()) {
        case FAULT:
            evt.setAspect(ProcessMessageExchangeEvent.PARTNER_FAULT);
            responseChannel.onFault();
            break;
        case RESPONSE:
            evt.setAspect(ProcessMessageExchangeEvent.PARTNER_OUTPUT);
            responseChannel.onResponse();
            break;
        case FAILURE:
            evt.setAspect(ProcessMessageExchangeEvent.PARTNER_FAILURE);
            responseChannel.onFailure();
            break;
        default:
            __log.error("Invalid response state for mex " + mexid + ": " + status);
        }
        sendEvent(evt);
    }

    /**
     * @see BpelRuntimeContext#sendEvent(org.apache.ode.bpel.evt.ProcessInstanceEvent)
     */
    public void sendEvent(ProcessInstanceEvent event) {
        // fill in missing pieces
        event.setProcessId(_dao.getProcess().getProcessId());
        event.setProcessName(_dao.getProcess().getType());
        event.setProcessInstanceId(_dao.getInstanceId());
        _bpelProcess._debugger.onEvent(event);

        // notify the listeners
        _bpelProcess._server.fireEvent(event);

        // saving
        _bpelProcess.saveEvent(event, _dao);
    }

    /**
     * We record all values of properties of a 'MessageType' variable for efficient lookup.
     */
    private void writeProperties(VariableInstance variable, Node value, XmlDataDAO dao) {
        if (variable.declaration.type instanceof OMessageVarType) {
            for (OProcess.OProperty property : variable.declaration.getOwner().properties) {
                OProcess.OPropertyAlias alias = property.getAlias(variable.declaration.type);
                if (alias != null) {
                    try {
                        String val = _bpelProcess.extractProperty((Element) value, alias, variable.declaration.getDescription());
                        if (val != null) {
                            dao.setProperty(property.name.toString(), val);
                        }
                    } catch (FaultException e) {
                        // This will fail as we're basically trying to extract properties on all
                        // received messages for optimization purposes.
                        if (__log.isDebugEnabled())
                            __log.debug("Couldn't extract property '" + property.toString() + "' in property pre-extraction: "
                                    + e.toString());
                    }
                }
            }
        }
    }

    /**
     * Called when the process completes to clean up any outstanding message exchanges.
     * 
     */
    private void cleanupOutstandingMyRoleExchanges(FaultData optionalFaultData) {
        String[] mexRefs = getORM().releaseAll();
        for (String mexId : mexRefs) {
            MessageExchangeDAO mexDao = _dao.getConnection().getMessageExchange(mexId);
            if (mexDao != null) {
                Status status = mexDao.getStatus();
                MessageExchangePattern pattern = MessageExchange.MessageExchangePattern.valueOf(mexDao.getPattern());
                InvocationStyle istyle = mexDao.getInvocationStyle();
                if (pattern == MessageExchangePattern.REQUEST_ONLY) {
                    mexDao.setAckType(AckType.ONEWAY);
                    mexDao.setStatus(Status.COMPLETED);
                    continue;
                }

                mexDao.setAckType(AckType.FAILURE);
                mexDao.setFailureType(FailureType.NO_RESPONSE);
                if (optionalFaultData != null) {
                    mexDao.setFaultExplanation(optionalFaultData.toString());
                }
                mexDao.setFaultExplanation("Process completed without responding.");
                mexDao.setStatus(Status.ACK);
                _bpelProcess.onMyRoleMexAck(mexDao, status);
            }
        }
    }

    private OutstandingRequestManager getORM() {
        return (OutstandingRequestManager) _soup.getGlobalData();
    }

    private void cleanupOutstandingMyRoleExchanges() {
        cleanupOutstandingMyRoleExchanges(null);
    }

    public Element getPartnerResponse(String mexId) {
        return _getPartnerResponse(mexId).getData();
    }

    public Element getMyRequest(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        if (dao == null) {
            // this should not happen....
            String msg = "Engine requested non-existent message exchange: " + mexId;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }

        if (dao.getDirection() != MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE) {
            // this should not happen....
            String msg = "Engine requested my-role request for a partner-role mex: " + mexId;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }

        MessageDAO request = dao.getRequest();
        if (request == null) {
            // this also should not happen
            String msg = "Engine requested request for message exchange that did not have one: " + mexId;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }

        return request.getData();

    }

    public QName getPartnerFault(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        if (dao == null) {
            // this should not happen....
            String msg = "Engine requested non-existent message exchange: " + mexId;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }
        return dao.getFault();
    }

    public QName getPartnerResponseType(String mexId) {
        return _getPartnerResponse(mexId).getType();
    }

    public String getPartnerFaultExplanation(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        return dao != null ? dao.getFaultExplanation() : null;
    }

    private MessageDAO _getPartnerResponse(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        if (dao == null) {
            // this should not happen....
            String msg = "Engine requested non-existent message exchange: " + mexId;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }
        if (dao.getDirection() != MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE) {
            // this should not happen....
            String msg = "Engine requested partner response for a my-role mex: " + mexId;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }

        MessageDAO response;
        MessageExchange.Status status = dao.getStatus();
        if (status == Status.ACK) {
            response = dao.getResponse();
            if (response == null) {
                // this also should not happen
                String msg = "Engine requested response for message exchange that did not have one: " + mexId;
                __log.fatal(msg);
                throw new BpelEngineException(msg);
            }
        } else {
            // We should not be in any other state when requesting this.
            String msg = "Engine requested response while the message exchange " + mexId + " was in the state " + status;
            __log.fatal(msg);
            throw new BpelEngineException(msg);
        }
        return response;
    }

    public void releasePartnerMex(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        dao.release();
    }

    public Node getPartData(Element message, Part part) {
        Element partEl = DOMUtils.findChildByName((Element) message, new QName(null, part.name), false);

        // This could occur if the message does not contain the required part.
        if (partEl == null)
            return null;

        Node container = DOMUtils.getFirstChildElement(partEl);
        if (container == null)
            container = partEl.getFirstChild(); // either a text node / element
        // /
        // xsd-type-wrapper
        return container;
    }

    public Element getSourceEPR(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        String epr = dao.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_EPR);
        if (epr == null)
            return null;
        try {
            Element eepr = DOMUtils.stringToDOM(epr);
            return eepr;
        } catch (Exception ex) {
            __log.error("Invalid value for SEP property " + MessageExchange.PROPERTY_SEP_PARTNERROLE_EPR + ": " + epr);
        }

        return null;
    }

    public String getSourceSessionId(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        return dao.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
    }

    public void registerActivityForRecovery(ActivityRecoveryChannel channel, long activityId, String reason, Date dateTime,
            Element details, String[] actions, int retries) {
        if (reason == null)
            reason = "Unspecified";
        if (dateTime == null)
            dateTime = new Date();
        __log.info("ActivityRecovery: Registering activity " + activityId + ", failure reason: " + reason + " on channel "
                + channel.export());
        _dao.createActivityRecovery(channel.export(), (int) activityId, reason, dateTime, details, actions, retries);
    }

    public void unregisterActivityForRecovery(ActivityRecoveryChannel channel) {
        _dao.deleteActivityRecovery(channel.export());
    }

    public void recoverActivity(final String channel, final long activityId, final String action, final FaultData fault) {
        _vpu.inject(new JacobRunnable() {
            private static final long serialVersionUID = 3168964409165899533L;

            public void run() {
                ActivityRecoveryChannel recovery = importChannel(channel, ActivityRecoveryChannel.class);
                __log.info("ActivityRecovery: Recovering activity " + activityId + " with action " + action + " on channel "
                        + recovery);
                if (recovery != null) {
                    if ("cancel".equals(action))
                        recovery.cancel();
                    else if ("retry".equals(action))
                        recovery.retry();
                    else if ("fault".equals(action))
                        recovery.fault(fault);
                }
            }
        });
        // _dao.deleteActivityRecovery(channel);
        execute();
    }

    /**
     * Fetch the session-identifier for the partner link from the database.
     */
    public String fetchMySessionId(PartnerLinkInstance pLink) {
        String sessionId = fetchPartnerLinkDAO(pLink).getMySessionId();
        assert sessionId != null : "Session ID should always be set!";
        return sessionId;
    }

    public String fetchPartnersSessionId(PartnerLinkInstance pLink) {
        return fetchPartnerLinkDAO(pLink).getPartnerSessionId();
    }

    public void initializePartnersSessionId(PartnerLinkInstance pLink, String session) {
        if (__log.isDebugEnabled())
            __log.debug("initializing partner " + pLink + "  sessionId to " + session);
        fetchPartnerLinkDAO(pLink).setPartnerSessionId(session);

    }
}
