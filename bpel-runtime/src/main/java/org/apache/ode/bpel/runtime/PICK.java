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
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.obj.OElementVarType;
import org.apache.ode.bpel.obj.OMessageVarType;
import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.bpel.obj.OPickReceive;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.PickResponse;
import org.apache.ode.bpel.runtime.channels.Termination;
import org.apache.ode.jacob.ProcessUtil;
import org.apache.ode.jacob.ReceiveProcess;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.xsd.Duration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.apache.ode.jacob.ProcessUtil.compose;


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
        PickResponse pickResponseChannel = newChannel(PickResponse.class);
        Date timeout;
        Selector[] selectors;

        try {
            selectors = new Selector[_opick.getOnMessages().size()];
            int idx = 0;
            for (OPickReceive.OnMessage onMessage : _opick.getOnMessages()) {
                // collect all initiated correlations
                Set<OScope.CorrelationSet> matchCorrelations = new HashSet<OScope.CorrelationSet>();
                matchCorrelations.addAll(onMessage.getMatchCorrelations());
                for( OScope.CorrelationSet cset : onMessage.getJoinCorrelations() ) {
                    if(getBpelRuntimeContext().isCorrelationInitialized(_scopeFrame.resolve(cset))) {
                        matchCorrelations.add(cset);
                    }
                }

                PartnerLinkInstance pLinkInstance = _scopeFrame.resolve(onMessage.getPartnerLink());
                CorrelationKeySet keySet = resolveCorrelationKey(pLinkInstance, matchCorrelations);

                selectors[idx] = new Selector(idx, pLinkInstance, onMessage.getOperation().getName(), onMessage.getOperation()
                        .getOutput() == null, onMessage.getMessageExchangeId(), keySet, onMessage.getRoute());
                idx++;
            }

            timeout = null;
            for (OPickReceive.OnAlarm onAlarm : _opick.getOnAlarms()) {
                Date dt = onAlarm.getForExpr() != null ? offsetFromNow(getBpelRuntimeContext().getExpLangRuntime()
                        .evaluateAsDuration(onAlarm.getForExpr(), getEvaluationContext())) : getBpelRuntimeContext()
                        .getExpLangRuntime().evaluateAsDate(onAlarm.getUntilExpr(), getEvaluationContext()).getTime();
                if (timeout == null || timeout.compareTo(dt) > 0) {
                    timeout = dt;
                    _alarm = onAlarm;
                }
            }
            getBpelRuntimeContext().select(pickResponseChannel, timeout, _opick.isCreateInstanceFlag(), selectors);
        } catch (FaultException e) {
            __log.error(e);
            FaultData fault = createFault(e.getQName(), _opick, e.getMessage());
            dpe(_opick.getOutgoingLinks());
            _self.parent.completed(fault, CompensationHandler.emptySet());
            return;
        } catch (EvaluationException e) {
            String msg = "Unexpected evaluation error evaluating alarm.";
            __log.error(msg, e);
            throw new InvalidProcessException(msg, e);
        }

        // Dead path all the alarms that have no chace of coming first.
        for (OPickReceive.OnAlarm oa : _opick.getOnAlarms()) {
            if (!oa.equals(_alarm)) {
                dpe(oa.getActivity());
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

        if (matchCorrelations.isEmpty() && !_opick.isCreateInstanceFlag()) {
            // Adding a route for opaque correlation. In this case,
            // correlation is on "out-of-band" session-id
            String sessionId = getBpelRuntimeContext().fetchMySessionId(pLinkInstance);
            keySet.add(new CorrelationKey("-1", new String[] { sessionId }));
        } else if (!matchCorrelations.isEmpty()) {
            for( OScope.CorrelationSet cset : matchCorrelations ) {
                CorrelationKey key = null;

                if(!getBpelRuntimeContext().isCorrelationInitialized(
                    _scopeFrame.resolve(cset))) {
                    if (!_opick.isCreateInstanceFlag()) {
                        throw new FaultException(_opick.getOwner().getConstants().getQnCorrelationViolation(),
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
        if (onMessage.getVariable() == null) return;

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

        Collection<String> partNames = (Collection<String>) onMessage.getOperation().getInput().getMessage().getParts().keySet();

        // Let's do some sanity checks here so that we don't get weird errors in assignment later.
        // The engine should have checked to make sure that the messages that are  delivered conform
        // to the correct format; but you know what they say, don't trust anyone.
        if (!(onMessage.getVariable().getType() instanceof OMessageVarType)) {
            String errmsg = "Non-message variable for receive: should have been picked up by static analysis.";
            __log.fatal(errmsg);
            throw new InvalidProcessException(errmsg);
        }

        OMessageVarType vartype = (OMessageVarType) onMessage.getVariable().getType();

        // Check that each part contains what we expect.
        for (String pName : partNames) {
            QName partName = new QName(null, pName);
            Element msgPart = DOMUtils.findChildByName(msgEl, partName);
            Part part = vartype.getParts().get(pName);
            if (part == null) {
                String errmsg = "Inconsistent WSDL, part " + pName + " not found in message type " + vartype.getMessageType();
                __log.fatal(errmsg);
                throw new InvalidProcessException(errmsg);
            }
            if (msgPart == null) {
                String errmsg = "Message missing part: " + pName;
                __log.fatal(errmsg);
                throw new InvalidContextException(errmsg);
            }

            if (part.getType() instanceof OElementVarType) {
                OElementVarType ptype = (OElementVarType) part.getType();
                Element e  = DOMUtils.getFirstChildElement(msgPart);
                if (e == null) {
                    String errmsg = "Message (element) part " + pName + " did not contain child element.";
                    __log.fatal(errmsg);
                    throw new InvalidContextException(errmsg);
                }

                QName qn = new QName(e.getNamespaceURI(), e.getLocalName());
                if(!qn.equals(ptype.getElementType())) {
                    String errmsg = "Message (element) part " + pName + " did not contain correct child element: expected "
                            + ptype.getElementType() + " but got " + qn;
                    __log.fatal(errmsg);
                    throw new InvalidContextException(errmsg);
                }
            }

        }

        VariableInstance vinst = _scopeFrame.resolve(onMessage.getVariable());

        try {
            initializeVariable(vinst, msgEl);
        } catch (ExternalVariableModuleException e) {
            __log.error("Exception while initializing external variable", e);
            _self.parent.failure(e.toString(), null);
            return;
        }

        // Generating event
        VariableModificationEvent se = new VariableModificationEvent(vinst.declaration.getName());
        se.setNewValue(msgEl);
        if (_opick.getDebugInfo() != null)
            se.setLineNo(_opick.getDebugInfo().getStartLine());
        sendEvent(se);
    }

    private class WAITING extends BpelJacobRunnable {
        private static final long serialVersionUID = 1L;

        private PickResponse _pickResponseChannel;

        private WAITING(PickResponse pickResponseChannel) {
            this._pickResponseChannel = pickResponseChannel;
        }

        public void run() {
            object(false, compose(new ReceiveProcess() {
                private static final long serialVersionUID = -8237296827418738011L;
            }.setChannel(_pickResponseChannel).setReceiver(new PickResponse() {
                public void onRequestRcvd(int selectorIdx, String mexId) {
                    OPickReceive.OnMessage onMessage = _opick.getOnMessages().get(selectorIdx);

                    // dead path the non-selected onMessage blocks.
                    for (OPickReceive.OnMessage onmsg : _opick.getOnMessages()) {
                        if (!onmsg.equals(onMessage)) {
                            dpe(onmsg.getActivity());
                        }
                    }

                    // dead-path the alarm (if any)
                    if (_alarm != null) {
                        dpe(_alarm.getActivity());
                    }

                    getBpelRuntimeContext().cancelOutstandingRequests(ProcessUtil.exportChannel(_pickResponseChannel));

                    FaultData fault;
                    initVariable(mexId, onMessage);
                    try {
                        VariableInstance vinst = _scopeFrame.resolve(onMessage.getVariable());
                        for (OScope.CorrelationSet cset : onMessage.getInitCorrelations()) {
                            initializeCorrelation(_scopeFrame.resolve(cset), vinst);
                        }
                        for( OScope.CorrelationSet cset : onMessage.getJoinCorrelations() ) {
                            // will be ignored if already initialized
                            initializeCorrelation(_scopeFrame.resolve(cset), vinst);
                        }
                        if (onMessage.getPartnerLink().hasPartnerRole()) {
                            // Trying to initialize partner epr based on a
                            // message-provided epr/session.

                            if (!getBpelRuntimeContext().isPartnerRoleEndpointInitialized(
                                    _scopeFrame.resolve(onMessage.getPartnerLink()))
                                    || !onMessage.getPartnerLink().isInitializePartnerRole()) {

                                Node fromEpr = getBpelRuntimeContext().getSourceEPR(mexId);
                                if (fromEpr != null) {
                                    if (__log.isDebugEnabled())
                                        __log.debug("Received callback EPR " + DOMUtils.domToString(fromEpr)
                                                + " saving it on partner link " + onMessage.getPartnerLink().getName());
                                    getBpelRuntimeContext().writeEndpointReference(
                                            _scopeFrame.resolve(onMessage.getPartnerLink()), (Element) fromEpr);
                                }
                            }

                            String partnersSessionId = getBpelRuntimeContext().getSourceSessionId(mexId);
                            if (partnersSessionId != null)
                                getBpelRuntimeContext().initializePartnersSessionId(
                                        _scopeFrame.resolve(onMessage.getPartnerLink()), partnersSessionId);

                        }
                        // this request is now waiting for a reply
                        getBpelRuntimeContext().processOutstandingRequest(_scopeFrame.resolve(onMessage.getPartnerLink()),
                                onMessage.getOperation().getName(), onMessage.getMessageExchangeId(), mexId);

                    } catch (FaultException e) {
                        __log.error(e);
                        fault = createFault(e.getQName(), onMessage);
                        _self.parent.completed(fault, CompensationHandler.emptySet());
                        dpe(onMessage.getActivity());
                        return;
                    }


                    // load 'onMessage' activity
                    // Because we are done with all the DPE, we can simply
                    // re-use our control
                    // channels for the child.
                    ActivityInfo child = new ActivityInfo(genMonotonic(), onMessage.getActivity(), _self.self, _self.parent);
                    instance(createChild(child, _scopeFrame, _linkFrame));
                }

                public void onTimeout() {
                    // Dead path all the onMessage activiites (the other alarms
                    // have already been DPE'ed)
                    for (OPickReceive.OnMessage onMessage : _opick.getOnMessages()) {
                        dpe(onMessage.getActivity());
                    }

                    // Because we are done with all the DPE, we can simply
                    // re-use our control
                    // channels for the child.
                    ActivityInfo child = new ActivityInfo(genMonotonic(), _alarm.getActivity(), _self.self, _self.parent);
                    instance(createChild(child, _scopeFrame, _linkFrame));
                }

                public void onCancel() {
                    _self.parent.completed(null, CompensationHandler.emptySet());
                }

            })).or(new ReceiveProcess() {
                private static final long serialVersionUID = 4399496341785922396L;
            }.setChannel(_self.self).setReceiver(new Termination() {
                public void terminate() {
                    getBpelRuntimeContext().cancel(_pickResponseChannel);
                    instance(WAITING.this);
                }
            })));
        }
    }
}
