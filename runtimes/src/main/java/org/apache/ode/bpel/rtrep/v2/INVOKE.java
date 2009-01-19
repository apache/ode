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
package org.apache.ode.bpel.rtrep.v2;

import java.util.Collection;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.rtrep.v2.channels.FaultData;
import org.apache.ode.bpel.rtrep.v2.channels.InvokeResponseChannel;
import org.apache.ode.bpel.rtrep.v2.channels.InvokeResponseChannelListener;
import org.apache.ode.bpel.rtrep.v2.channels.TerminationChannelListener;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.iapi.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * JacobRunnable that performs the work of the <code>invoke</code> activity.
 */
public class INVOKE extends ACTIVITY {
    private static final long serialVersionUID = 992248281026821783L;
    private static final Log __log = LogFactory.getLog(INVOKE.class);

    private OInvoke _oinvoke;

    public INVOKE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
        _oinvoke = (OInvoke) _self.o;
    }

    public final void run() {
        Element outboundMsg;
        try {
            outboundMsg = setupOutbound(_oinvoke, _oinvoke.initCorrelationsInput);
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

        if (_oinvoke.isRestful()) {
            restInvoke(outboundMsg);
        } else {
            wsdlInvoke(outboundMsg);
        }
    }

    private void restInvoke(Element outboundMsg) {
        VariableInstance outputVar = null;
        if (_oinvoke.outputVar != null) outputVar = _scopeFrame.resolve(_oinvoke.outputVar);
        InvokeResponseChannel invokeResponseChannel = newChannel(InvokeResponseChannel.class);
        try {
            String path = getBpelRuntime().getExpLangRuntime()
                    .evaluateAsString(_oinvoke.resource.getSubpath(), getEvaluationContext());
            String mexId = getBpelRuntime().invoke(invokeResponseChannel.export(),
                    new Resource(path, "application/xml", _oinvoke.resource.getMethod()), outboundMsg);
            setupListeners(mexId, invokeResponseChannel, outputVar);
        } catch (FaultException fault) {
            __log.error(fault);
            FaultData faultData = createFault(fault.getQName(), _oinvoke, fault.getMessage());
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        }

    }

    private void wsdlInvoke(Element outboundMsg) {
        // if there is no output variable, then this is a one-way invoke
        boolean isTwoWay = _oinvoke.outputVar != null;

        try {
            if (!isTwoWay) {
                FaultData faultData = null;
                getBpelRuntime().invoke(null, _scopeFrame.resolve(_oinvoke.partnerLink),
                    _oinvoke.operation, outboundMsg, null);
                _self.parent.completed(faultData, CompensationHandler.emptySet());

            } else /* two-way */{
                final VariableInstance outputVar = _scopeFrame.resolve(_oinvoke.outputVar);
                final InvokeResponseChannel invokeResponseChannel = newChannel(InvokeResponseChannel.class);

                final String mexId = getBpelRuntime().invoke(invokeResponseChannel.export(),
                    _scopeFrame.resolve(_oinvoke.partnerLink), _oinvoke.operation,
                    outboundMsg, invokeResponseChannel);

                setupListeners(mexId, invokeResponseChannel, outputVar);
            }
        } catch (FaultException fault) {
            __log.error(fault);
            FaultData faultData = createFault(fault.getQName(), _oinvoke, fault.getMessage());
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        }
    }

    private void setupListeners(final String mexId, final InvokeResponseChannel invokeResponseChannel, final VariableInstance outputVar) {
        object(false, new InvokeResponseChannelListener(invokeResponseChannel) {
            private static final long serialVersionUID = 4496880438819196765L;

            public void onResponse() {
                // we don't have to write variable data -> this already
                // happened in the nativeAPI impl
                FaultData fault = null;

                Element response = null;
                try {
                    response = getBpelRuntime().getPartnerResponse(mexId);
                } catch (Exception e) {
                    // In RESTful invokes, we discover an empty response after the fact (204), so we could
                    // very well have no response here.
                    __log.debug(e);
                }

                if (outputVar != null && response ==null) {
                    String msg = "The process is expected to set an output variable after an invocation but " +
                            "there's no response provided by the partner. The process is going to be failed.";
                    __log.info(msg);
                    _self.parent.failure(msg, null);
                }                    

                try {
                    if (outputVar != null) initializeVariable(outputVar, response);
                } catch (ExternalVariableModuleException e) {
                    __log.error("Exception while initializing external variable", e);
                    _self.parent.failure(e.toString(), null);
                    return;
                }

                // Generating event
                if (outputVar != null) {
                    VariableModificationEvent se = new VariableModificationEvent(outputVar.declaration.name);
                    se.setNewValue(response);
                    if (_oinvoke.debugInfo != null)
                        se.setLineNo(_oinvoke.debugInfo.startLine);
                    sendEvent(se);
                }

                try {
                    for (OScope.CorrelationSet anInitCorrelationsOutput : _oinvoke.initCorrelationsOutput) {
                        initializeCorrelation(_scopeFrame.resolve(anInitCorrelationsOutput), outputVar);
                    }
                    if (_oinvoke.partnerLink != null && _oinvoke.partnerLink.hasPartnerRole()) {
                        // Trying to initialize partner epr based on a message-provided epr/session.
                        if (!getBpelRuntime().isPartnerRoleEndpointInitialized(_scopeFrame
                                .resolve(_oinvoke.partnerLink)) || !_oinvoke.partnerLink.initializePartnerRole) {

                            Node fromEpr = getBpelRuntime().getSourceEPR(mexId);
                            if (fromEpr != null) {
                                getBpelRuntime().writeEndpointReference(
                                    _scopeFrame.resolve(_oinvoke.partnerLink), (Element) fromEpr);
                            }
                        }

                        String partnersSessionId = getBpelRuntime().getSourceSessionId(mexId);
                        if (partnersSessionId != null)
                            getBpelRuntime().initializePartnersSessionId(
                                    _scopeFrame.resolve(_oinvoke.partnerLink), partnersSessionId);
                    }
                } catch (FaultException e) {
                    fault = createFault(e.getQName(), _oinvoke);
                }

                // TODO update output variable with data from non-initiate correlation sets

                _self.parent.completed(fault, CompensationHandler.emptySet());
                getBpelRuntime().releasePartnerMex(mexId);
            }

            public void onFault() {
                QName faultName = getBpelRuntime().getPartnerFault(mexId);
                Element msg = getBpelRuntime().getPartnerResponse(mexId);
                QName msgType = getBpelRuntime().getPartnerResponseType(mexId);
                FaultData fault = createFault(faultName, msg,
                    _oinvoke.getOwner().messageTypes.get(msgType), _self.o);
                _self.parent.completed(fault, CompensationHandler.emptySet());
                getBpelRuntime().releasePartnerMex(mexId);
            }

            public void onFailure() {
                // This indicates a communication failure. We don't throw a fault,
                // because there is no fault, instead we'll re-incarnate the invoke
                // and either retry or indicate failure condition.
                // admin to resume the process.
                _self.parent.failure(getBpelRuntime().getPartnerFaultExplanation(mexId), null);
                getBpelRuntime().releasePartnerMex(mexId);
            }

        }.or(new TerminationChannelListener(_self.self) {
            private static final long serialVersionUID = 4219496341785922396L;

            public void terminate() {
                _self.parent.completed(null, CompensationHandler.emptySet());
                object(new InvokeResponseChannelListener(invokeResponseChannel) {
                    private static final long serialVersionUID = 688746737897792929L;
                        public void onFailure() {
                            __log.debug("Failure on invoke ignored, the invoke has already been terminated: " + _oinvoke.toString());
                        }
                        public void onFault() {
                            __log.debug("Fault on invoke ignored, the invoke has already been terminated: " + _oinvoke.toString());
                        }
                        public void onResponse() {
                            __log.debug("Response on invoke ignored, the invoke has already been terminated: " + _oinvoke.toString());
                        }
                    });
                }
        }));
    }

    private Element setupOutbound(OInvoke oinvoke, Collection<OScope.CorrelationSet> outboundInitiations)
        throws FaultException, ExternalVariableModuleException {
        if (outboundInitiations.size() > 0) {
            for (OScope.CorrelationSet c : outboundInitiations) {
                initializeCorrelation(_scopeFrame.resolve(c), _scopeFrame.resolve(oinvoke.inputVar));
            }
        }

        if ((oinvoke.operation != null && oinvoke.operation.getInput().getMessage().getParts().size() > 0)
                || (oinvoke.isRestful() && oinvoke.inputVar != null)) {
            sendVariableReadEvent(_scopeFrame.resolve(oinvoke.inputVar));
            Node outboundMsg = fetchVariableData(_scopeFrame.resolve(oinvoke.inputVar), false);
            // TODO outbound message should be updated with non-initiate correlation sets
            assert outboundMsg instanceof Element;
            return (Element) outboundMsg;
        } else return null;
    }

}
