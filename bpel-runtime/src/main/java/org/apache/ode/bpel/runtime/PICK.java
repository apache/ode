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
package org.apache.ode.bpel.runtime;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPickReceive;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.PickResponseChannel;
import org.apache.ode.bpel.runtime.channels.PickResponseChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.xsd.Duration;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Template for the BPEL <code>pick</code> activity.
 */
class PICK extends ACTIVITY {
    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(PICK.class);

    private OPickReceive _opick;

    // if multiple alarms are set, this is the alarm the evaluates to
    // the shortest absolute time until firing.
    private OPickReceive.OnAlarm _alarm = null;

    public PICK(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
        _opick = (OPickReceive) self.o;
    }

    /**
     * @see org.apache.ode.jacob.JacobRunnable#run()
     */
    public void run() {
        PickResponseChannel pickResponseChannel = newChannel(PickResponseChannel.class);
        Date timeout;
        Selector[] selectors;

        try {
            selectors = new Selector[_opick.onMessages.size()];
            int idx = 0;
            for (OPickReceive.OnMessage onMessage : _opick.onMessages) {
            	// collect all initiated correlations
            	Set<OScope.CorrelationSet> matchCorrelations = new HashSet<OScope.CorrelationSet>();
            	matchCorrelations.addAll(onMessage.matchCorrelations);
            	for( OScope.CorrelationSet cset : onMessage.joinCorrelations ) {
            		if(getBpelRuntimeContext().isCorrelationInitialized(_scopeFrame.resolve(cset))) {
            			matchCorrelations.add(cset);
            		}
            	}

                PartnerLinkInstance pLinkInstance = _scopeFrame.resolve(onMessage.partnerLink);
                CorrelationKeySet keySet = resolveCorrelationKey(pLinkInstance, matchCorrelations);

                selectors[idx] = new Selector(idx, pLinkInstance, onMessage.operation.getName(), onMessage.operation
                        .getOutput() == null, onMessage.messageExchangeId, keySet, onMessage.route);
                idx++;
            }

            timeout = null;
            for (OPickReceive.OnAlarm onAlarm : _opick.onAlarms) {
                Date dt = onAlarm.forExpr != null ? offsetFromNow(getBpelRuntimeContext().getExpLangRuntime()
                        .evaluateAsDuration(onAlarm.forExpr, getEvaluationContext())) : getBpelRuntimeContext()
                        .getExpLangRuntime().evaluateAsDate(onAlarm.untilExpr, getEvaluationContext()).getTime();
                if (timeout == null || timeout.compareTo(dt) > 0) {
                    timeout = dt;
                    _alarm = onAlarm;
                }
            }
            getBpelRuntimeContext().select(pickResponseChannel, timeout, _opick.createInstanceFlag, selectors);
        } catch (FaultException e) {
            __log.error(e);
            FaultData fault = createFault(e.getQName(), _opick, e.getMessage());
            dpe(_opick.outgoingLinks);
            _self.parent.completed(fault, CompensationHandler.emptySet());
            return;
        } catch (EvaluationException e) {
            String msg = "Unexpected evaluation error evaluating alarm.";
            __log.error(msg, e);
            throw new InvalidProcessException(msg, e);
        }

        // Dead path all the alarms that have no chace of coming first.
        for (OPickReceive.OnAlarm oa : _opick.onAlarms) {
            if (!oa.equals(_alarm)) {
                dpe(oa.activity);
            }
        }

        instance(new WAITING(pickResponseChannel));
    }

    /**
     * Resolves the correlation key from the given PartnerLinkInstance and a match type correlation(non-initiate or
     * already initialized join correlation).
     * 
     * @param pLinkInstance the partner link instance
     * @param matchCorrelations the match type correlation
     * @return returns the resolved CorrelationKey
     * @throws FaultException thrown when the correlation is not initialized and createInstance flag is not set
     */
    private CorrelationKeySet resolveCorrelationKey(PartnerLinkInstance pLinkInstance, Set<OScope.CorrelationSet> matchCorrelations) throws FaultException {
        CorrelationKeySet keySet = new CorrelationKeySet(); // is empty for the case of the createInstance activity

        if (matchCorrelations.isEmpty() && !_opick.createInstanceFlag) {
            // Adding a route for opaque correlation. In this case,
            // correlation is on "out-of-band" session-id
            String sessionId = getBpelRuntimeContext().fetchMySessionId(pLinkInstance);
            keySet.add(new CorrelationKey("-1", new String[] { sessionId }));
        } else if (!matchCorrelations.isEmpty()) {
    		for( OScope.CorrelationSet cset : matchCorrelations ) {
    			CorrelationKey key = null;
    			
        		if(!getBpelRuntimeContext().isCorrelationInitialized(
                    _scopeFrame.resolve(cset))) {
                    if (!_opick.createInstanceFlag) {
	                    throw new FaultException(_opick.getOwner().constants.qnCorrelationViolation,
	                    "Correlation not initialized.");
                    }
        		} else {
        			key = getBpelRuntimeContext().readCorrelation(_scopeFrame.resolve(cset));
                    assert key != null;
        		}
        		
        		if( key != null ) {
        			keySet.add(key);
        		}
    		}
        }
        
        return keySet;
    }
    
    /**
     * Calculate a duration offset from right now.
     *
     * @param duration
     *            the offset
     * @return the resulting date.
     */
    private static Date offsetFromNow(Duration duration) {
        Calendar cal = Calendar.getInstance();
        duration.addTo(cal);
        return cal.getTime();
    }

    @SuppressWarnings("unchecked")
    private void initVariable(String mexId, OPickReceive.OnMessage onMessage) {
        // This is allowed, if there is no parts in the message for example.
        if (onMessage.variable == null) return;

        Element msgEl;
        try {
            // At this point, not being able to get the request is most probably
            // a mex that hasn't properly replied to (process issue).
            msgEl = getBpelRuntimeContext().getMyRequest(mexId);
        } catch (BpelEngineException e) {
            __log.error("The message exchange seems to be in an unconsistent state, you're " +
                "probably missing a reply on a request/response interaction.");
            _self.parent.failure(e.toString(), null);
            return;
        }

        Collection<String> partNames = (Collection<String>) onMessage.operation.getInput().getMessage().getParts().keySet();

        // Let's do some sanity checks here so that we don't get weird errors in assignment later.
        // The engine should have checked to make sure that the messages that are  delivered conform 
        // to the correct format; but you know what they say, don't trust anyone.  
        if (!(onMessage.variable.type instanceof OMessageVarType)) {
            String errmsg = "Non-message variable for receive: should have been picked up by static analysis.";
            __log.fatal(errmsg);
            throw new InvalidProcessException(errmsg);
        }

        OMessageVarType vartype = (OMessageVarType) onMessage.variable.type;

        // Check that each part contains what we expect. 
        for (String pName : partNames) {
            QName partName = new QName(null, pName);
            Element msgPart = DOMUtils.findChildByName(msgEl, partName);
            Part part = vartype.parts.get(pName);
            if (part == null) {
                String errmsg = "Inconsistent WSDL, part " + pName + " not found in message type " + vartype.messageType;
                __log.fatal(errmsg);
                throw new InvalidProcessException(errmsg);
            }
            if (msgPart == null) {
                String errmsg = "Message missing part: " + pName;
                __log.fatal(errmsg);
                throw new InvalidContextException(errmsg);
            }

            if (part.type instanceof OElementVarType) {
                OElementVarType ptype = (OElementVarType) part.type;
                Element e  = DOMUtils.getFirstChildElement(msgPart);
                if (e == null) {
                    String errmsg = "Message (element) part " + pName + " did not contain child element.";
                    __log.fatal(errmsg);
                    throw new InvalidContextException(errmsg);
                }

                QName qn = new QName(e.getNamespaceURI(), e.getLocalName());
                if(!qn.equals(ptype.elementType)) {
                    String errmsg = "Message (element) part " + pName + " did not contain correct child element: expected "
                            + ptype.elementType + " but got " + qn;
                    __log.fatal(errmsg);
                    throw new InvalidContextException(errmsg);
                }
            }

        }

        VariableInstance vinst = _scopeFrame.resolve(onMessage.variable);

        try {
            initializeVariable(vinst, msgEl);
        } catch (ExternalVariableModuleException e) {
            __log.error("Exception while initializing external variable", e);
            _self.parent.failure(e.toString(), null);
            return;
        }

        // Generating event
        VariableModificationEvent se = new VariableModificationEvent(vinst.declaration.name);
        se.setNewValue(msgEl);
        if (_opick.debugInfo != null)
            se.setLineNo(_opick.debugInfo.startLine);
        sendEvent(se);
    }

    private class WAITING extends BpelJacobRunnable {
        private static final long serialVersionUID = 1L;

        private PickResponseChannel _pickResponseChannel;

        private WAITING(PickResponseChannel pickResponseChannel) {
            this._pickResponseChannel = pickResponseChannel;
        }

        public void run() {
            object(false, new PickResponseChannelListener(_pickResponseChannel) {
                private static final long serialVersionUID = -8237296827418738011L;

                public void onRequestRcvd(int selectorIdx, String mexId) {
                    OPickReceive.OnMessage onMessage = _opick.onMessages.get(selectorIdx);

                    // dead path the non-selected onMessage blocks.
                    for (OPickReceive.OnMessage onmsg : _opick.onMessages) {
                        if (!onmsg.equals(onMessage)) {
                            dpe(onmsg.activity);
                        }
                    }

                    // dead-path the alarm (if any)
                    if (_alarm != null) {
                        dpe(_alarm.activity);
                    }

                    getBpelRuntimeContext().cancelOutstandingRequests(_pickResponseChannel.export());

                    FaultData fault;
                    initVariable(mexId, onMessage);
                    try {
                        VariableInstance vinst = _scopeFrame.resolve(onMessage.variable);
                        for (OScope.CorrelationSet cset : onMessage.initCorrelations) {
                            initializeCorrelation(_scopeFrame.resolve(cset), vinst);
                        }
                        for( OScope.CorrelationSet cset : onMessage.joinCorrelations ) {
                        	// will be ignored if already initialized
                        	initializeCorrelation(_scopeFrame.resolve(cset), vinst);
                        }
                        if (onMessage.partnerLink.hasPartnerRole()) {
                            // Trying to initialize partner epr based on a
                            // message-provided epr/session.

                            if (!getBpelRuntimeContext().isPartnerRoleEndpointInitialized(
                                    _scopeFrame.resolve(onMessage.partnerLink))
                                    || !onMessage.partnerLink.initializePartnerRole) {

                                Node fromEpr = getBpelRuntimeContext().getSourceEPR(mexId);
                                if (fromEpr != null) {
                                    if (__log.isDebugEnabled())
                                        __log.debug("Received callback EPR " + DOMUtils.domToString(fromEpr)
                                                + " saving it on partner link " + onMessage.partnerLink.getName());
                                    getBpelRuntimeContext().writeEndpointReference(
                                            _scopeFrame.resolve(onMessage.partnerLink), (Element) fromEpr);
                                }
                            }

                            String partnersSessionId = getBpelRuntimeContext().getSourceSessionId(mexId);
                            if (partnersSessionId != null)
                                getBpelRuntimeContext().initializePartnersSessionId(
                                        _scopeFrame.resolve(onMessage.partnerLink), partnersSessionId);

                        }
                        // this request is now waiting for a reply
                        getBpelRuntimeContext().processOutstandingRequest(_scopeFrame.resolve(onMessage.partnerLink), 
                                onMessage.operation.getName(), onMessage.messageExchangeId, mexId);
                        
                    } catch (FaultException e) {
                        __log.error(e);
                        fault = createFault(e.getQName(), onMessage);
                        _self.parent.completed(fault, CompensationHandler.emptySet());
                        dpe(onMessage.activity);
                        return;
                    }


                    // load 'onMessage' activity
                    // Because we are done with all the DPE, we can simply
                    // re-use our control
                    // channels for the child.
                    ActivityInfo child = new ActivityInfo(genMonotonic(), onMessage.activity, _self.self, _self.parent);
                    instance(createChild(child, _scopeFrame, _linkFrame));
                }

                public void onTimeout() {
                    // Dead path all the onMessage activiites (the other alarms
                    // have already been DPE'ed)
                    for (OPickReceive.OnMessage onMessage : _opick.onMessages) {
                        dpe(onMessage.activity);
                    }

                    // Because we are done with all the DPE, we can simply
                    // re-use our control
                    // channels for the child.
                    ActivityInfo child = new ActivityInfo(genMonotonic(), _alarm.activity, _self.self, _self.parent);
                    instance(createChild(child, _scopeFrame, _linkFrame));
                }

                public void onCancel() {
                    _self.parent.completed(null, CompensationHandler.emptySet());
                }

            }.or(new TerminationChannelListener(_self.self) {
                private static final long serialVersionUID = 4399496341785922396L;

                public void terminate() {
                    getBpelRuntimeContext().cancel(_pickResponseChannel);
                    instance(WAITING.this);
                }
            }));
        }
    }
}
