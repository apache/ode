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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
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
import org.apache.ode.bpel.engine.rapi.CorrelationSet;
import org.apache.ode.bpel.engine.rapi.FaultInfo;
import org.apache.ode.bpel.engine.rapi.NoSuchOperationException;
import org.apache.ode.bpel.engine.rapi.OdeRTInstance;
import org.apache.ode.bpel.engine.rapi.OdeRTInstanceContext;
import org.apache.ode.bpel.engine.rapi.PartnerLink;
import org.apache.ode.bpel.engine.rapi.PartnerLinkModel;
import org.apache.ode.bpel.engine.rapi.Selector;
import org.apache.ode.bpel.engine.rapi.UninitializedPartnerEPR;
import org.apache.ode.bpel.engine.rapi.UninitializedVariableException;
import org.apache.ode.bpel.engine.rapi.Variable;
import org.apache.ode.bpel.evt.CorrelationSetWriteEvent;
import org.apache.ode.bpel.evt.ProcessCompletionEvent;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.apache.ode.bpel.evt.ProcessMessageExchangeEvent;
import org.apache.ode.bpel.evt.ProcessTerminationEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
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
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.QNameUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * 
 * @author Maciej Szefler
 */
class OdeRTInstanceContextImpl implements OdeRTInstanceContext {

    private static final Log __log = LogFactory.getLog(OdeRTInstanceContextImpl.class);

    /** Data-access object for process instance. */
    private ProcessInstanceDAO _dao;

    /** Process Instance ID */
    private final Long _iid;

    private MessageExchangeDAO _instantiatingMessageExchange;

    private BpelInstanceWorker _instanceWorker;

    private OdeProcess _bpelProcess;

    private Contexts _contexts;

    private boolean _forceFlush;

    /** Process instance as represented by runtime. */
    final OdeRTInstance _rti;

    /** Five second maximum for continous execution. */
    private long _maxReductionTimeMs = 2000000;


    /**
     * @param iworker
     * @param instanceDAO
     */
    public OdeRTInstanceContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO instanceDAO, OdeRTInstance rti) {
        _instanceWorker = instanceWorker;
        _bpelProcess = instanceWorker._process;
        _contexts = instanceWorker._contexts;
        _dao = instanceDAO;
        _iid = instanceDAO.getInstanceId();
        _rti = rti;
        _rti.setContext(this);
    }

    /*
    OdeRTInstanceContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO dao) {
        this(instanceWorker, dao, new ExecutionQueueImpl(null));

        if (ProcessState.isFinished(dao.getState()))
            throw new BpelEngineException("Invalid process state (process is finished)!!!");

        // The following allows us to skip deserialization of the soup if our execution state in memory is the same
        // as that in the database.

        Object cachedState = instanceWorker.getCachedState(dao.getExecutionStateCounter());
        if (cachedState != null) {
            if (__log.isDebugEnabled())
                __log.debug("CACHE HIT: Using cached state #" + dao.getExecutionStateCounter() + " to resume instance "
                        + dao.getInstanceId());
            _soup = (ExecutionQueueImpl) cachedState;
            _soup.setReplacementMap(_bpelProcess.getReplacementMap());
            _vpu.setContext(_soup);
        } else {
            if (__log.isDebugEnabled())
                __log.debug("CACHE MISS: state #" + dao.getExecutionStateCounter() + " is stale; loading state to resume instance "
                        + dao.getInstanceId() + " from database ");
            byte[] daoState = dao.getExecutionState();
            ByteArrayInputStream iis = new ByteArrayInputStream(daoState);
            try {
                _soup.read(iis);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    OdeRTInstanceContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO dao, ExecutionQueueImpl soup) {
        _soup = soup;
        _soup.setReplacementMap(_bpelProcess.getReplacementMap());
        _vpu.setContext(_soup);
        if (OdeProcess.__log.isDebugEnabled()) {
            __log.debug("BpelRuntimeContextImpl created for instance " + _iid + ". INDEXED STATE=" + _soup.getIndex());
        }
    }
    
    */

    public void executeCreateInstance(MessageExchangeDAO instantiatingMessageExchange) {
        if (instantiatingMessageExchange == null)
            throw new NullPointerException();
        _instantiatingMessageExchange = instantiatingMessageExchange;
        _rti.onCreateInstance(instantiatingMessageExchange.getMessageExchangeId());
        execute();
    }

    public String toString() {
        return "{OdeRTInstanceContextImpl PID=" + _bpelProcess.getPID() + ", IID=" + _iid + "}";
    }

    public Long getPid() {
        return _iid;
    }

    public long genId() {
        return _dao.genMonotonic();
    }

    /**
     * @see OdeRTInstanceContext#isCorrelationInitialized(org.apache.ode.bpel.jrep.v2.CorrelationSetInstance)
     */
    public boolean isCorrelationInitialized(CorrelationSet correlationSet) {
        ScopeDAO scopeDAO = _dao.getScope(correlationSet.getScopeId());
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(correlationSet.getName());
        return cs.getValue() != null;
    }

    /**
     * @see OdeRTInstanceContext#isVariableInitialized(org.apache.ode.bpel.jrep.v2.VariableInstance)
     */
    public boolean isVariableInitialized(Variable var) {
        ScopeDAO scopeDAO = _dao.getScope(var.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(var.getName());
        return !dataDAO.isNull();
    }

    public boolean isPartnerRoleEndpointInitialized(PartnerLink pLink) {
        PartnerLinkDAO spl = fetchPartnerLinkDAO(pLink);

        return spl.getPartnerEPR() != null || _bpelProcess.getInitialPartnerRoleEPR(pLink.getModel()) != null;
    }

    /**
     * @see OdeRTInstanceContext#completedFault(org.apache.ode.bpel.jrep.v2.channels.FaultData)
     */
    public void completedFault(FaultInfo faultData) {
        if (OdeProcess.__log.isDebugEnabled()) {
            OdeProcess.__log.debug("ProcessImpl completed with fault '" + faultData.getFaultName() + "'");
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

    }

    /**
     * @see OdeRTInstanceContext#completedOk()
     */
    public void completedOk() {
        if (OdeProcess.__log.isDebugEnabled()) {
            OdeProcess.__log.debug("ProcessImpl " + _bpelProcess.getPID() + " completed OK.");
        }

        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_COMPLETED_OK);
        evt.setNewState(ProcessState.STATE_COMPLETED_OK);
        sendEvent(evt);

        sendEvent(new ProcessCompletionEvent(null));
        _dao.finishCompletion();

    }

    /**
     * @see OdeRTInstanceContext#createScopeInstance(Long, org.apache.ode.bpel.jrep.v2.OScope)
     */
    public Long createScopeInstance(Long parentScopeId, String name, int modelId) {
        if (OdeProcess.__log.isTraceEnabled()) {
            OdeProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("createScopeInstance", new Object[] { "parentScopeId",
                    parentScopeId, "name", name }));
        }

        ScopeDAO parent = null;

        if (parentScopeId != null) {
            parent = _dao.getScope(parentScopeId);
        }

        ScopeDAO scopeDao = _dao.createScope(parent, name, modelId);
        return scopeDao.getScopeInstanceId();
    }

    public void initializePartnerLinks(Long parentScopeId, Collection<? extends PartnerLinkModel> partnerLinks) {

        if (OdeProcess.__log.isTraceEnabled()) {
            OdeProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("initializeEndpointReferences", new Object[] {
                    "parentScopeId", parentScopeId, "partnerLinks", partnerLinks }));
        }

        ScopeDAO parent = _dao.getScope(parentScopeId);
        for (PartnerLinkModel partnerLink : partnerLinks) {
            PartnerLinkDAO pdao = parent.createPartnerLink(partnerLink.getId(), partnerLink.getName(), partnerLink.getMyRoleName(),
                    partnerLink.getPartnerRoleName());
            // If there is a myrole on the link, initialize the session id so it
            // is always
            // available for opaque correlations. The myrole session id should
            // never be changed.
            if (partnerLink.hasMyRole())
                pdao.setMySessionId(new GUID().toString());
        }
    }

    public void select(String selectId, Date timeout, org.apache.ode.bpel.engine.rapi.Selector[] selectors) {
        if (OdeProcess.__log.isTraceEnabled())
            OdeProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("select", new Object[] { "selectId",
                    selectId, "timeout", timeout, "selectors", selectors }));


        ProcessDAO processDao = _dao.getProcess();

        // check if this is first pick
        if (_dao.getState() == ProcessState.STATE_NEW) {            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_NEW);
            _dao.setState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_READY);
            sendEvent(evt);
        }

        List<CorrelatorDAO> correlators = new ArrayList<CorrelatorDAO>(selectors.length);
        for (org.apache.ode.bpel.engine.rapi.Selector selector : selectors) {
            String correlatorId = OdeProcess.genCorrelatorId(selector.getPartnerLink().getModel(), selector.getOperation());
            if (OdeProcess.__log.isDebugEnabled()) {
                OdeProcess.__log.debug("SELECT: " + selectId + ": USING CORRELATOR " + correlatorId);
            }
            correlators.add(processDao.getCorrelator(correlatorId));
        }


        // TODO - ODE-58

        // First check if we match to a new instance.
        if (_instantiatingMessageExchange != null && _dao.getState() == ProcessState.STATE_READY) {
            if (OdeProcess.__log.isDebugEnabled()) {
                OdeProcess.__log.debug("SELECT: " + selectId + ": CHECKING for NEW INSTANCE match");
            }
            for (int i = 0; i < correlators.size(); ++i) {
                CorrelatorDAO ci = correlators.get(i);
                if (ci.equals(_dao.getInstantiatingCorrelator())) {
                    injectMyRoleMessageExchange(selectId, i, _instantiatingMessageExchange);
                    if (OdeProcess.__log.isDebugEnabled()) {
                        OdeProcess.__log.debug("SELECT: " + selectId + ": FOUND match for NEW instance mexRef="
                                + _instantiatingMessageExchange);
                    }
                    return;
                }
            }
        }

        if (timeout != null) {
            registerTimer(selectId, timeout);
            if (OdeProcess.__log.isDebugEnabled()) {
                OdeProcess.__log.debug("SELECT: " + selectId + "REGISTERED TIMEOUT for " + timeout);
            }
        }

        for (int i = 0; i < selectors.length; ++i) {
            CorrelatorDAO correlator = correlators.get(i);
            Selector selector = selectors[i];

            correlator.addRoute(selectId, _dao, i, selector.getCorrelationKey());
            scheduleCorrelatorMatcher(correlator.getCorrelatorId(), selector.getCorrelationKey());

            if (OdeProcess.__log.isDebugEnabled()) {
                OdeProcess.__log.debug("SELECT: " + selectId + ": ADDED ROUTE " + correlator.getCorrelatorId() + ": "
                        + selector.getCorrelationKey() + " --> " + _dao.getInstanceId());
            }
        }

    }

    /**
     * @see OdeRTInstanceContext#readCorrelation(org.apache.ode.bpel.jrep.v2.CorrelationSetInstance)
     */
    public CorrelationKey readCorrelation(CorrelationSet cset) {
        ScopeDAO scopeDAO = _dao.getScope(cset.getScopeId());
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(cset.getName());
        return cs.getValue();
    }

    public Node fetchVariableData(Variable variable, boolean forWriting) {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());

        if (dataDAO.isNull()) {
            return null;
        }

        return dataDAO.get();

    }

    public Element fetchPartnerRoleEndpointReferenceData(PartnerLink pLink) {
        PartnerLinkDAO pl = fetchPartnerLinkDAO(pLink);
        Element epr = pl.getPartnerEPR();

        if (epr == null) {
            EndpointReference e = _bpelProcess.getInitialPartnerRoleEPR(pLink.getModel());
            if (e != null)
                epr = e.toXML().getDocumentElement();
        }

        return epr;
    }

    public Element fetchMyRoleEndpointReferenceData(PartnerLink pLink) {
        return _bpelProcess.getInitialMyRoleEPR(pLink.getModel()).toXML().getDocumentElement();
    }

    private PartnerLinkDAO fetchPartnerLinkDAO(PartnerLink pLink) {
        ScopeDAO scopeDAO = _dao.getScope(pLink.getScopeId());
        return scopeDAO.getPartnerLink(pLink.getModel().getId());
    }

    public Node initializeVariable(Variable variable, Node initData) {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());

        dataDAO.set(initData);
        return dataDAO.get();
    }

    public void writeEndpointReference(PartnerLink partnerLink, Element data) {
        if (__log.isDebugEnabled()) {
            __log.debug("Writing endpoint reference " + partnerLink.getName() + " with value "
                    + DOMUtils.domToString(data));
        }

        PartnerLinkDAO eprDAO = fetchPartnerLinkDAO(partnerLink);
        eprDAO.setPartnerEPR(data);
    }

    public String fetchEndpointSessionId(PartnerLink pLink, boolean isMyEPR) {
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

    public void commitChanges(Variable variable, Node changes) {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());
        dataDAO.set(changes);
    }

    public void reply(String mexId, final PartnerLink plink, final String opName, Element msg, QName fault) throws NoSuchOperationException {
        
        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setMexId(mexId);
        evt.setOperation(opName);
        evt.setPortType(plink.getModel().getMyRolePortType().getQName());

        // Get the "my-role" mex from the DB.
        MessageExchangeDAO myrolemex = _dao.getConnection().getMessageExchange(mexId);

        Operation operation = plink.getModel().getMyRoleOperation(opName);
        if (operation == null || operation.getOutput() == null) {
            throw new NoSuchOperationException();
        }

        // TODO what if msg==null? i.e. for a reply-with-fault.

        MessageDAO message = myrolemex.createMessage(operation.getOutput().getMessage().getQName());
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

    public void writeCorrelation(CorrelationSet cset, QName[] propNames, CorrelationKey correlation) {
        ScopeDAO scopeDAO = _dao.getScope(cset.getScopeId());
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(cset.getName());
        cs.setValue(propNames, correlation);

        CorrelationSetWriteEvent cswe = new CorrelationSetWriteEvent(cset.getName(), correlation);
        cswe.setScopeId(cset.getScopeId());
        sendEvent(cswe);

    }

        /**
     * @see OdeRTInstanceContext#terminate()
     */
    public void terminate() {
        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_TERMINATED);
        evt.setNewState(ProcessState.STATE_TERMINATED);
        sendEvent(evt);
        sendEvent(new ProcessTerminationEvent());

        _dao.finishCompletion();
    }

    public void registerTimer(String timerId, Date timeToFire) {
        WorkEvent we = new WorkEvent();
        we.setIID(_dao.getInstanceId());
        we.setProcessId(_bpelProcess.getPID());
        we.setChannel(timerId);
        we.setType(WorkEvent.Type.TIMER);
        _bpelProcess.scheduleWorkEvent(we, timeToFire);
    }

    public boolean cancelSelect(String selectId) {
        _dao.getProcess().removeRoutes(selectId, _dao);
        return true;
        
    }
    
    public boolean cancelTimer(String timerId) {
        // No way to cancel these now.
        return true;        
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

    public String invoke(String requestId, PartnerLink partnerLink, Operation operation, Element outgoingMessage) throws UninitializedPartnerEPR {

        // TODO: move a lot of this into BpelProcess

        // TODO: think we should move the dao creation into bpelprocess --mbs
        MessageExchangeDAO mexDao = _dao.getConnection().createMessageExchange(new GUID().toString(),
                MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE);
        mexDao.setStatus(MessageExchange.Status.REQ);
        mexDao.setOperation(operation.getName());
        mexDao.setPortType(partnerLink.getModel().getPartnerRolePortType().getQName());
        mexDao.setPartnerLinkModelId(partnerLink.getModel().getId());

        PartnerRoleChannel partnerRoleChannel = _bpelProcess.getPartnerRoleChannel(partnerLink.getModel());
        PartnerLinkDAO plinkDAO = fetchPartnerLinkDAO(partnerLink);

        Element partnerEPR = plinkDAO.getPartnerEPR();

        EndpointReference partnerEpr;

        if (partnerEPR == null) {
            partnerEpr = partnerRoleChannel.getInitialEndpointReference();
            // In this case, the partner link has not been initialized.
            if (partnerEpr == null)
                throw new UninitializedPartnerEPR();
        } else {
            partnerEpr = _contexts.eprContext.resolveEndpointReference(partnerEPR);
        }

        mexDao.setEPR(partnerEpr.toXML().getDocumentElement());
        mexDao.setPartnerLink(plinkDAO);
        mexDao.setProcess(_dao.getProcess());
        mexDao.setInstance(_dao);
        mexDao.setPattern((operation.getOutput() != null ? MessageExchangePattern.REQUEST_RESPONSE
                : MessageExchangePattern.REQUEST_ONLY));
        mexDao.setChannel(requestId);

        MessageDAO message = mexDao.createMessage(operation.getInput().getMessage().getQName());
        mexDao.setRequest(message);
        mexDao.setTimeout(30000);
        message.setData(outgoingMessage);
        message.setType(operation.getInput().getMessage().getQName());

        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setOperation(operation.getName());
        evt.setPortType(partnerLink.getModel().getPartnerRolePortType().getQName());
        evt.setAspect(ProcessMessageExchangeEvent.PARTNER_INPUT);
        evt.setMexId(mexDao.getMessageExchangeId());
        sendEvent(evt);

        if (__log.isDebugEnabled()) {
            __log.debug("INVOKING PARTNER: partnerLink=" + partnerLink + ", op=" + operation.getName() + " requestId=" + requestId
                    + ")");
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
        
        long maxTime = System.currentTimeMillis() + _maxReductionTimeMs;

        // Execute the process state reductions
        boolean canReduce = true;
        while (ProcessState.canExecute(_dao.getState()) && System.currentTimeMillis() < maxTime && canReduce && !_forceFlush) {
            canReduce = _rti.execute();
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Object cachedState;
        try {
            cachedState = _rti.saveState(bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int newcount = _dao.getExecutionStateCounter() + 1;
        _dao.setExecutionStateCounter(newcount);
        _dao.setExecutionState(bos.toByteArray());
        _instanceWorker.setCachedState(newcount, cachedState);

        __log.debug("CACHE SAVE: #" + newcount + " for instance " + _dao.getInstanceId());
    }

    void injectMyRoleMessageExchange(final String selectId, final int idx, MessageExchangeDAO mexdao) {
        // if we have a message match, this instance should be marked
        // active if it isn't already
        if (_dao.getState() == ProcessState.STATE_READY) {
            if (OdeProcess.__log.isDebugEnabled()) {
                OdeProcess.__log.debug("INPUTMSGMATCH: Changing process instance state from ready to active");
            }

            _dao.setState(ProcessState.STATE_ACTIVE);

            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_ACTIVE);
            sendEvent(evt);
        }
        
        _rti.onSelectEvent(selectId, mexdao.getMessageExchangeId(), idx);

    }

    boolean injectTimerEvent(final String timerResponseChannel) {
        // In case this is a pick event, we remove routes,
        // and cancel the outstanding requests.
        _dao.getProcess().removeRoutes(timerResponseChannel, _dao);
        
        // Ignore timer events after the process is finished.
        if (ProcessState.isFinished(_dao.getState())) {
            return false;
        }
        
        _rti.onTimerEvent(timerResponseChannel);
        return true;
    }

    
    void injectPartnerResponse(final String mexid, final String invokeId) {
        if (invokeId == null)
            throw new NullPointerException("Null responseChannelId");
        if (mexid == null)
            throw new NullPointerException("Null mexId");

        if (OdeProcess.__log.isDebugEnabled()) {
            __log.debug("<invoke> response for mexid " + mexid + " and channel " + invokeId);
        }
        
        MessageExchangeDAO mex = _dao.getConnection().getMessageExchange(mexid);

        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setPortType(mex.getPortType());
        evt.setMexId(mexid);
        evt.setOperation(mex.getOperation());

        MessageExchange.Status status = mex.getStatus();

        OdeRTInstance.InvokeResponseType  irt;
        switch (mex.getAckType()) {
        case FAULT:
            irt = OdeRTInstance.InvokeResponseType.FAULT; 
            evt.setAspect(ProcessMessageExchangeEvent.PARTNER_FAULT);
            break;
        case RESPONSE:
            irt = OdeRTInstance.InvokeResponseType.REPLY; 
            evt.setAspect(ProcessMessageExchangeEvent.PARTNER_OUTPUT);
            break;
        case FAILURE:
            irt = OdeRTInstance.InvokeResponseType.FAILURE; 
            evt.setAspect(ProcessMessageExchangeEvent.PARTNER_FAILURE);
            break;
        default:
            String msg = "Invalid response state for mex " + mexid + ": " + status; 
            __log.error(msg);
            return;
        }
        sendEvent(evt);

        _rti.onInvokeResponse(invokeId, irt, mexid);
    }

    /**
     * @see OdeRTInstanceContext#sendEvent(org.apache.ode.bpel.evt.ProcessInstanceEvent)
     */
    public void sendEvent(ProcessInstanceEvent event) {
        // fill in missing pieces
        event.setProcessId(_dao.getProcess().getProcessId());
        event.setProcessName(_dao.getProcess().getType());
        event.setProcessInstanceId(_dao.getInstanceId());
        _bpelProcess._debugger.onEvent(event);

        // filter events
        List<String> scopeNames = null;
        if (event instanceof ScopeEvent) {
            scopeNames = ((ScopeEvent) event).getParentScopesNames();
        }

        if (_bpelProcess._pconf.isEventEnabled(scopeNames, event.getType())) {
            // notify the listeners
            _bpelProcess._server.fireEvent(event);

            // saving
            _bpelProcess.saveEvent(event, _dao);
        }
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

    public void registerActivityForRecovery(String channel, long activityId, String reason, Date dateTime,
            Element details, String[] actions, int retries) {
        if (reason == null)
            reason = "Unspecified";
        if (dateTime == null)
            dateTime = new Date();
        __log.info("ActivityRecovery: Registering activity " + activityId + ", failure reason: " + reason + " on channel "
                + channel);
        _dao.createActivityRecovery(channel, (int) activityId, reason, dateTime, details, actions, retries);
    }

    public void unregisterActivityForRecovery(String channel) {
        _dao.deleteActivityRecovery(channel);
    }


    /**
     * Fetch the session-identifier for the partner link from the database.
     */
    public String fetchMySessionId(PartnerLink pLink) {
        String sessionId = fetchPartnerLinkDAO(pLink).getMySessionId();
        assert sessionId != null : "Session ID should always be set!";
        return sessionId;
    }

    public String fetchPartnersSessionId(PartnerLink pLink) {
        return fetchPartnerLinkDAO(pLink).getPartnerSessionId();
    }

    public void initializePartnersSessionId(PartnerLink pLink, String session) {
        if (__log.isDebugEnabled())
            __log.debug("initializing partner " + pLink + "  sessionId to " + session);
        fetchPartnerLinkDAO(pLink).setPartnerSessionId(session);

    }

    public void forceFlush() {
        _forceFlush = true;
    }

  
    /*
     * Note, this method simply reads properties from the database and knows nothing about property aliases and the like. 
     * (non-Javadoc)
     * @see org.apache.ode.bpel.engine.rapi.VariableContext#readVariableProperty(org.apache.ode.bpel.engine.rapi.Variable, javax.xml.namespace.QName)
     */
    public String readVariableProperty(Variable variable, QName property) throws UninitializedVariableException {

        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());

        if (dataDAO.isNull()) {
            throw new UninitializedVariableException();
        }

        String val = dataDAO.getProperty(QNameUtils.fromQName(property));
        
        if (OdeProcess.__log.isTraceEnabled()) {
            OdeProcess.__log.trace("readVariableProperty(variable=" + variable + ", property=" + property + ") = " + val);
            
        }

        return val;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ode.bpel.engine.rapi.VariableContext#writeVariableProperty(org.apache.ode.bpel.engine.rapi.Variable,
     *      javax.xml.namespace.QName, java.lang.String)
     */
    public void writeVariableProperty(Variable variable, QName property, String value) throws UninitializedVariableException {

        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());

        if (dataDAO.isNull()) {
            throw new UninitializedVariableException();
        }

        dataDAO.setProperty(QNameUtils.fromQName(property), value);
        
        if (OdeProcess.__log.isTraceEnabled()) {
            OdeProcess.__log.trace("writeVariableProperty(variable=" + variable + ", property=" + property + ") = " + value);
            
        }

    }

    /**
     * Recover an activity. Called from the Process/Instance management API. Not exposed to runtime. 
     * @param channel
     * @param activityId
     * @param action
     * @param fault
     */
    void recoverActivity(final String channel, final long activityId, final String action, final FaultInfo fault) {
        _rti.recoverActivity(channel, activityId, action, fault);
        // _dao.deleteActivityRecovery(channel);
        execute();
    }

    /* (non-Javadoc)
     * @see org.apache.ode.bpel.engine.rapi.OdeRTInstanceContext#noreply(java.lang.String, org.apache.ode.bpel.engine.rapi.FaultInfo)
     */
    public void noreply(String mexId, FaultInfo optionalFaultData) {
        MessageExchangeDAO mexDao = _dao.getConnection().getMessageExchange(mexId);
        if (mexDao != null) {
            Status status = mexDao.getStatus();
            InvocationStyle istyle = mexDao.getInvocationStyle();
            if (mexDao.getPattern() == MessageExchangePattern.REQUEST_ONLY) {
                mexDao.setAckType(AckType.ONEWAY);
                mexDao.setStatus(Status.COMPLETED);
                return;
            }

            mexDao.setAckType(AckType.FAILURE);
            mexDao.setFailureType(FailureType.NO_RESPONSE);
            if (optionalFaultData != null) {
                mexDao.setFaultExplanation(optionalFaultData.toString());
            }
            mexDao.setFaultExplanation("Process did not respond.");
            mexDao.setStatus(Status.ACK);
            _bpelProcess.onMyRoleMexAck(mexDao, status);
        }
    }


}
