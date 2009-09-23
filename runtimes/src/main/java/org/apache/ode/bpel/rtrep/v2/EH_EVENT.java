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

import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.v2.channels.EventHandlerControlChannel;
import org.apache.ode.bpel.rtrep.v2.channels.EventHandlerControlChannelListener;
import org.apache.ode.bpel.rtrep.v2.channels.FaultData;
import org.apache.ode.bpel.rtrep.v2.channels.ParentScopeChannel;
import org.apache.ode.bpel.rtrep.v2.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.rtrep.v2.channels.PickResponseChannel;
import org.apache.ode.bpel.rtrep.v2.channels.PickResponseChannelListener;
import org.apache.ode.bpel.rtrep.v2.channels.TerminationChannel;
import org.apache.ode.bpel.rtrep.v2.channels.TerminationChannelListener;
import org.apache.ode.bpel.rapi.InvalidProcessException;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * Message event handler.
 */
class EH_EVENT extends BpelJacobRunnable {

    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(EH_EVENT.class);

    private EventHandlerControlChannel _ehc;
    private TerminationChannel _tc;
    private ParentScopeChannel _psc;
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

    EH_EVENT(ParentScopeChannel psc,TerminationChannel tc, EventHandlerControlChannel ehc, OEventHandler.OEvent o, ScopeFrame scopeFrame) {
        _scopeFrame = scopeFrame;
        _oevent = o;
        _tc = tc;
        _psc = psc;
        _ehc = ehc;
    }

    public void run() {
        instance(new SELECT(_scopeFrame, 0));
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

        private int _counter;

        public SELECT(ScopeFrame scopeFrame, int counter) {
            _scopeFrame = scopeFrame;
            _counter = counter;
        }

        /**
         * @see org.apache.ode.jacob.JacobRunnable#run()
         */
        public void run() {
            Selector selector;
            try {
                PickResponseChannel pickResponseChannel = newChannel(PickResponseChannel.class);
                if (_oevent.isRestful()) {
                    getBpelRuntime().checkResourceRoute(_scopeFrame.resolve(_oevent.resource),
                            _oevent.messageExchangeId+_counter, pickResponseChannel, 0);
                } else {
                    PartnerLinkInstance pLinkInstance = _scopeFrame.resolve(_oevent.partnerLink);
                    selector =  new Selector(0,pLinkInstance,_oevent.operation.getName(),
                            _oevent.operation.getOutput() == null, _oevent.messageExchangeId, getCorrelationKey(pLinkInstance));
                    getBpelRuntime().select(pickResponseChannel, null, false, new Selector[] { selector} );
                }
                instance(new WAITING(pickResponseChannel, _scopeFrame, _counter));
            } catch(FaultException e){
                __log.error(e);
                if (_fault == null) {
                    _fault = createFault(e.getQName(), _oevent);
                }
                terminateActive();
                instance(new WAITING(null, _scopeFrame, _counter));
            }
        }
    }
    
    private CorrelationKey getCorrelationKey(PartnerLinkInstance pLinkInstance) throws FaultException {
        CorrelationKey key = null;
        if (_oevent.matchCorrelation == null) {
            // Adding a route for opaque correlation. In this case correlation is done on "out-of-band" session id.
            String sessionId = getBpelRuntime().fetchMySessionId(pLinkInstance);
            key = new CorrelationKey(-1, new String[] {sessionId});
        } else {
            if (!getBpelRuntime().isCorrelationInitialized(_scopeFrame.resolve(_oevent.matchCorrelation))) {
                throw new FaultException(_oevent.getOwner().constants.qnCorrelationViolation,"Correlation not initialized.");
            }
            key = getBpelRuntime().readCorrelation(_scopeFrame.resolve(_oevent.matchCorrelation));
            assert key != null;
        }
        return key;
    }

    /**
     * Template that represents the waiting for a pick response.
     */
    private class WAITING extends BpelJacobRunnable {
        private static final long serialVersionUID = 1L;
        private PickResponseChannel _pickResponseChannel;
        private int _counter;

        private WAITING(PickResponseChannel pickResponseChannel, ScopeFrame scopeFrame, int counter) {
            _pickResponseChannel = pickResponseChannel;
            _scopeFrame = scopeFrame;
            _counter = counter;
        }

        public void run() {

            if (!_active.isEmpty() || _pickResponseChannel != null) {
                HashSet<ChannelListener> mlset = new HashSet<ChannelListener>();

                if (!_terminated) {
                    mlset.add(new TerminationChannelListener(_tc) {
                        private static final long serialVersionUID = 7666910462948788042L;
                        public void terminate() {
                            terminateActive();
                            _terminated = true;
                            if (_pickResponseChannel != null)
                                getBpelRuntime().cancel(_pickResponseChannel);
                            instance(WAITING.this);
                        }
                    });
                }

                if (!_stopped) {
                    mlset.add(new EventHandlerControlChannelListener(_ehc) {
                        private static final long serialVersionUID = -1050788954724647970L;
                        public void stop() {
                            _stopped = true;
                            if (_pickResponseChannel != null)
                                getBpelRuntime().cancel(_pickResponseChannel);
                            instance(WAITING.this);
                        }
                    });
                }

                for (final ActivityInfo ai : _active) {
                    mlset.add(new ParentScopeChannelListener(ai.parent) {
                        private static final long serialVersionUID = 5341207762415360982L;
                        public void compensate(OScope scope, SynchChannel ret) {
                            _psc.compensate(scope, ret);
                            instance(WAITING.this);
                        }
                        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                            _active.remove(ai);
                            _comps.addAll(compensations);
                            if (faultData != null && _fault == null) {
                                _fault = faultData;
                                terminateActive();
                                _psc.completed(_fault, _comps);
                            } else
                                instance(WAITING.this);
                        }
                        public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                        public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                    });
                }

                if (_pickResponseChannel != null)
                    mlset.add(new PickResponseChannelListener(_pickResponseChannel) {
                        private static final long serialVersionUID = -4929999153478677288L;

                        public void onRequestRcvd(int selectorIdx, String mexId) {
                            // The receipt of the message causes a new scope to be created:
                            ScopeFrame ehScopeFrame = new ScopeFrame(_oevent,
                                    getBpelRuntime().createScopeInstance(_scopeFrame.scopeInstanceId, _oevent),
                                    _scopeFrame, _comps, _fault);
                            ehScopeFrame.eventScope = true;
                            if (_oevent.isRestful()) {
                                getBpelRuntime().associateEvent(_scopeFrame.resolve(_oevent.resource),
                                        _oevent.messageExchangeId+_counter, _oevent.messageExchangeId+ehScopeFrame.scopeInstanceId);
                            } else {
                                try {
                                    PartnerLinkInstance plink = _scopeFrame.resolve(_oevent.partnerLink);
                                    if (_oevent.operation.getOutput() == null) {
                                        getBpelRuntime().cancel(_pickResponseChannel);
                                    } else {
                                        getBpelRuntime().associateEvent(plink, _oevent.operation.getName(),
                                                getCorrelationKey(plink), _oevent.messageExchangeId, mexId);
                                    }
                                } catch (FaultException e) {
                                    fault(e);
                                    return;
                                }
                            }

                            if (_oevent.variable != null) {
                                Element msgEl = getBpelRuntime().getMyRequest(mexId);

                                if (msgEl != null) {
                                    try {
                                        VariableInstance vinst = ehScopeFrame.resolve(_oevent.variable);
                                        initializeVariable(vinst, msgEl);

                                        VariableModificationEvent se = new VariableModificationEvent(vinst.declaration.name);
                                        se.setNewValue(msgEl);
                                        _scopeFrame.fillEventInfo(se);
                                        if (_oevent.debugInfo != null)
                                            se.setLineNo(_oevent.debugInfo.startLine);
                                        getBpelRuntime().sendEvent(se);
                                    } catch (Exception ex) {
                                        __log.fatal(ex);
                                        throw new InvalidProcessException(ex);
                                    }
                                }
                            }

                            if (_oevent.isRestful()) {
                                // Retrieving the map of properties associated with a RESTful mex. Those are the query
                                // parameters and are used to set implicit variables associated with these parameters
                                // in the process.
                                Map<String,String> props = getBpelRuntime().getProperties(mexId);
                                for (Map.Entry<String, String> entry : props.entrySet()) {
                                    VariableInstance vi = _scopeFrame.resolveVariable(entry.getKey());
                                    if (vi != null) {
                                        // Always expected to be a string
                                        Document doc = DOMUtils.newDocument();
                                        Node textNode = doc.createTextNode(entry.getValue());
                                        try {
                                            initializeVariable(vi, textNode);
                                        } catch (ExternalVariableModuleException e) {
                                            __log.error("Exception while initializing external variable", e);
                                            _psc.failure(e.toString(), null);
                                            return;
                                        }
                                    }
                                }

                            }

                            try {
                                for (OScope.CorrelationSet cset : _oevent.initCorrelations) {
                                    initializeCorrelation(ehScopeFrame.resolve(cset), ehScopeFrame.resolve(_oevent.variable));
                                }

                                if (_oevent.partnerLink != null && _oevent.partnerLink.hasPartnerRole()) {
                                    // Trying to initialize partner epr based on a message-provided epr/session.
                                    if (!getBpelRuntime().isPartnerRoleEndpointInitialized(ehScopeFrame
                                            .resolve(_oevent.partnerLink)) || !_oevent.partnerLink.initializePartnerRole) {
                                        Node fromEpr = getBpelRuntime().getSourceEPR(mexId);
                                        if (fromEpr != null) {
                                            getBpelRuntime().writeEndpointReference(
                                                    ehScopeFrame.resolve(_oevent.partnerLink), (Element) fromEpr);
                                        }
                                    }

                                    String partnersSessionId = getBpelRuntime().getSourceSessionId(mexId);
                                    if (partnersSessionId != null)
                                        getBpelRuntime().initializePartnersSessionId(ehScopeFrame.resolve(_oevent.partnerLink),
                                                partnersSessionId);
                                }

                            } catch (FaultException e) {
                                fault(e);
                                return;
                            }

                            // load 'onMessage' activity; we'll do this even if a stop/terminate has been
                            // requested becasue we cannot undo the receipt of the message at this point.
                            ActivityInfo child = new ActivityInfo(genMonotonic(), _oevent.activity,
                                    newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));

                            _active.add(child);

                            LinkFrame lf = new LinkFrame(null);

                            instance(new SCOPE(child,ehScopeFrame, lf));

                            // If we previously terminated the other activiites, then we do the same
                            // here; this is easier then undoing the receive.
                            if (_childrenTerminated) replication(child.self).terminate();

                            if (_terminated || _stopped || _fault != null)
                                instance(new WAITING(null, _scopeFrame, _counter));
                            else
                                instance(new SELECT(_scopeFrame, _counter+1));
                        }

                        public void onTimeout() {
                            instance(new WAITING(null, _scopeFrame, _counter));
                        }

                        public void onCancel() {
                            instance(new WAITING(null, _scopeFrame, _counter));
                        }
                        
                        private void fault(FaultException e) {
                            __log.error(e);
                            if (_fault == null) {
                                _fault = createFault(e.getQName(), _oevent);
                                terminateActive();
                            }
                            instance(new WAITING(null, _scopeFrame, _counter));
                        }
                    });

                object(false, mlset);
            } else /* Nothing more to do. */ {
                _psc.completed(_fault, _comps);
            }
        }

    }
}
