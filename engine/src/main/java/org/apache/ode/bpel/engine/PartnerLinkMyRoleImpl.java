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

import java.util.*;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.InvalidMessageException;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.evt.CorrelationMatchEvent;
import org.apache.ode.bpel.evt.CorrelationNoMatchEvent;
import org.apache.ode.bpel.evt.NewProcessInstanceEvent;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.CorrelationSetModel;
import org.apache.ode.bpel.rapi.PropertyAliasModel;
import org.apache.ode.bpel.rapi.PropertyExtractor;
import org.apache.ode.utils.CollectionUtils;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
class PartnerLinkMyRoleImpl extends PartnerLinkRoleImpl {
    private static final Log __log = LogFactory.getLog(ODEProcess.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** The local endpoint for this "myrole". */
    public Endpoint _endpoint;

    PartnerLinkMyRoleImpl(ODEProcess process, PartnerLinkModel plink, Endpoint endpoint) {
        super(process, plink);
        _endpoint = endpoint;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("{PartnerLinkRole-");
        buf.append(_plinkDef.getName());
        buf.append('.');
        buf.append(_plinkDef.getMyRoleName());
        buf.append(" on ");
        buf.append(_endpoint);
        buf.append('}');

        return buf.toString();
    }

    /**
     * Called when an input message has been received.
     * 
     * @param mex exchange to which the message is related
     */
    public CorrelationStatus invokeMyRole(MessageExchangeDAO mex) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter(this + ":inputMsgRcvd", new Object[] { "messageExchange", mex }));
        }

        Operation operation = getMyRoleOperation(mex.getOperation());
        if (operation == null) {
            __log.error(__msgs.msgUnknownOperation(mex.getOperation(), _plinkDef.getMyRolePortType().getQName()));
            MexDaoUtil.setFailed(mex, FailureType.UNKNOWN_OPERATION, mex.getOperation());
            return null;
        }

        // Is this a /possible/ createInstance Operation?
        boolean isCreateInstance = _plinkDef.isCreateInstanceOperation(operation);
        String correlatorId = ODEProcess.genCorrelatorId(_plinkDef, operation.getName());
        CorrelatorDAO correlator = _process.getProcessDAO().getCorrelator(correlatorId);

        MessageRouteDAO messageRoute = null;

        // now, the tricks begin: when a message arrives we have to see if there is anyone waiting for it. Get the correlator, a
        // persisted communnication-reduction data structure supporting correlation correlationKey matching!

        CorrelationKey[] processKeys, uniqueKeys;

        // We need to compute the correlation keys (based on the operation
        // we can infer which correlation keys to compute - this is merely a set
        // consisting of each correlationKey used in each correlation sets
        // that is ever referenced in an <receive>/<onMessage> on this
        // partnerlink/operation.
        try {
            processKeys = computeCorrelationKeys(mex, operation);
            uniqueKeys = computeUniqueCorrelationKeys(mex, operation);
        } catch (InvalidMessageException ime) {
            // We'd like to do a graceful exit here, no sense in rolling back due to a
            // a message format problem.
            __log.debug("Unable to evaluate correlation keys, invalid message format. ", ime);
            MexDaoUtil.setFailed(mex, FailureType.FORMAT_ERROR,  ime.getMessage());
            return null;
        }

        String mySessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);
        String partnerSessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
        if (__log.isDebugEnabled()) {
            __log.debug("INPUTMSG: " + correlatorId + ": MSG RCVD keys=" + CollectionUtils.makeCollection(HashSet.class, processKeys)
                    + " mySessionId=" + mySessionId + " partnerSessionId=" + partnerSessionId);
        }

        CorrelationKey matchedKey = null;

        // Try to find a route for one of our keys.
        for (CorrelationKey key : processKeys) {
            messageRoute = correlator.findRoute(key);
            if (messageRoute != null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("INPUTMSG: " + correlatorId + ": ckey " + key + " ROUTED TO (grp,index,iid) = (" + messageRoute.getGroupId() + "," + messageRoute.getIndex() + ", " + messageRoute.getTargetInstance().getInstanceId() +  ")");
                }
                matchedKey = key;
                break;
            }
        }

        // TODO - ODE-58

        // If no luck, and this operation qualifies for create-instance
        // treatment, then create a new process
        // instance.
        if (messageRoute == null && isCreateInstance) {
            invokeMyRoleCreateInstance(mex, operation, correlatorId, correlator, uniqueKeys);
        } else if (messageRoute != null) {
            if (__log.isDebugEnabled()) {
                __log.debug("INPUTMSG: " + correlatorId + ": ROUTING to instance "
                        + messageRoute.getTargetInstance().getInstanceId());
            }

            ProcessInstanceDAO instanceDAO = messageRoute.getTargetInstance();
            ProcessDAO processDAO = instanceDAO.getProcess();
            enforceUniqueConstraint(processDAO, uniqueKeys);

            // Reload process instance for DAO.

            // Kill the route so some new message does not get routed to
            // same process instance.
            correlator.removeRoutes(messageRoute.getGroupId(), instanceDAO);

            // send process instance event
            CorrelationMatchEvent evt = new CorrelationMatchEvent(_process.getProcessModel().getQName(),
                    _process.getProcessDAO().getProcessId(), instanceDAO.getInstanceId(), matchedKey);
            evt.setPortType(mex.getPortType());
            evt.setOperation(operation.getName());
            evt.setMexId(mex.getMessageExchangeId());

            _process._debugger.onEvent(evt);
            // store event
            _process.saveEvent(evt, instanceDAO);

            mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.MATCHED.toString());
            mex.setInstance(messageRoute.getTargetInstance());

            // We're overloading the channel here to be the PICK response channel + index
            mex.setChannel(messageRoute.getGroupId() + "&" + messageRoute.getIndex());
        } else {
            if (__log.isDebugEnabled()) {
                __log.debug("INPUTMSG: " + correlatorId + ": SAVING to DB (no match) ");
            }

            // TODO: Revist (BART)
            // if (!mex.isAsynchronous()) {
            // mex.setFailure(MessageExchange.FailureType.NOMATCH, "No process instance matching correlation keys.", null);
            //
            // } else {
            // send event
            CorrelationNoMatchEvent evt = new CorrelationNoMatchEvent(mex.getPortType(), mex.getOperation(), mex
                    .getMessageExchangeId(), processKeys);

            evt.setProcessId(_process.getProcessDAO().getProcessId());
            evt.setProcessName(_process.getProcessModel().getQName());
            _process._debugger.onEvent(evt);

            mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.QUEUED.toString());
            correlator.enqueueMessage(mex, processKeys);
        }

        return CorrelationStatus.valueOf(mex.getCorrelationStatus());
    }

    private void invokeMyRoleCreateInstance(MessageExchangeDAO mex, Operation operation, String correlatorId,
            CorrelatorDAO correlator, CorrelationKey[] uniqueKeys) {
        if (__log.isDebugEnabled()) {
            __log.debug("INPUTMSG: " + correlatorId + ": routing failed, CREATING NEW INSTANCE");
        }
        ProcessDAO processDAO = _process.getProcessDAO();

        if (_process._pconf.getState() == ProcessState.RETIRED) {
            throw new InvalidProcessException("Process is retired.", InvalidProcessException.RETIRED_CAUSE_CODE);
        }

        // if (!_process.processInterceptors(mex, InterceptorInvoker.__onNewInstanceInvoked)) {
        // __log.debug("Not creating a new instance for mex " + mex + "; interceptor prevented!");
        // return;
        // }

        enforceUniqueConstraint(processDAO, uniqueKeys);
        
        ProcessInstanceDAO newInstance = processDAO.createInstance(correlator);
        
        // send process instance event
        NewProcessInstanceEvent evt = new NewProcessInstanceEvent(_process.getProcessModel().getQName(),
                processDAO.getProcessId(), newInstance.getInstanceId());
        evt.setPortType(mex.getPortType());
        evt.setOperation(operation.getName());
        evt.setMexId(mex.getMessageExchangeId());
        _process._debugger.onEvent(evt);
        _process.saveEvent(evt, newInstance);
        mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.CREATE_INSTANCE.toString());
        mex.setInstance(newInstance);

    }

    private void enforceUniqueConstraint(ProcessDAO processDAO, CorrelationKey[] uniqueKeys) {
        for (CorrelationKey uniqueKey : uniqueKeys) {
            // double-check that the correlation set is indeed unique
            if (uniqueKey.isUnique()) {
                Collection<ProcessInstanceDAO> instances = processDAO.findInstance(uniqueKey, false);
                if (instances.size() != 0) {
                    __log.debug("Not creating a new instance for process " + processDAO.getProcessId() + "; unique correlation constraint would be violated!");
                    throw new InvalidProcessException("Unique process constraint violated", InvalidProcessException.DUPLICATE_CAUSE_CODE);
                }
            }        	
        }
    }

    @SuppressWarnings("unchecked")
    private Operation getMyRoleOperation(String operationName) {
        return _plinkDef.getMyRoleOperation(operationName);
    }

    private CorrelationKey[] computeCorrelationKeys(MessageExchangeDAO mex, Operation operation) {
        Element msg = mex.getRequest().getData();
        javax.wsdl.Message msgDescription = operation.getInput().getMessage();
        List<CorrelationKey> keys = new ArrayList<CorrelationKey>();

        Set<CorrelationSetModel> csets = _plinkDef.getCorrelationSetsForOperation(operation);
        for (CorrelationSetModel cset : csets) {
            CorrelationKey key = computeCorrelationKey(cset, msgDescription.getQName(), msg);
            keys.add(key);
        }

        // Let's creata a key based on the sessionId
        String mySessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);
        if (mySessionId != null)
            keys.add(new CorrelationKey(-1, new String[] { mySessionId }));

        return keys.toArray(new CorrelationKey[keys.size()]);
    }

    private CorrelationKey[] computeUniqueCorrelationKeys(MessageExchangeDAO mex, Operation operation) {
        Element msg = mex.getRequest().getData();
        javax.wsdl.Message msgDescription = operation.getInput().getMessage();
        List<CorrelationKey> keys = new ArrayList<CorrelationKey>();

        Set<CorrelationSetModel> csets = _plinkDef.getUniqueCorrelationSetsForOperation(operation);
        for (CorrelationSetModel cset : csets) {
            CorrelationKey key = computeCorrelationKey(cset, msgDescription.getQName(), msg);
            keys.add(key);
        }

        // Let's creata a key based on the sessionId
        String mySessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);
        if (mySessionId != null)
            keys.add(new CorrelationKey(-1, new String[] { mySessionId }));

        return keys.toArray(new CorrelationKey[keys.size()]);
    }
    
    private CorrelationKey computeCorrelationKey(CorrelationSetModel cset, QName messageName, Element msg) {
        String[] values;
        if (cset.getExtractors().isEmpty()) {
            List<PropertyAliasModel> aliases = cset.getAliases(messageName);
            values = new String[aliases.size()];
            int jIdx = 0;
            for (PropertyAliasModel alias : aliases) {
                String value;
                try {
                    value = _process._runtime.extractProperty(msg, alias, msg.toString());
                } catch (FaultException fe) {
                    String emsg = __msgs.msgPropertyAliasDerefFailedOnMessage(alias.getDescription(), fe.getMessage());
                    __log.error(emsg, fe);
                    throw new InvalidMessageException(emsg, fe);
                }
                values[jIdx++] = value;
            }
        } else {
            List<PropertyExtractor> extractors = cset.getExtractors();
            values = new String[extractors.size()];
            int jIdx = 0;
            for (PropertyExtractor extractor : extractors) {
                try {
                    values[jIdx++] = _process._runtime.extractMatch(msg, extractor);
                } catch (FaultException e) {
                    String emsg = __msgs.msgPropertyAliasDerefFailedOnMessage(extractor.toString(), e.getMessage());
                    __log.error(emsg, e);
                    throw new InvalidMessageException(emsg, e);
                }
            }
        }

        CorrelationKey key = new CorrelationKey(cset.getId(), values);
        key.setUnique(cset.isUnique());
        return key;
    }

    public boolean isOneWayOnly() {
        PortType portType = _plinkDef.getMyRolePortType();
        if (portType == null) {
            return false;
        }
        for (Operation operation : (List<Operation>) portType.getOperations()) {
            if (operation.getOutput() != null) {
                return false;
            }
        }
        return true;
    }

}
