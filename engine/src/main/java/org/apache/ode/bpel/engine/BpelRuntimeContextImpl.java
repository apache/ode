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
import java.net.URI;
import java.util.*;

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
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.MessageExchange.AckType;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.utils.*;

import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evar.ExternalVariableModule.Value;
import org.apache.ode.bpel.rapi.*;
import org.apache.ode.bpel.rapi.Resource;
import org.apache.ode.bpel.memdao.ProcessInstanceDaoImpl;

import org.w3c.dom.*;

class BpelRuntimeContextImpl implements OdeRTInstanceContext {

    private static final Log __log = LogFactory.getLog(BpelRuntimeContextImpl.class);

    /** Data-access object for process instance. */
    private ProcessInstanceDAO _dao;

    /** Process Instance ID */
    private final Long _iid;

    private MessageExchangeDAO _instantiatingMessageExchange;

    private BpelInstanceWorker _instanceWorker;

    private ODEProcess _bpelProcess;

    private Contexts _contexts;

    private boolean _forceFlush;

    /** Process instance as represented by runtime. */
    final OdeRTInstance _rti;

    /** Five second maximum for continous execution. */
    private long _maxReductionTimeMs = 2000000;

    public BpelRuntimeContextImpl(BpelInstanceWorker instanceWorker, ProcessInstanceDAO instanceDAO, OdeRTInstance rti) {
        _instanceWorker = instanceWorker;
        _bpelProcess = instanceWorker._process;
        _contexts = instanceWorker._contexts;
        _dao = instanceDAO;
        _iid = instanceDAO.getInstanceId();
        _rti = rti;
        _rti.setContext(this);
    }

    public String toString() {
        return "{BpelRuntimeCtx PID=" + _bpelProcess.getPID() + ", IID=" + _iid + "}";
    }

    public Long getInstanceId() {
        return _iid;
    }

    public long genId() {
        return _dao.genMonotonic();
    }

    public boolean isCorrelationInitialized(CorrelationSet correlationSet) {
        ScopeDAO scopeDAO = _dao.getScope(correlationSet.getScopeId());
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(correlationSet.getName());

        return cs.getValue() != null;
    }

    public boolean isVariableInitialized(Variable var) {
        ScopeDAO scopeDAO = _dao.getScope(var.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(var.getName());
        return !dataDAO.isNull();
    }

    public Node initializeVariable(Variable variable, Node initData) {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());

        dataDAO.set(initData);
        return dataDAO.get();
    }

    public boolean isPartnerRoleEndpointInitialized(PartnerLink pLink) {
        PartnerLinkDAO spl = fetchPartnerLinkDAO(pLink);

        return spl.getPartnerEPR() != null || ((ODEWSProcess)_bpelProcess).getInitialPartnerRoleEPR(pLink.getModel()) != null;
    }

    public void completedFault(FaultInfo faultData) {
        if (ODEProcess.__log.isDebugEnabled()) {
            ODEProcess.__log.debug("ProcessImpl completed with fault '" + faultData.getFaultName() + "'");
        }

        _dao.setFault(faultData.getFaultName(), faultData.getExplanation(), faultData.getFaultLineNo(),
                faultData.getActivityId(), faultData.getFaultMessage());

        cleanupResourceRoutes();

        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_COMPLETED_WITH_FAULT);
        evt.setNewState(ProcessState.STATE_COMPLETED_WITH_FAULT);
        sendEvent(evt);

        sendEvent(new ProcessCompletionEvent(faultData.getFaultName()));
        _dao.finishCompletion();
    }

    public void completedOk() {
        if (ODEProcess.__log.isDebugEnabled()) {
            ODEProcess.__log.debug("ProcessImpl " + _bpelProcess.getPID() + " completed OK.");
        }

        cleanupResourceRoutes();

        // send event
        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
        evt.setOldState(_dao.getState());
        _dao.setState(ProcessState.STATE_COMPLETED_OK);
        evt.setNewState(ProcessState.STATE_COMPLETED_OK);
        sendEvent(evt);

        sendEvent(new ProcessCompletionEvent(null));
        _dao.finishCompletion();
    }

    public Long createScopeInstance(Long parentScopeId, String name, int modelId) {
        if (ODEProcess.__log.isTraceEnabled())
            ODEProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("createScopeInstance", new Object[] { "parentScopeId",
                    parentScopeId, "name", name }));

        ScopeDAO parent = null;

        if (parentScopeId != null) {
            parent = _dao.getScope(parentScopeId);
        }

        ScopeDAO scopeDao = _dao.createScope(parent, name, modelId);
        return scopeDao.getScopeInstanceId();
    }

    public void initializePartnerLinks(Long parentScopeId, Collection<? extends PartnerLinkModel> partnerLinks) {
        if (ODEProcess.__log.isTraceEnabled())
            ODEProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("initializeEndpointReferences", new Object[] {
                    "parentScopeId", parentScopeId, "partnerLinks", partnerLinks }));

        ScopeDAO parent = _dao.getScope(parentScopeId);
        for (PartnerLinkModel partnerLink : partnerLinks) {
            PartnerLinkDAO pdao = parent.createPartnerLink(partnerLink.getId(), partnerLink.getName(),
                    partnerLink.getMyRoleName(), partnerLink.getPartnerRoleName());
            // If there is a myrole on the link, initialize the session id so it is always
            // available for opaque correlations. The myrole session id should never be changed.
            if (partnerLink.hasMyRole()) pdao.setMySessionId(new GUID().toString());
        }
    }

    public void initializeResource(Long parentScopeId, ResourceModel resource, String url) {
        ScopeDAO parent = _dao.getScope(parentScopeId);
        // Storing the resource as a variable
        XmlDataDAO resourceData = parent.getVariable(resource.getName());
        Document doc = DOMUtils.newDocument();
        resourceData.set(doc.createTextNode(url));
    }

    public void initializeInstantiatingUrl(String url) {
        _dao.setInstantiatingUrl(url);
        _instantiatingMessageExchange.setResource(url + "~POST");
    }

    public String getInstantiatingUrl() {
        return _dao.getInstantiatingUrl();
    }

    public String readResource(Long parentScopeId, ResourceModel resource) {
        ScopeDAO parent = _dao.getScope(parentScopeId);
        XmlDataDAO resourceData = parent.getVariable(resource.getName());
        Node resourceNode = resourceData.get();
        if (resourceData.isNull()) return null;
        else return ((Text)resourceNode).getWholeText();
    }

    public void select(String selectChannelId, Date timeout, Selector[] selectors) {
        if (ODEProcess.__log.isTraceEnabled())
            ODEProcess.__log.trace(ObjectPrinter.stringifyMethodEnter("select", new Object[] { "pickResponseChannel",
                    selectChannelId, "timeout", timeout, "selectors", selectors }));

        ProcessDAO processDao = _dao.getProcess();

        // check if this is first pick
        if (_dao.getState() == ProcessState.STATE_NEW) {
            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_NEW);
            _dao.setState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_READY);
            sendEvent(evt);
        }

        List<CorrelatorDAO> correlators = new ArrayList<CorrelatorDAO>(selectors.length);
        for (Selector selector : selectors) {
            String correlatorId = ODEProcess.genCorrelatorId(selector.getPartnerLink().getModel(), selector.getOperation());
            if (ODEProcess.__log.isDebugEnabled()) {
                ODEProcess.__log.debug("SELECT: " + selectChannelId + ": USING CORRELATOR " + correlatorId);
            }
            correlators.add(processDao.getCorrelator(correlatorId));
        }

        // First check if we match to a new instance.
        if (_instantiatingMessageExchange != null && _dao.getState() == ProcessState.STATE_READY) {
            if (ODEProcess.__log.isDebugEnabled()) {
                ODEProcess.__log.debug("SELECT: " + selectChannelId + ": CHECKING for NEW INSTANCE match");
            }
            for (int i = 0; i < correlators.size(); ++i) {
                CorrelatorDAO ci = correlators.get(i);
                if (ci.equals(_dao.getInstantiatingCorrelator())) {
                    injectMyRoleMessageExchange(selectChannelId, i, _instantiatingMessageExchange);
                    if (ODEProcess.__log.isDebugEnabled()) {
                        ODEProcess.__log.debug("SELECT: " + selectChannelId + ": FOUND match for NEW instance mexRef="
                                + _instantiatingMessageExchange);
                    }
                    return;
                }
            }
        }

        if (timeout != null) {
            registerTimer(selectChannelId, timeout);
            if (ODEProcess.__log.isDebugEnabled()) {
                ODEProcess.__log.debug("SELECT: " + selectChannelId + "REGISTERED TIMEOUT for " + timeout);
            }
        }

        for (int i = 0; i < selectors.length; ++i) {
            CorrelatorDAO correlator = correlators.get(i);
            Selector selector = selectors[i];

            correlator.addRoute(selectChannelId, _dao, i, selector.getCorrelationKey());
            scheduleCorrelatorMatcher(correlator.getCorrelatorId(), selector.getCorrelationKey());

            if (ODEProcess.__log.isDebugEnabled()) {
                ODEProcess.__log.debug("SELECT: " + selectChannelId + ": ADDED ROUTE " + correlator.getCorrelatorId() + ": "
                        + selector.getCorrelationKey() + " --> " + _dao.getInstanceId());
            }
        }
    }

    public void checkResourceRoute(Resource resourceInstance, String pickResponseChannel, int selectorIdx) {
        // check if this is first pick
        if (_dao.getState() == ProcessState.STATE_NEW) {
            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_NEW);
            _dao.setState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_READY);
            sendEvent(evt);
        }

        String method = resourceInstance.getModel().getMethod();
        if (_instantiatingMessageExchange != null && method.equals("POST") && _dao.getState() == ProcessState.STATE_READY)
            injectMyRoleMessageExchange(pickResponseChannel, selectorIdx, _instantiatingMessageExchange);
        else {
            String url = readResource(resourceInstance.getScopeInstanceId(), resourceInstance.getModel());
            _dao.createResourceRoute(url, method, pickResponseChannel, selectorIdx);
            org.apache.ode.bpel.iapi.Resource res = new org.apache.ode.bpel.iapi.Resource(url, "application/xml", method);
            _bpelProcess._contexts.bindingContext.activateProvidedResource(res);
        }

        // TODO schedule a matcher to see if the message arrived already
    }

    private void cleanupResourceRoutes() {
        Set<String> routes = _dao.getAllResourceRoutes();
        for (String route : routes) {
            String[] resArr = route.split("~");
            org.apache.ode.bpel.iapi.Resource res = new org.apache.ode.bpel.iapi.Resource(resArr[0], "application/xml", resArr[1]);
            _bpelProcess._contexts.bindingContext.deactivateProvidedResource(res);
        }
    }

    public CorrelationKey readCorrelation(CorrelationSet cset) {
        ScopeDAO scopeDAO = _dao.getScope(cset.getScopeId());
        CorrelationSetDAO cs = scopeDAO.getCorrelationSet(cset.getName());
        return cs.getValue();
    }
  

    public Element fetchPartnerRoleEndpointReferenceData(PartnerLink pLink) {
        PartnerLinkDAO pl = fetchPartnerLinkDAO(pLink);
        Element epr = pl.getPartnerEPR();
        if (epr == null) {
            EndpointReference e = ((ODEWSProcess)_bpelProcess).getInitialPartnerRoleEPR(pLink.getModel());
            if (e != null)
                epr = e.toXML().getDocumentElement();
        }

        return epr;
    }

    public Element fetchMyRoleEndpointReferenceData(PartnerLink pLink) {
        return ((ODEWSProcess)_bpelProcess).getInitialMyRoleEPR(pLink.getModel()).toXML().getDocumentElement();
    }

    private PartnerLinkDAO fetchPartnerLinkDAO(PartnerLink pLink) {
        ScopeDAO scopeDAO = _dao.getScope(pLink.getScopeId());
        return scopeDAO.getPartnerLink(pLink.getModel().getId());
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
    public String readVariableProperty(Variable variable, QName property) throws UninitializedVariableException {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());
        if (dataDAO.isNull()) throw new UninitializedVariableException();
        return dataDAO.getProperty(QNameUtils.fromQName(property));
    }

    public Node fetchVariableData(Variable variable, boolean forWriting) {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());
        if (dataDAO.isNull()) return null;
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

    public String fetchEndpointSessionId(PartnerLink pLink, boolean isMyEPR) throws FaultException {
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

    public void writeVariableProperty(Variable variable, QName property, String value) throws UninitializedVariableException {
        ScopeDAO scopeDAO = _dao.getScope(variable.getScopeId());
        XmlDataDAO dataDAO = scopeDAO.getVariable(variable.getName());
        if (dataDAO.isNull()) throw new UninitializedVariableException();
        dataDAO.setProperty(QNameUtils.fromQName(property), value);
    }

    public void reply(String mexId, final PartnerLink plink, final String opName, Element msg, QName fault)
            throws NoSuchOperationException {
        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setMexId(mexId);
        evt.setOperation(opName);
        evt.setPortType(plink.getModel().getMyRolePortType().getQName());

        // Get the "my-role" mex from the DB.
        MessageExchangeDAO myrolemex = _dao.getConnection().getMessageExchange(mexId);

        Operation operation = plink.getModel().getMyRoleOperation(opName);
        if (operation == null || operation.getOutput() == null) throw new NoSuchOperationException();

        // TODO what if msg==null? i.e. for a reply-with-fault.

        MessageDAO message = myrolemex.createMessage(operation.getOutput().getMessage().getQName());
        buildOutgoingMessage(message, msg);

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
        ((ODEWSProcess)_bpelProcess).onMyRoleMexAck(myrolemex, previousStatus);
        sendEvent(evt);
    }

    public void reply(String mexId, Resource resource, Element msg, QName fault) throws NoSuchOperationException {
        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setMexId(mexId);
        evt.setResource(resource.getName());

        MessageExchangeDAO mex = _dao.getConnection().getMessageExchange(mexId);
        MessageDAO message = mex.createMessage(null);
        buildOutgoingMessage(message, msg);
        mex.setResponse(message);

        AckType ackType;
        if (fault != null) {
            ackType = AckType.FAULT;
            mex.setFault(fault);
            evt.setAspect(ProcessMessageExchangeEvent.PROCESS_FAULT);
        } else {
            ackType = AckType.RESPONSE;
            evt.setAspect(ProcessMessageExchangeEvent.PROCESS_OUTPUT);
        }

        String url = readResource(resource.getScopeInstanceId(), resource.getModel());

        Status previousStatus = mex.getStatus();
        mex.setStatus(Status.ACK);
        mex.setAckType(ackType);
        ((ODERESTProcess)_bpelProcess).onRestMexAck(mex, previousStatus, url);
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

    public void registerTimer(String timerChannelId, Date timeToFire) {
        WorkEvent we = new WorkEvent();
        we.setIID(_dao.getInstanceId());
        we.setProcessId(_bpelProcess.getPID());
        we.setChannel(timerChannelId);
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

    public String invoke(String requestId, PartnerLink partnerLink, Operation operation, Element outgoingMessage)
            throws UninitializedPartnerEPR {

        MessageExchangeDAO mexDao = _dao.getConnection().createMessageExchange(new GUID().toString(),
                MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE);
        mexDao.setStatus(MessageExchange.Status.REQ);
        mexDao.setOperation(operation.getName());
        mexDao.setPortType(partnerLink.getModel().getPartnerRolePortType().getQName());
        mexDao.setPartnerLinkModelId(partnerLink.getModel().getId());

        PartnerRoleChannel partnerRoleChannel = ((ODEWSProcess)_bpelProcess).getPartnerRoleChannel(partnerLink.getModel());
        PartnerLinkDAO plinkDAO = fetchPartnerLinkDAO(partnerLink);

        Element partnerEPR = plinkDAO.getPartnerEPR();

        EndpointReference partnerEpr;
        if (partnerEPR == null) {
            partnerEpr = partnerRoleChannel.getInitialEndpointReference();
            // In this case, the partner link has not been initialized.
            if (partnerEpr == null) throw new UninitializedPartnerEPR();
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
        message.setType(operation.getInput().getMessage().getQName());
        buildOutgoingMessage(message, outgoingMessage);

        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setOperation(operation.getName());
        evt.setPortType(partnerLink.getModel().getPartnerRolePortType().getQName());
        evt.setAspect(ProcessMessageExchangeEvent.PARTNER_INPUT);
        evt.setMexId(mexDao.getMessageExchangeId());
        sendEvent(evt);

        if (__log.isDebugEnabled()) {
            __log.debug("INVOKING PARTNER: partnerLink=" + partnerLink + ", op=" +
                    operation.getName() + " channel=" + requestId + ")");
        }
        _bpelProcess.invokePartner(mexDao);

        // In case a response/fault was available right away, which will happen for BLOCKING/TRANSACTED invocations,
        // we need to inject a message on the response channel, so that the process continues.
        switch (mexDao.getStatus()) {
        case ACK:
            if (mexDao.getChannel() != null) injectPartnerResponse(mexDao.getMessageExchangeId(), mexDao.getChannel());
            break;
        case ASYNC:
            // we'll have to wait for the response.
            break;
        default:
            throw new AssertionError("Unexpected MEX status: " + mexDao.getStatus());
        }
        
        return mexDao.getMessageExchangeId();
    }

    public String invoke(String requestId, org.apache.ode.bpel.iapi.Resource resource, Element outgoingMessage) {

        MessageExchangeDAO mexDao = _dao.getConnection().createMessageExchange(new GUID().toString(),
                MessageExchangeDAO.DIR_BPEL_INVOKES_PARTNERROLE);
        mexDao.setStatus(MessageExchange.Status.REQ);
        mexDao.setResource(resource.getUrl() + "~" + resource.getMethod());
        mexDao.setProcess(_dao.getProcess());
        mexDao.setInstance(_dao);
        mexDao.setPattern(MessageExchangePattern.REQUEST_RESPONSE);
        mexDao.setChannel(requestId);

        MessageDAO message = mexDao.createMessage(null);
        mexDao.setRequest(message);
        mexDao.setTimeout(30000);
        message.setData(outgoingMessage);

        // prepare event
        ProcessMessageExchangeEvent evt = new ProcessMessageExchangeEvent();
        evt.setResource(resource.getUrl() + "~" + resource.getMethod());
        evt.setAspect(ProcessMessageExchangeEvent.PARTNER_INPUT);
        evt.setMexId(mexDao.getMessageExchangeId());
        sendEvent(evt);

        if (__log.isDebugEnabled())
            __log.debug("INVOKING PARTNER: resource=" + resource + " channel=" + requestId + ")");

        _bpelProcess.invokePartner(mexDao);

        // In case a response/fault was available right away, which will happen for BLOCKING/TRANSACTED invocations,
        // we need to inject a message on the response channel, so that the process continues.
        switch (mexDao.getStatus()) {
        case ACK:
            if (mexDao.getChannel() != null) injectPartnerResponse(mexDao.getMessageExchangeId(), mexDao.getChannel());
            break;
        case ASYNC:
            // we'll have to wait for the response.
            break;
        default:
            throw new AssertionError("Unexpected MEX status: " + mexDao.getStatus());
        }

        return mexDao.getMessageExchangeId();
    }

    private void buildOutgoingMessage(MessageDAO message, Element outgoingElmt) {
        if (outgoingElmt == null) return;
        
        Document doc = DOMUtils.newDocument();
        Element header = doc.createElement("header");
        NodeList parts = outgoingElmt.getChildNodes();
        for (int m = 0; m < parts.getLength(); m++) {
            Element part = (Element) parts.item(m);
            if (part.getAttribute("headerPart") != null && part.getAttribute("headerPart").length() > 0) {
                header.appendChild(doc.importNode(part, true));
                outgoingElmt.removeChild(part);
            }
        }
        message.setData(outgoingElmt);
        message.setHeader(header);
    }

    public void executeCreateInstance(MessageExchangeDAO instantiatingMessageExchange) {
        if (instantiatingMessageExchange == null) throw new NullPointerException();
        _instantiatingMessageExchange = instantiatingMessageExchange;
        _rti.onCreateInstance(instantiatingMessageExchange.getMessageExchangeId());
        execute();
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
        if (_bpelProcess.isInMemory()) {
            try {
                ((ProcessInstanceDaoImpl)_dao).setSoup(_rti.saveState(null));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
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
    }

    void injectMyRoleMessageExchange(final String responseChannelId, final int idx, MessageExchangeDAO mexdao) {
        // if we have a message match, this instance should be marked
        // active if it isn't already
        if (_dao.getState() == ProcessState.STATE_READY) {
            if (ODEProcess.__log.isDebugEnabled()) {
                ODEProcess.__log.debug("INPUTMSGMATCH: Changing process instance state from ready to active");
            }

            _dao.setState(ProcessState.STATE_ACTIVE);

            // send event
            ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
            evt.setOldState(ProcessState.STATE_READY);
            evt.setNewState(ProcessState.STATE_ACTIVE);
            sendEvent(evt);
        }

        _rti.onSelectEvent(responseChannelId, mexdao.getMessageExchangeId(), idx);
    }

    boolean injectTimerEvent(final String timerResponseChannel) {
        // In case this is a pick event, we remove routes,
        // and cancel the outstanding requests.
        _dao.getProcess().removeRoutes(timerResponseChannel, _dao);

        // Ignore timer events after the process is finished.
        if (ProcessState.isFinished(_dao.getState())) return false;

        _rti.onTimerEvent(timerResponseChannel);
        return true;
    }

    public boolean cancelTimer(String timerId) {
        // TODO No way to cancel these now.
        return true;
    }

    public void cancelSelect(String selectId) {
        _dao.getProcess().removeRoutes(selectId, _dao);
    }

    void injectPartnerResponse(final String mexid, final String invokeId) {
        if (invokeId == null)
            throw new NullPointerException("Null responseChannelId");
        if (mexid == null)
            throw new NullPointerException("Null mexId");

        if (ODEProcess.__log.isDebugEnabled()) {
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

    public void sendEvent(ProcessInstanceEvent event) {
        // fill in missing pieces
        event.setProcessId(_dao.getProcess().getProcessId());
        event.setProcessName(_dao.getProcess().getType());
        event.setProcessInstanceId(_dao.getInstanceId());
        if (_bpelProcess._debugger != null) _bpelProcess._debugger.onEvent(event);

        //filter events
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

    public void noreply(String mexId, FaultInfo optionalFaultData) {
        MessageExchangeDAO mexDao = _dao.getConnection().getMessageExchange(mexId);
        if (mexDao != null) {
            Status status = mexDao.getStatus();
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
            ((ODEWSProcess)_bpelProcess).onMyRoleMexAck(mexDao, status);
        }
    }

    public Element getPartnerResponse(String mexId) {
        return mergeHeaders(_getPartnerResponse(mexId));
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

        return mergeHeaders(request);
    }

    private Element mergeHeaders(MessageDAO msg) {
        // Merging header data, it's all stored in the same variable
        Element data = msg.getData();
        if (msg.getHeader() != null) {
            if (data == null) {
                Document doc = DOMUtils.newDocument();
                data = doc.createElement("message");
                doc.appendChild(data);
            }

            NodeList headerParts = msg.getHeader().getChildNodes();
            for (int m = 0; m < headerParts.getLength(); m++) {
                if (headerParts.item(m).getNodeType() == Node.ELEMENT_NODE) {
                    Element headerPart = (Element) headerParts.item(m);
                    headerPart.setAttribute("headerPart", "true");
                    data.appendChild(data.getOwnerDocument().importNode(headerPart, true));
                }
            }
        }
        return data;
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
        String epr = dao.getProperty(WSMessageExchange.PROPERTY_SEP_PARTNERROLE_EPR);
        if (epr == null)
            return null;
        try {
            return DOMUtils.stringToDOM(epr);
        } catch (Exception ex) {
            __log.error("Invalid value for SEP property " + WSMessageExchange.PROPERTY_SEP_PARTNERROLE_EPR + ": " + epr);
        }

        return null;
    }

    public String getSourceSessionId(String mexId) {
        MessageExchangeDAO dao = _dao.getConnection().getMessageExchange(mexId);
        return dao.getProperty(WSMessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
    }

    public void registerActivityForRecovery(String channel, long activityId, String reason, Date dateTime,
            Element details, String[] actions, int retries) {
        if (reason == null)
            reason = "Unspecified";
        if (dateTime == null)
            dateTime = new Date();
        __log.info("ActivityRecovery: Registering activity " + activityId +
                ", failure reason: " + reason + " on channel " + channel);
        _dao.createActivityRecovery(channel, (int) activityId, reason, dateTime, details, actions, retries);
    }

    public void unregisterActivityForRecovery(String channel) {
        _dao.deleteActivityRecovery(channel);
    }

    void recoverActivity(final String channel, final long activityId, final String action, final FaultInfo fault) {
        _rti.recoverActivity(channel, activityId, action, fault);
        execute();
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
    
	public Node readExtVar(Variable variable, Node reference) throws ExternalVariableModuleException {
		Value val = _bpelProcess.getEVM().read(variable, reference, _iid);
		return val.value;
	}

	public ValueReferencePair writeExtVar(Variable variable, Node reference, Node value) throws ExternalVariableModuleException {
		ValueReferencePair vrp = new ValueReferencePair();
		
		Value val = _bpelProcess.getEVM().write(variable, reference, value, _iid);
		vrp.reference = val.locator.reference;
		vrp.value = val.value;
		
		return vrp;
	}

	public URI getBaseResourceURI() {
		return _bpelProcess.getBaseResourceURI();
	}

}
