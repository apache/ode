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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.wsdl.Operation;
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
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.utils.ArrayUtils;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
class PartnerLinkMyRoleImpl extends PartnerLinkRoleImpl {
    private static final Log __log = LogFactory.getLog(BpelProcess.class);

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** The local endpoint for this "myrole". */
    public Endpoint _endpoint;

    PartnerLinkMyRoleImpl(BpelProcess process, OPartnerLink plink, Endpoint endpoint) {
        super(process, plink);
        _endpoint = endpoint;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("{PartnerLinkRole-");
        buf.append(_plinkDef.name);
        buf.append('.');
        buf.append(_plinkDef.myRoleName);
        buf.append(" on ");
        buf.append(_endpoint);
        buf.append('}');

        return buf.toString();
    }

    /**
     * Called when an input message has been received.
     * 
     * @param mex
     *            exchange to which the message is related
     */
    public CorrelationStatus invokeMyRole(MessageExchangeDAO mex) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter(this + ":inputMsgRcvd", new Object[] { "messageExchange", mex }));
        }

        Operation operation = getMyRoleOperation(mex.getOperation());
        if (operation == null) {
            __log.error(__msgs.msgUnknownOperation(mex.getOperation(), _plinkDef.myRolePortType.getQName()));
            MexDaoUtil.setFailed(mex, FailureType.UNKNOWN_OPERATION, mex.getOperation());
            return null;
        }

        // Is this a /possible/ createInstance Operation?
        boolean isCreateInstnace = _plinkDef.isCreateInstanceOperation(operation);
        String correlatorId = BpelProcess.genCorrelatorId(_plinkDef, operation.getName());
        CorrelatorDAO correlator = _process.getProcessDAO().getCorrelator(correlatorId);

        // Special logic for in-mem processes, only createInstance is allowed, so we can skip the
        // correlation BS to save time.
        if (_process.isInMemory()) {
            if (isCreateInstnace)
                invokeMyRoleCreateInstance(mex, operation, correlatorId, correlator);
            else {
                MexDaoUtil.setFailed(mex, FailureType.OTHER,
                        "Invalid in-memory process: non createInstance operations are not supported!");
                return null;
            }

        } else {

            MessageRouteDAO messageRoute = null;

            // now, the tricks begin: when a message arrives we have to see if there is anyone waiting for it. Get the correlator, a
            // persisted communnication-reduction data structure supporting correlation correlationKey matching!

            CorrelationKey[] keys;

            // We need to compute the correlation keys (based on the operation
            // we can infer which correlation keys to compute - this is merely a set
            // consisting of each correlationKey used in each correlation sets
            // that is ever referenced in an <receive>/<onMessage> on this
            // partnerlink/operation.
            try {
                keys = computeCorrelationKeys(mex, operation);
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
                __log.debug("INPUTMSG: " + correlatorId + ": MSG RCVD keys=" + ArrayUtils.makeCollection(HashSet.class, keys)
                        + " mySessionId=" + mySessionId + " partnerSessionId=" + partnerSessionId);
            }

            CorrelationKey matchedKey = null;

            // Try to find a route for one of our keys.
            for (CorrelationKey key : keys) {
                messageRoute = correlator.findRoute(key);
                if (messageRoute != null) {
                    if (__log.isDebugEnabled()) {
                        __log.debug("INPUTMSG: " + correlatorId + ": ckey " + key + " route is to " + messageRoute);
                    }
                    matchedKey = key;
                    break;
                }
            }

            // TODO - ODE-58

            // If no luck, and this operation qualifies for create-instance
            // treatment, then create a new process
            // instance.
            if (messageRoute == null && isCreateInstnace) {
                invokeMyRoleCreateInstance(mex, operation, correlatorId, correlator);
            } else if (messageRoute != null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("INPUTMSG: " + correlatorId + ": ROUTING to instance "
                            + messageRoute.getTargetInstance().getInstanceId());
                }

                ProcessInstanceDAO instanceDao = messageRoute.getTargetInstance();

                // Reload process instance for DAO.

                // Kill the route so some new message does not get routed to
                // same process instance.
                correlator.removeRoutes(messageRoute.getGroupId(), instanceDao);

                // send process instance event
                CorrelationMatchEvent evt = new CorrelationMatchEvent(new QName(_process.getOProcess().targetNamespace, _process
                        .getOProcess().getName()), _process.getProcessDAO().getProcessId(), instanceDao.getInstanceId(), matchedKey);
                evt.setPortType(mex.getPortType());
                evt.setOperation(operation.getName());
                evt.setMexId(mex.getMessageExchangeId());

                _process._debugger.onEvent(evt);
                // store event
                _process.saveEvent(evt, instanceDao);

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
                        .getMessageExchangeId(), keys);

                evt.setProcessId(_process.getProcessDAO().getProcessId());
                evt.setProcessName(new QName(_process.getOProcess().targetNamespace, _process.getOProcess().getName()));
                _process._debugger.onEvent(evt);

                mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.QUEUED.toString());
                correlator.enqueueMessage(mex, keys);
            }

        }

        return CorrelationStatus.valueOf(mex.getCorrelationStatus());
    }

    private void invokeMyRoleCreateInstance(MessageExchangeDAO mex, Operation operation, String correlatorId,
            CorrelatorDAO correlator) {
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

        ProcessInstanceDAO newInstance = processDAO.createInstance(correlator);

        // send process instance event
        NewProcessInstanceEvent evt = new NewProcessInstanceEvent(new QName(_process.getOProcess().targetNamespace, _process
                .getOProcess().getName()), _process.getProcessDAO().getProcessId(), newInstance.getInstanceId());
        evt.setPortType(mex.getPortType());
        evt.setOperation(operation.getName());
        evt.setMexId(mex.getMessageExchangeId());
        _process._debugger.onEvent(evt);
        _process.saveEvent(evt, newInstance);
        mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.CREATE_INSTANCE.toString());
        mex.setInstance(newInstance);

    }

    @SuppressWarnings("unchecked")
    private Operation getMyRoleOperation(String operationName) {
        Operation op = _plinkDef.getMyRoleOperation(operationName);
        return op;
    }

    private CorrelationKey[] computeCorrelationKeys(MessageExchangeDAO mex, Operation operation) {
        Element msg = mex.getRequest().getData();
        javax.wsdl.Message msgDescription = operation.getInput().getMessage();
        List<CorrelationKey> keys = new ArrayList<CorrelationKey>();

        Set<OScope.CorrelationSet> csets = _plinkDef.getCorrelationSetsForOperation(operation);

        for (OScope.CorrelationSet cset : csets) {
            CorrelationKey key = computeCorrelationKey(cset, _process.getOProcess().messageTypes.get(msgDescription.getQName()),
                    msg);
            keys.add(key);
        }

        // Let's creata a key based on the sessionId
        String mySessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);
        if (mySessionId != null)
            keys.add(new CorrelationKey(-1, new String[] { mySessionId }));

        return keys.toArray(new CorrelationKey[keys.size()]);
    }

    private CorrelationKey computeCorrelationKey(OScope.CorrelationSet cset, OMessageVarType messagetype, Element msg) {
        String[] values = new String[cset.properties.size()];

        int jIdx = 0;
        for (Iterator j = cset.properties.iterator(); j.hasNext(); ++jIdx) {
            OProcess.OProperty property = (OProcess.OProperty) j.next();
            OProcess.OPropertyAlias alias = property.getAlias(messagetype);

            if (alias == null) {
                // TODO: Throw a real exception! And catch this at compile
                // time.
                throw new IllegalArgumentException("No alias matching property '" + property.name + "' with message type '"
                        + messagetype + "'");
            }

            String value;
            try {
                value = _process.extractProperty(msg, alias, msg.toString());
            } catch (FaultException fe) {
                String emsg = __msgs.msgPropertyAliasDerefFailedOnMessage(alias.getDescription(), fe.getMessage());
                __log.error(emsg, fe);
                throw new InvalidMessageException(emsg, fe);
            }
            values[jIdx] = value;
        }

        CorrelationKey key = new CorrelationKey(cset.getId(), values);
        return key;
    }

}
