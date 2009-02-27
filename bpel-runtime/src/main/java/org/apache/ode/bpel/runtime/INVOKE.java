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

import java.util.Collection;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ActivityFailureEvent;
import org.apache.ode.bpel.evt.ActivityRecoveryEvent;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.o.OFailureHandling;
import org.apache.ode.bpel.o.OInvoke;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.ActivityRecoveryChannel;
import org.apache.ode.bpel.runtime.channels.ActivityRecoveryChannelListener;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.InvokeResponseChannel;
import org.apache.ode.bpel.runtime.channels.InvokeResponseChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * JacobRunnable that performs the work of the <code>invoke</code> activity.
 */
public class INVOKE extends ACTIVITY {
    private static final long serialVersionUID = 992248281026821783L;
    private static final Log __log = LogFactory.getLog(INVOKE.class);

    private OInvoke _oinvoke;
    // Records number of invocations on the activity.
    private int     _invoked;
    // Date/time of last failure.
    private Date    _lastFailure;
    // Reason for last failure.
    private String  _failureReason;
    // Data associated with failure.
    private Element _failureData;

    public INVOKE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
        _oinvoke = (OInvoke) _self.o;
        _invoked = 0;
    }

    public final void run() {
        Element outboundMsg;
        try {
            outboundMsg = setupOutbound(_oinvoke, _oinvoke.initCorrelationsInput, _oinvoke.joinCorrelationsInput);
        } catch (FaultException e) {
            __log.error(e);
            FaultData fault = createFault(e.getQName(), _oinvoke);
            _self.parent.completed(fault, CompensationHandler.emptySet());
            return;
        } catch (ExternalVariableModuleException e) {
            __log.error(e);
            _self.parent.failure(e.toString(), null);
            return;
        }
        ++_invoked;

        // if there is no output variable, then this is a one-way invoke
        boolean isTwoWay = _oinvoke.outputVar != null;

        try {
            if (!isTwoWay) {
                FaultData faultData = null;
                getBpelRuntimeContext().invoke(_oinvoke.getId(),
                        _scopeFrame.resolve(_oinvoke.partnerLink),
                        _oinvoke.operation, outboundMsg, null);
                _self.parent.completed(faultData, CompensationHandler.emptySet());

            } else /* two-way */{
                final VariableInstance outputVar = _scopeFrame.resolve(_oinvoke.outputVar);
                InvokeResponseChannel invokeResponseChannel = newChannel(InvokeResponseChannel.class);

                final String mexId = getBpelRuntimeContext().invoke(_oinvoke.getId(),
                        _scopeFrame.resolve(_oinvoke.partnerLink), _oinvoke.operation,
                        outboundMsg, invokeResponseChannel);

                object(false, new InvokeResponseChannelListener(invokeResponseChannel) {
                    private static final long serialVersionUID = 4496880438819196765L;

                    public void onResponse() {
                        // we don't have to write variable data -> this already
                        // happened in the nativeAPI impl
                        FaultData fault = null;

                        Element response;
                        try {
                            response = getBpelRuntimeContext().getPartnerResponse(mexId);
                        } catch (Exception e) {
                            __log.error("Exception while processing invoke response", e);
                            throw new RuntimeException(e);
                        }

                        try {
                            initializeVariable(outputVar, response);
                        } catch (ExternalVariableModuleException e) {
                            __log.error("Exception while initializing external variable", e);
                            _self.parent.failure(e.toString(), null);
                            return;
                        }

                        // Generating event
                        VariableModificationEvent se = new VariableModificationEvent(outputVar.declaration.name);
                        se.setNewValue(response);
                        if (_oinvoke.debugInfo != null)
                            se.setLineNo(_oinvoke.debugInfo.startLine);
                        sendEvent(se);

                        try {
                            for (OScope.CorrelationSet anInitCorrelationsOutput : _oinvoke.initCorrelationsOutput) {
                                initializeCorrelation(_scopeFrame.resolve(anInitCorrelationsOutput), outputVar);
                            }
                            for (OScope.CorrelationSet aJoinCorrelationsOutput : _oinvoke.joinCorrelationsOutput) {
                            	// will be ignored if already initialized
                                initializeCorrelation(_scopeFrame.resolve(aJoinCorrelationsOutput), outputVar);
                            }
                            if (_oinvoke.partnerLink.hasPartnerRole()) {
                                // Trying to initialize partner epr based on a message-provided epr/session.
                                if (!getBpelRuntimeContext().isPartnerRoleEndpointInitialized(_scopeFrame
                                        .resolve(_oinvoke.partnerLink)) || !_oinvoke.partnerLink.initializePartnerRole) {

                                    Node fromEpr = getBpelRuntimeContext().getSourceEPR(mexId);
                                    if (fromEpr != null) {
                                        getBpelRuntimeContext().writeEndpointReference(
                                                _scopeFrame.resolve(_oinvoke.partnerLink), (Element) fromEpr);
                                    }
                                }

                                String partnersSessionId = getBpelRuntimeContext().getSourceSessionId(mexId);
                                if (partnersSessionId != null)
                                    getBpelRuntimeContext().initializePartnersSessionId(_scopeFrame.resolve(_oinvoke.partnerLink),
                                            partnersSessionId);

                            }
                        } catch (FaultException e) {
                            fault = createFault(e.getQName(), _oinvoke);
                        }

                        // TODO update output variable with data from non-initiate correlation sets

                        _self.parent.completed(fault, CompensationHandler.emptySet());
                        getBpelRuntimeContext().releasePartnerMex(mexId, fault == null);
                    }

                    public void onFault() {
                        QName faultName = getBpelRuntimeContext().getPartnerFault(mexId);
                        Element msg = getBpelRuntimeContext().getPartnerResponse(mexId);
                        QName msgType = getBpelRuntimeContext().getPartnerResponseType(mexId);
                        FaultData fault = createFault(faultName, msg,
                                _oinvoke.getOwner().messageTypes.get(msgType), _self.o);
                        _self.parent.completed(fault, CompensationHandler.emptySet());
                        getBpelRuntimeContext().releasePartnerMex(mexId, false);
                    }

                    public void onFailure() {
                        // This indicates a communication failure. We don't throw a fault,
                        // because there is no fault, instead we'll re-incarnate the invoke
                        // and either retry or indicate failure condition.
                        // admin to resume the process.
                        String reason = getBpelRuntimeContext().getPartnerFaultExplanation(mexId);
                        __log.error("Failure during invoke: " + reason);
                        try {
                            Element el = DOMUtils.stringToDOM("<invokeFailure><![CDATA["+reason+"]]></invokeFailure>");
                            _self.parent.failure(reason, el);
                        } catch (Exception e) {
                            _self.parent.failure(reason, null);
                        }
                        // Resuming the process creates a new invoke
                        getBpelRuntimeContext().releasePartnerMex(mexId, false);
                    }

                }.or(new TerminationChannelListener(_self.self) {
                    private static final long serialVersionUID = 4219496341785922396L;

                    public void terminate() {
                        _self.parent.completed(null, CompensationHandler.emptySet());
                    }
                }));

            }
        } catch (FaultException fault) {
            __log.error(fault);
            FaultData faultData = createFault(fault.getQName(), _oinvoke, fault.getMessage());
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        }
    }

    private Element setupOutbound(OInvoke oinvoke, Collection<OScope.CorrelationSet> outboundInitiations, Collection<OScope.CorrelationSet> outboundJoins)
            throws FaultException, ExternalVariableModuleException {
        for (OScope.CorrelationSet c : outboundInitiations) {
            initializeCorrelation(_scopeFrame.resolve(c), _scopeFrame.resolve(oinvoke.inputVar));
        }
        for (OScope.CorrelationSet c : outboundJoins) {
        	// will be ignored if already initialized
            initializeCorrelation(_scopeFrame.resolve(c), _scopeFrame.resolve(oinvoke.inputVar));
        }

        if (oinvoke.operation.getInput().getMessage().getParts().size() > 0) {
            sendVariableReadEvent(_scopeFrame.resolve(oinvoke.inputVar));
            Node outboundMsg = fetchVariableData(_scopeFrame.resolve(oinvoke.inputVar), false);
            // TODO outbound message should be updated with non-initiate correlation sets
            assert outboundMsg instanceof Element;
            return (Element) outboundMsg;
        } else return null;
    }

    @SuppressWarnings("unused")
    // TODO: does somebody need this method??
    private void requireRecovery() {
        if (__log.isDebugEnabled())
            __log.debug("ActivityRecovery: Invoke activity " + _self.aId + " requires recovery");
        sendEvent(new ActivityFailureEvent(_failureReason));
        final ActivityRecoveryChannel recoveryChannel = newChannel(ActivityRecoveryChannel.class);
        getBpelRuntimeContext().registerActivityForRecovery(recoveryChannel, _self.aId, _failureReason, _lastFailure, _failureData,
                new String[] { "retry", "cancel", "fault" }, _invoked - 1);
        object(false, new ActivityRecoveryChannelListener(recoveryChannel) {
            private static final long serialVersionUID = 8397883882810521685L;
            public void retry() {
                if (__log.isDebugEnabled())
                    __log.debug("ActivityRecovery: Retrying invoke activity " + _self.aId + " (user initiated)");
                sendEvent(new ActivityRecoveryEvent("retry"));
                getBpelRuntimeContext().unregisterActivityForRecovery(recoveryChannel);
                instance(INVOKE.this);
            }
            public void cancel() {
                if (__log.isDebugEnabled())
                    __log.debug("ActivityRecovery: Cancelling invoke activity " + _self.aId + " (user initiated)");
                sendEvent(new ActivityRecoveryEvent("cancel"));
                getBpelRuntimeContext().unregisterActivityForRecovery(recoveryChannel);
                _self.parent.cancelled();
            }
            public void fault(FaultData faultData) {
                if (__log.isDebugEnabled())
                    __log.debug("ActivityRecovery: Faulting invoke activity " + _self.aId + " (user initiated)");
                sendEvent(new ActivityRecoveryEvent("fault"));
                getBpelRuntimeContext().unregisterActivityForRecovery(recoveryChannel);
                if (faultData == null)
                    faultData = createFault(OFailureHandling.FAILURE_FAULT_NAME, _self.o, _failureReason);
                _self.parent.completed(faultData, CompensationHandler.emptySet());
            }
        }.or(new TerminationChannelListener(_self.self) {
            private static final long serialVersionUID = 2148587381204858397L;

            public void terminate() {
                if (__log.isDebugEnabled())
                    __log.debug("ActivityRecovery: Cancelling invoke activity " + _self.aId + " (terminated by scope)");
                getBpelRuntimeContext().unregisterActivityForRecovery(recoveryChannel);
                _self.parent.completed(null, CompensationHandler.emptySet());
            }
        }));
    }

}
