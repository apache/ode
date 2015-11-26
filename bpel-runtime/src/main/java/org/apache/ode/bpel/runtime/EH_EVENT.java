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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.obj.OEventHandler;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.runtime.channels.EventHandlerControl;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScope;
import org.apache.ode.bpel.runtime.channels.PickResponse;
import org.apache.ode.bpel.runtime.channels.Termination;
import org.apache.ode.jacob.CompositeProcess;
import org.apache.ode.jacob.ProcessUtil;
import org.apache.ode.jacob.ReceiveProcess;
import org.apache.ode.jacob.Synch;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Message event handler.
 */
class EH_EVENT extends BpelJacobRunnable {

    private static final long serialVersionUID = 1L;

    private static final Logger __log = LoggerFactory.getLogger(EH_EVENT.class);

    private EventHandlerControl _ehc;
    private Termination _tc;
    private ParentScope _psc;
    private ScopeFrame _scopeFrame;
    private OEventHandler.OEvent _oevent;

    /** Registered compensation handlers. */
    private Set<CompensationHandler> _comps = new HashSet<CompensationHandler>();

    private FaultData _fault;

    /** Active instances (we can have more than one!) */
    private Set<ActivityInfo> _active = new HashSet<ActivityInfo>();

    /** Whether a stop has been requested; if so no more new instances. */
    private boolean _stopped;

    /** Has a termination of this handler been requested. */
    private boolean _terminated;

    private boolean _childrenTerminated;


    EH_EVENT(ParentScope psc,Termination tc, EventHandlerControl ehc, OEventHandler.OEvent o, ScopeFrame scopeFrame) {
        _scopeFrame = scopeFrame;
        _oevent = o;
        _tc = tc;
        _psc = psc;
        _ehc = ehc;
    }


    public void run() {
        instance(new SELECT());
    }

    /**
     * Terminate all the active activities.
     */
    private void terminateActive() {
        if (!_childrenTerminated) {
            for (ActivityInfo tact : _active) {
                replication(tact.self).terminate();
            }
            _childrenTerminated = true;
        }
    }
    /**
     * Template that does the actual selection interaction with the runtime system, and
     * then waits on the pick response channel.
     */
    class SELECT extends BpelJacobRunnable {

        private static final long serialVersionUID = 1L;

        /**
         * @see org.apache.ode.jacob.JacobRunnable#run()
         */
        public void run() {
            Selector selector;
            try {
                PickResponse pickResponseChannel = newChannel(PickResponse.class);
                CorrelationKeySet keySet = new CorrelationKeySet();
                PartnerLinkInstance pLinkInstance = _scopeFrame.resolve(_oevent.getPartnerLink());
                for( OScope.CorrelationSet cset : _oevent.getJoinCorrelations() ) {
                    if(getBpelRuntimeContext().isCorrelationInitialized(_scopeFrame.resolve(cset))) {
                        keySet.add(getBpelRuntimeContext().readCorrelation(_scopeFrame.resolve(cset)));
                    }
                }
                for( OScope.CorrelationSet cset : _oevent.getMatchCorrelations() ) {
                    if (!getBpelRuntimeContext().isCorrelationInitialized(_scopeFrame.resolve(cset))) {
                        throw new FaultException(_oevent.getOwner().getConstants().getQnCorrelationViolation(),"Correlation not initialized.");
                    }
                    keySet.add(getBpelRuntimeContext().readCorrelation(_scopeFrame.resolve(cset)));
                }
                if( keySet.isEmpty() ) {
                    // Adding a route for opaque correlation. In this case correlation is done on "out-of-band" session id.
                    String sessionId = getBpelRuntimeContext().fetchMySessionId(pLinkInstance);
                    keySet.add(new CorrelationKey("-1", new String[] {sessionId}));
                }

                selector =  new Selector(0,pLinkInstance,_oevent.getOperation().getName(), _oevent.getOperation().getOutput() == null, _oevent.getMessageExchangeId(), keySet, _oevent.getRoute());
                getBpelRuntimeContext().select(pickResponseChannel, null, false, new Selector[] { selector} );
                instance(new WAITING(pickResponseChannel));
            } catch(FaultException e){
                __log.error("",e);
                if (_fault == null) {
                    _fault = createFault(e.getQName(), _oevent);
                }
                terminateActive();
                instance(new WAITING(null));
            }
        }
    }

    /**
     * Template that represents the waiting for a pick response.
     */
    private class WAITING extends BpelJacobRunnable {
        private static final long serialVersionUID = 1L;
        private PickResponse _pickResponseChannel;

        private WAITING(PickResponse pickResponseChannel) {
            _pickResponseChannel = pickResponseChannel;
        }

        public void run() {
            if (!_active.isEmpty() || _pickResponseChannel != null) {
                CompositeProcess mlset = ProcessUtil.compose(null);

                if (!_terminated) {
                    mlset.or(new ReceiveProcess() {
                        private static final long serialVersionUID = 7666910462948788042L;
                    }.setChannel(_tc).setReceiver(new Termination() {
                        public void terminate() {
                            terminateActive();
                            _terminated = true;
                            if (_pickResponseChannel != null)
                                getBpelRuntimeContext().cancel(_pickResponseChannel);
                            instance(WAITING.this);
                        }
                    }));
                }

                if (!_stopped) {
                    mlset.or(new ReceiveProcess() {
                        private static final long serialVersionUID = -1050788954724647970L;
                    }.setChannel(_ehc).setReceiver(new EventHandlerControl() {
                        public void stop() {
                            _stopped = true;
                            if (_pickResponseChannel != null)
                                getBpelRuntimeContext().cancel(_pickResponseChannel);
                            instance(WAITING.this);
                        }
                    }));
                }

                for (final ActivityInfo ai : _active) {
                    mlset.or(new ReceiveProcess() {
                        private static final long serialVersionUID = 5341207762415360982L;
                    }.setChannel(ai.parent).setReceiver(new ParentScope() {
                        public void compensate(OScope scope, Synch ret) {
                            _psc.compensate(scope, ret);
                            instance(WAITING.this);
                        }

                        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                            _active.remove(ai);
                            _comps.addAll(compensations);
                            if (faultData != null && _fault == null) {
                                _fault = faultData;
                                terminateActive();
                                // ODE-511; needs to clean up the route
                                if (_pickResponseChannel != null)
                                    getBpelRuntimeContext().cancel(_pickResponseChannel);
                                _psc.completed(_fault, _comps);
                            } else
                                instance(WAITING.this);
                        }

                        public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                        public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                    }));
                }

                if (_pickResponseChannel != null)
                    mlset.or(new ReceiveProcess() {
                        private static final long serialVersionUID = -4929999153478677288L;
                    }.setChannel(_pickResponseChannel).setReceiver(new PickResponse() {
                         public void onRequestRcvd(int selectorIdx, String mexId) {
                            // The receipt of the message causes a new scope to be created:
                            ScopeFrame ehScopeFrame = new ScopeFrame(_oevent,
                                    getBpelRuntimeContext().createScopeInstance(_scopeFrame.scopeInstanceId, _oevent),
                                    _scopeFrame,
                                    _comps,
                                    _fault);

                            if (_oevent.getVariable() != null) {
                                Element msgEl = getBpelRuntimeContext().getMyRequest(mexId);

                                if (msgEl != null) {
                                    try {
                                        VariableInstance vinst = ehScopeFrame.resolve(_oevent.getVariable());
                                        getBpelRuntimeContext().writeVariable(vinst, msgEl);

                                        VariableModificationEvent se = new VariableModificationEvent(vinst.declaration.getName());
                                        se.setNewValue(msgEl);
                                        _scopeFrame.fillEventInfo(se);
                                        if (_oevent.getDebugInfo() != null)
                                            se.setLineNo(_oevent.getDebugInfo().getStartLine());
                                        getBpelRuntimeContext().sendEvent(se);
                                    } catch (Exception ex) {
                                        __log.error("",ex);
                                        throw new InvalidProcessException(ex);
                                    }
                                }
                            }

                            try {
                                for (OScope.CorrelationSet cset : _oevent.getInitCorrelations()) {
                                    initializeCorrelation(ehScopeFrame.resolve(cset), ehScopeFrame.resolve(_oevent.getVariable()));
                                }
                                for( OScope.CorrelationSet cset : _oevent.getJoinCorrelations() ) {
                                    // will be ignored if already initialized
                                    initializeCorrelation(ehScopeFrame.resolve(cset), ehScopeFrame.resolve(_oevent.getVariable()));
                                }

                                if (_oevent.getPartnerLink().hasPartnerRole()) {
                                    // Trying to initialize partner epr based on a message-provided epr/session.
                                    if (!getBpelRuntimeContext().isPartnerRoleEndpointInitialized(ehScopeFrame
                                            .resolve(_oevent.getPartnerLink())) || !_oevent.getPartnerLink().isInitializePartnerRole()) {
                                        Node fromEpr = getBpelRuntimeContext().getSourceEPR(mexId);
                                        if (fromEpr != null) {
                                            getBpelRuntimeContext().writeEndpointReference(
                                                    ehScopeFrame.resolve(_oevent.getPartnerLink()), (Element) fromEpr);
                                        }
                                    }

                                    String partnersSessionId = getBpelRuntimeContext().getSourceSessionId(mexId);
                                    if (partnersSessionId != null)
                                        getBpelRuntimeContext().initializePartnersSessionId(ehScopeFrame.resolve(_oevent.getPartnerLink()),
                                                partnersSessionId);
                                }

                                getBpelRuntimeContext().cancelOutstandingRequests(ProcessUtil.exportChannel(_pickResponseChannel));
                                // this request is now waiting for a reply
                                getBpelRuntimeContext().processOutstandingRequest(_scopeFrame.resolve(_oevent.getPartnerLink()),
                                        _oevent.getOperation().getName(), _oevent.getMessageExchangeId(), mexId);

                            } catch (FaultException e) {
                                __log.error("",e);
                                if (_fault == null) {
                                    _fault = createFault(e.getQName(), _oevent);
                                    terminateActive();
                                }
                                instance(new WAITING(null));
                                return;
                            }

                            // load 'onMessage' activity; we'll do this even if a stop/terminate has been
                            // requested becasue we cannot undo the receipt of the message at this point.
                            ActivityInfo child = new ActivityInfo(genMonotonic(),
                                _oevent.getActivity(),
                                newChannel(Termination.class), newChannel(ParentScope.class));

                            _active.add(child);

                            LinkFrame lf = new LinkFrame(null);

                            ScopeFrame innerScopeFrame = new ScopeFrame((OScope) _oevent.getActivity(),
                                getBpelRuntimeContext().createScopeInstance(_scopeFrame.scopeInstanceId, (OScope) _oevent.getActivity()),
                                ehScopeFrame,
                                _comps,
                                _fault);
                            instance(new SCOPE(child, innerScopeFrame, lf));

                            // If we previously terminated the other activiites, then we do the same
                            // here; this is easier then undoing the receive.
                            if (_childrenTerminated)
                                replication(child.self).terminate();


                            if (_terminated || _stopped || _fault != null)
                                instance(new WAITING(null));
                            else
                                instance(new SELECT());
                        }


                        public void onTimeout() {
                            instance(new WAITING(null));
                        }

                        public void onCancel() {
                            instance(new WAITING(null));
                        }
                    }));

                object(false, mlset);
            } else /* Nothing more to do. */ {
                _psc.completed(_fault, _comps);
            }
        }
    }
}
