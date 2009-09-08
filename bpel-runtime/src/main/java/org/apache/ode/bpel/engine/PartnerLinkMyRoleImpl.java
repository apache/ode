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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.InvalidMessageException;
import org.apache.ode.bpel.common.OptionalCorrelationKey;
import org.apache.ode.bpel.dao.CorrelatorDAO;
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
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.InvalidProcessException;
import org.apache.ode.bpel.runtime.PROCESS;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class PartnerLinkMyRoleImpl extends PartnerLinkRoleImpl {
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

    public boolean isCreateInstance(MyRoleMessageExchangeImpl mex) {
        Operation operation = getMyRoleOperation(mex.getOperationName());
        return _plinkDef.isCreateInstanceOperation(operation);
    }

    public List<RoutingInfo> findRoute(MyRoleMessageExchangeImpl mex) {
    	List<RoutingInfo> routingInfos = new ArrayList<RoutingInfo>();
    	
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter(this + ":inputMsgRcvd", new Object[] {
                    "messageExchange", mex }));
        }

        Operation operation = getMyRoleOperation(mex.getOperationName());
        if (operation == null) {
            __log.error(__msgs.msgUnknownOperation(mex.getOperationName(), _plinkDef.myRolePortType.getQName()));
            mex.setFailure(MessageExchange.FailureType.UNKNOWN_OPERATION, mex.getOperationName(), null);
            return null;
        }
        setMexRole(mex);

        // now, the tricks begin: when a message arrives we have to see if there
        // is anyone waiting for it. Get the correlator, a persisted communication-reduction
        // data structure supporting correlation correlationKey matching!
        String correlatorId = BpelProcess.genCorrelatorId(_plinkDef, operation.getName());

        CorrelatorDAO correlator = _process.getProcessDAO().getCorrelator(correlatorId);

        CorrelationKeySet keySet;

        // We need to compute the correlation keys (based on the operation
        // we can  infer which correlation keys to compute - this is merely a set
        // consisting of each correlationKey used in each correlation sets
        // that is ever referenced in an <receive>/<onMessage> on this
        // partnerlink/operation.
        try {
            keySet = computeCorrelationKeys(mex);
        } catch (InvalidMessageException ime) {
            // We'd like to do a graceful exit here, no sense in rolling back due to a
            // a message format problem.
            __log.debug("Unable to evaluate correlation keys, invalid message format. ",ime);
            mex.setFailure(MessageExchange.FailureType.FORMAT_ERROR, ime.getMessage(), null);
            return null;
        }
        
        String mySessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);
        String partnerSessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
        if (__log.isDebugEnabled()) {
            __log.debug("INPUTMSG: " + correlatorId + ": MSG RCVD keys="
                    + keySet + " mySessionId=" + mySessionId
                    + " partnerSessionId=" + partnerSessionId);
        }

        // Try to find a route for one of our keys.
        List<MessageRouteDAO> messageRoutes = correlator.findRoute(keySet);
        if (messageRoutes != null && messageRoutes.size() > 0) {
            for (MessageRouteDAO messageRoute : messageRoutes) {
                if (__log.isDebugEnabled()) {
                    __log.debug("INPUTMSG: " + correlatorId + ": ckeySet " + messageRoute.getCorrelationKeySet() + " route is to " + messageRoute);
                }
                routingInfos.add(new RoutingInfo(messageRoute, messageRoute.getCorrelationKeySet(), correlator, keySet));
            }
        }
        
        if (routingInfos.size() == 0) {
        	routingInfos.add(new RoutingInfo(null, null, correlator, keySet));
        }

        return routingInfos;
    }

    public static class RoutingInfo {
        public MessageRouteDAO messageRoute;
        public CorrelationKeySet matchedKeySet;
        public CorrelatorDAO correlator;
//        CorrelationKey[] keys;
        public CorrelationKeySet wholeKeySet;

        public RoutingInfo(MessageRouteDAO messageRoute, CorrelationKeySet matchedKeySet,
                           CorrelatorDAO correlator, CorrelationKeySet wholeKeySet) {
            this.messageRoute = messageRoute;
            this.matchedKeySet = matchedKeySet;
            this.correlator = correlator;
            this.wholeKeySet = wholeKeySet;
        }
    }

    public void invokeNewInstance(MyRoleMessageExchangeImpl mex, RoutingInfo routing) {
        Operation operation = getMyRoleOperation(mex.getOperationName());

        if (__log.isDebugEnabled()) {
            __log.debug("INPUTMSG: " + routing.correlator.getCorrelatorId() + ": routing failed, CREATING NEW INSTANCE");
        }
        ProcessDAO processDAO = _process.getProcessDAO();

        if (_process._pconf.getState() == ProcessState.RETIRED) {
            throw new InvalidProcessException("Process is retired.", InvalidProcessException.RETIRED_CAUSE_CODE);
        }

        if (!_process.processInterceptors(mex, InterceptorInvoker.__onNewInstanceInvoked)) {
            __log.debug("Not creating a new instance for mex " + mex + "; interceptor prevented!");
            throw new InvalidProcessException("Cannot instantiate process '" + _process.getPID() + "' any more.", InvalidProcessException.TOO_MANY_INSTANCES_CAUSE_CODE);
        }

        ProcessInstanceDAO newInstance = processDAO.createInstance(routing.correlator);

        BpelRuntimeContextImpl instance = _process
                .createRuntimeContext(newInstance, new PROCESS(_process.getOProcess()), mex);

        // send process instance event
        NewProcessInstanceEvent evt = new NewProcessInstanceEvent(new QName(_process.getOProcess().targetNamespace,
                _process.getOProcess().getName()), _process.getProcessDAO().getProcessId(), newInstance.getInstanceId());
        evt.setPortType(mex.getPortType().getQName());
        evt.setOperation(operation.getName());
        evt.setMexId(mex.getMessageExchangeId());
        _process._debugger.onEvent(evt);
        _process.saveEvent(evt, newInstance);
        mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.CREATE_INSTANCE);
        mex.getDAO().setInstance(newInstance);
        if (mex.getDAO().getCreateTime() == null)
            mex.getDAO().setCreateTime(instance.getCurrentEventDateTime()); 
        
        instance.execute();
    }

    public void invokeInstance(MyRoleMessageExchangeImpl mex, RoutingInfo routing) {
        Operation operation = getMyRoleOperation(mex.getOperationName());
        if (__log.isDebugEnabled()) {
            __log.debug("INPUTMSG: " + routing.correlator.getCorrelatorId() + ": ROUTING to instance "
                    + routing.messageRoute.getTargetInstance().getInstanceId());
        }

        ProcessInstanceDAO instanceDao = routing.messageRoute.getTargetInstance();

        // Reload process instance for DAO.
        BpelRuntimeContextImpl instance = _process.createRuntimeContext(instanceDao, null, null);
        instance.inputMsgMatch(routing.messageRoute.getGroupId(), routing.messageRoute.getIndex(), mex);

        // Kill the route so some new message does not get routed to
        // same process instance.
        routing.correlator.removeRoutes(routing.messageRoute.getGroupId(), instanceDao);

        // send process instance event
        CorrelationMatchEvent evt = new CorrelationMatchEvent(new QName(_process.getOProcess().targetNamespace,
                _process.getOProcess().getName()), _process.getProcessDAO().getProcessId(),
                instanceDao.getInstanceId(), routing.matchedKeySet);
        evt.setPortType(mex.getPortType().getQName());
        evt.setOperation(operation.getName());
        evt.setMexId(mex.getMessageExchangeId());

        _process._debugger.onEvent(evt);
        // store event
        _process.saveEvent(evt, instanceDao);

        mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.MATCHED);
        mex.getDAO().setInstance(routing.messageRoute.getTargetInstance());
        if (mex.getDAO().getCreateTime() == null)
            mex.getDAO().setCreateTime(instance.getCurrentEventDateTime()); 
        
        instance.execute();
    }

    public void noRoutingMatch(MyRoleMessageExchangeImpl mex, List<RoutingInfo> routings) {
        if (!mex.isAsynchronous()) {
            mex.setFailure(MessageExchange.FailureType.NOMATCH, "No process instance matching correlation keys.", null);
        } else {
        	// enqueue message with the last message route, as per the comments in caller (@see BpelProcess.invokeProcess())
        	RoutingInfo routing = 
        		(routings != null && routings.size() > 0) ? 
        				routings.get(routings.size() - 1) : null;
        	if (routing != null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("INPUTMSG: " + routing.correlator.getCorrelatorId() + ": SAVING to DB (no match) ");
                }

	            // send event
	            CorrelationNoMatchEvent evt = new CorrelationNoMatchEvent(mex.getPortType().getQName(), mex
	                    .getOperation().getName(), mex.getMessageExchangeId(), routing.wholeKeySet);
	
	            evt.setProcessId(_process.getProcessDAO().getProcessId());
	            evt.setProcessName(new QName(_process.getOProcess().targetNamespace, _process.getOProcess().getName()));
	            _process._debugger.onEvent(evt);
	
	            mex.setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.QUEUED);
	
	            // No match, means we add message exchange to the queue.
	            routing.correlator.enqueueMessage(mex.getDAO(), routing.wholeKeySet);
        	}
        }
    }

    private void setMexRole(MyRoleMessageExchangeImpl mex) {
        Operation operation = getMyRoleOperation(mex.getOperationName());
        mex.getDAO().setPartnerLinkModelId(_plinkDef.getId());
        mex.setPortOp(_plinkDef.myRolePortType, operation);
        mex.setPattern(operation.getOutput() == null ? MessageExchange.MessageExchangePattern.REQUEST_ONLY
                : MessageExchange.MessageExchangePattern.REQUEST_RESPONSE);
    }

    private Operation getMyRoleOperation(String operationName) {
        return _plinkDef.getMyRoleOperation(operationName);
    }

    private CorrelationKeySet computeCorrelationKeys(MyRoleMessageExchangeImpl mex) {
        CorrelationKeySet keySet = new CorrelationKeySet();

        Operation operation = mex.getOperation();
        Element msg = mex.getRequest().getMessage();
        javax.wsdl.Message msgDescription = operation.getInput().getMessage();

        Set<OScope.CorrelationSet> csets = _plinkDef.getNonInitiatingCorrelationSetsForOperation(operation);
        for (OScope.CorrelationSet cset : csets) {
            CorrelationKey key = computeCorrelationKey(cset,
                    _process.getOProcess().messageTypes.get(msgDescription.getQName()), msg);
            keySet.add(key);
        }

        csets = _plinkDef.getJoinningCorrelationSetsForOperation(operation);
        for (OScope.CorrelationSet cset : csets) {
            CorrelationKey key = computeCorrelationKey(cset,
                    _process.getOProcess().messageTypes.get(msgDescription.getQName()), msg);
            keySet.add(key);
        }

        // Let's creata a key based on the sessionId
        String mySessionId = mex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);
        if (mySessionId != null)
            keySet.add(new CorrelationKey("-1", new String[] { mySessionId }));

        return keySet;
    }

    @SuppressWarnings("unchecked")
    private CorrelationKey computeCorrelationKey(OScope.CorrelationSet cset, OMessageVarType messagetype,
            Element msg) {
    	CorrelationKey key = null;
    	
        String[] values = new String[cset.properties.size()];

        int jIdx = 0;
        for (Iterator j = cset.properties.iterator(); j.hasNext(); ++jIdx) {
            OProcess.OProperty property = (OProcess.OProperty) j.next();
            OProcess.OPropertyAlias alias = property.getAlias(messagetype);

            if (alias == null) {
                // TODO: Throw a real exception! And catch this at compile
                // time.
                throw new IllegalArgumentException("No alias matching property '" + property.name
                        + "' with message type '" + messagetype + "'");
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

        if( cset.hasJoinUseCases ) {
        	key = new OptionalCorrelationKey(cset.name, values);
        } else {
        	key = new CorrelationKey(cset.name, values);
        }
        
        return key;
    }
    
    @SuppressWarnings("unchecked")
    public boolean isOneWayOnly() {
		PortType portType = _plinkDef.myRolePortType;
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
