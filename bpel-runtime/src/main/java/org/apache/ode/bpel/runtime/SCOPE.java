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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.evt.ScopeCompletionEvent;
import org.apache.ode.bpel.evt.ScopeFaultEvent;
import org.apache.ode.bpel.evt.ScopeStartEvent;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OCatch;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OEventHandler;
import org.apache.ode.bpel.o.OFailureHandling;
import org.apache.ode.bpel.o.OFaultHandler;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.runtime.channels.CompensationChannel;
import org.apache.ode.bpel.runtime.channels.EventHandlerControlChannel;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.SynchChannel;
import org.w3c.dom.Element;

/**
 * An active scope.
 */
class SCOPE extends ACTIVITY {
    private static final long serialVersionUID = 6111903798996023525L;

    private static final Log __log = LogFactory.getLog(SCOPE.class);

    private OScope _oscope;
    private ActivityInfo _child;
    private Set<EventHandlerInfo> _eventHandlers = new HashSet<EventHandlerInfo>();

    /** Constructor. */
    public SCOPE(ActivityInfo self, ScopeFrame frame, LinkFrame linkFrame) {
        super(self, frame, linkFrame);
        _oscope = (OScope) self.o;
        assert _oscope.activity != null;
    }

    public void run() {

        // Start the child activity.
        _child = new ActivityInfo(genMonotonic(),
            _oscope.activity,
            newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
        instance(createChild(_child, _scopeFrame, _linkFrame));

        if (_oscope.eventHandler != null) {
            for (Iterator<OEventHandler.OAlarm> i = _oscope.eventHandler.onAlarms.iterator(); i.hasNext(); ) {
                OEventHandler.OAlarm alarm = i.next();
                EventHandlerInfo ehi = new EventHandlerInfo(alarm,
                        newChannel(EventHandlerControlChannel.class),
                        newChannel(ParentScopeChannel.class),
                        newChannel(TerminationChannel.class));
                _eventHandlers.add(ehi);
                instance(new EH_ALARM(ehi.psc,ehi.tc, ehi.cc, alarm, _scopeFrame));
            }

            for (Iterator<OEventHandler.OEvent> i = _oscope.eventHandler.onMessages.iterator(); i.hasNext(); ) {
                OEventHandler.OEvent event = i.next();
                EventHandlerInfo ehi = new EventHandlerInfo(event,
                        newChannel(EventHandlerControlChannel.class),
                        newChannel(ParentScopeChannel.class),
                        newChannel(TerminationChannel.class));
                _eventHandlers.add(ehi);
                instance(new EH_EVENT(ehi.psc,ehi.tc, ehi.cc, event, _scopeFrame));
            }
        }

        getBpelRuntimeContext().initializePartnerLinks(_scopeFrame.scopeInstanceId,
            _oscope.partnerLinks.values());

        sendEvent(new ScopeStartEvent());
        instance(new ACTIVE());
    }

    private List<CompensationHandler> findCompensationData(OScope scope) {
        List<CompensationHandler> out = new ArrayList<CompensationHandler>();
        for (Iterator<CompensationHandler> i = _scopeFrame.availableCompensations.iterator(); i.hasNext(); ) {
            CompensationHandler  ch = i.next();
            if (null == scope || ch.compensated.oscope.equals(scope))
                out.add(ch);
        }
        // sort out in terms of completion order
        Collections.sort(out);
        return out;
    }

    class ACTIVE extends ACTIVITY {
        private static final long serialVersionUID = -5876892592071965346L;
        /** Links collected. */
        private boolean _terminated;
        private FaultData _fault;
        private long _startTime;
        private final HashSet<CompensationHandler> _compensations = new HashSet<CompensationHandler>();
        private boolean _childTermRequested;

        ACTIVE() {
            super(SCOPE.this._self, SCOPE.this._scopeFrame, SCOPE.this._linkFrame);
            _startTime = System.currentTimeMillis();
        }

        public void run() {
            if (_child != null || !_eventHandlers.isEmpty()) {
                HashSet<ChannelListener> mlSet = new HashSet<ChannelListener>();

                // Listen to messages from our parent.
                mlSet.add(new TerminationChannelListener(_self.self) {
                    private static final long serialVersionUID = 1913414844895865116L;

                    public void terminate() {
                        _terminated = true;

                        // Forward the termination request to the nested activity.
                        if (_child != null && !_childTermRequested) {
                            replication(_child.self).terminate();
                            _childTermRequested = true;
                        }

                        // Forward the termination request to our event handlers.
                        terminateEventHandlers();

                        instance(ACTIVE.this);
                    }
                });

                // Handle messages from the child if it is still alive
                if (_child != null) {
                    mlSet.add(new ParentScopeChannelListener(_child.parent) {
                        private static final long serialVersionUID = -6934246487304813033L;

                        public void compensate(OScope scope, SynchChannel ret) {
                            //  If this scope does not have available compensations, defer to
                            // parent scope, otherwise do compensation.
                            if (_scopeFrame.availableCompensations == null)
                                _self.parent.compensate(scope, ret);
                            else {
                                // TODO: Check if we are doing duplicate compensation
                                List<CompensationHandler> compensations = findCompensationData(scope);
                                _scopeFrame.availableCompensations.removeAll(compensations);
                                instance(new ORDEREDCOMPENSATOR(compensations, ret));
                            }
                            instance(ACTIVE.this);
                        }

                        public void completed(FaultData flt, Set<CompensationHandler> compensations) {
                              // Set the fault to the activity's choice, if and only if no previous fault
                              // has been detected (first fault wins).
                              if (flt != null && _fault == null)
                                  _fault = flt;
                              _child = null;
                              _compensations.addAll(compensations);

                              if (flt == null)
                                  stopEventHandlers();
                              else
                                  terminateEventHandlers();

                              instance(ACTIVE.this);
                        }

                        public void cancelled() {
                            // Implicit scope holds links of the enclosed activity,
                            // they only get cancelled when we propagate upwards.
                            if (_oscope.implicitScope)
                                _self.parent.cancelled();
                            else
                                completed(null, CompensationHandler.emptySet());
                        }

                        public void failure(String reason, Element data) {
                            completed(createFault(OFailureHandling.FAILURE_FAULT_NAME, _self.o, null),
                                      CompensationHandler.emptySet());
                        }

                    });
                }

                // Similarly, handle messages from the event handler, if one exists
                // and if it has not completed.
                for (Iterator<EventHandlerInfo> i = _eventHandlers.iterator();i.hasNext();) {
                    final EventHandlerInfo ehi = i.next();

                    mlSet.add(new ParentScopeChannelListener(ehi.psc) {
                        private static final long serialVersionUID = -4694721357537858221L;

                        public void compensate(OScope scope, SynchChannel ret) {
                            // ACTIVE scopes do not compensate, send request up to parent.
                            _self.parent.compensate(scope, ret);
                            instance(ACTIVE.this);
                        }

                        public void completed(FaultData flt, Set<CompensationHandler> compenstations) {
                            // Set the fault to the activity's choice, if and only if no previous fault
                            // has been detected (first fault wins).
                            if (flt != null && _fault == null)
                                _fault = flt;
                            _eventHandlers.remove(ehi);
                            _compensations.addAll(compenstations);

                            if (flt != null) {
                                // Terminate child if we get a fault from the event handler.
                                if (_child != null && !_childTermRequested) {
                                    replication(_child.self).terminate();
                                    _childTermRequested = true;
                                }
                                terminateEventHandlers();
                            } else
                                stopEventHandlers();

                            instance(ACTIVE.this);
                        }

                        public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                        public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                    });
                }
                object(false, mlSet);
            } else /* nothing to wait for... */ {
                // Any compensation handlers that were available but not activated will be forgotten.
                Set<CompensationHandler> unreachableCompensationHandlers = _scopeFrame.availableCompensations;
                if (unreachableCompensationHandlers != null)
                    for (Iterator<CompensationHandler> i = unreachableCompensationHandlers.iterator(); i.hasNext(); ) {
                        CompensationHandler ch = i.next();
                        ch.compChannel.forget();
                    }
                _scopeFrame.availableCompensations = null;

                // Maintain a set of links needing dead-path elimination.
                Set<OLink> linksNeedingDPE = new HashSet<OLink>();
                if (_oscope.faultHandler != null)
                    for (Iterator<OCatch> i = _oscope.faultHandler.catchBlocks.iterator(); i.hasNext(); )
                        linksNeedingDPE.addAll(i.next().outgoingLinks);

                // We're done with the main work, if we were terminated, we will
                // need to load the termination handler:
                if (_terminated) {
                    if (__log.isDebugEnabled()) {
                        __log.debug("Scope: " + _oscope + " was terminated.");
                    }
                    // ??? Should we forward
                    _self.parent.completed(null,_compensations);
                } else if (_fault != null) {

                    sendEvent(new ScopeFaultEvent(_fault.getFaultName(), _fault.getFaultLineNo(),_fault.getExplanation()));

                    // Find a fault handler for our fault.
                    OCatch catchBlock = _oscope.faultHandler == null ? null : findCatch(_oscope.faultHandler, _fault.getFaultName(), _fault.getFaultType());

                    // Collect all the compensation data for completed child scopes.
                    assert !!_eventHandlers.isEmpty();
                    assert _child == null;
                    if (catchBlock == null) {
                        // If we cannot find a catch block for this fault, then we simply propagate the fault
                        // to the parent. NOTE: the "default" fault handler as described in the BPEL spec
                        // must be generated by the compiler.
                          __log.warn(_self + ": has no fault handler for "
                                + _fault.getFaultName() + "; scope will propagate FAULT! , " + _fault.toString());


                        _self.parent.completed(_fault, _compensations);
                    } else /* catchBlock != null */ {
                            __log.warn(_self + ": has a fault handler for "
                                    + _fault.getFaultName() + ": "+ catchBlock + " ,  " + _fault.toString());

                        linksNeedingDPE.removeAll(catchBlock.outgoingLinks);

                        // We have to create a scope for the catch block.
                        BpelRuntimeContext ntive = getBpelRuntimeContext();

                        ActivityInfo faultHandlerActivity = new ActivityInfo(genMonotonic(), catchBlock,
                                newChannel(TerminationChannel.class,"FH"), newChannel(ParentScopeChannel.class,"FH"));

                        ScopeFrame faultHandlerScopeFrame = new ScopeFrame(catchBlock,
                                ntive.createScopeInstance(_scopeFrame.scopeInstanceId, catchBlock),
                                _scopeFrame, _compensations, _fault);
                        if (catchBlock.faultVariable != null) {
                            try {
                                VariableInstance vinst =  faultHandlerScopeFrame.resolve(catchBlock.faultVariable);
                                initializeVariable(vinst, _fault.getFaultMessage());

                                // Generating event
                                VariableModificationEvent se = new VariableModificationEvent(vinst.declaration.name);
                                se.setNewValue(_fault.getFaultMessage());
                                if (_oscope.debugInfo != null)
                                    se.setLineNo(_oscope.debugInfo.startLine);
                                sendEvent(se);
                            } catch (Exception ex) {
                                __log.fatal(ex);
                                throw new InvalidProcessException(ex);
                            }
                        }

                        // Create the fault handler scope.
                        instance(new SCOPE(faultHandlerActivity,faultHandlerScopeFrame, SCOPE.this._linkFrame));

                        object(new ParentScopeChannelListener(faultHandlerActivity.parent) {
                            private static final long serialVersionUID = -6009078124717125270L;

                            public void compensate(OScope scope, SynchChannel ret) {
                                // This should never happen.
                                throw new AssertionError("received compensate request!");
                            }

                            public void completed(FaultData fault, Set<CompensationHandler> compensations) {
                                // The compensations that have been registered here, will never be activated,
                                // so we'll forget them as soon as possible.
                                for (CompensationHandler compensation : compensations)
                                    compensation.compChannel.forget();

                                _self.parent.completed(fault, CompensationHandler.emptySet());
                            }

                            public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                            public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                        });
                    }
                } else /* completed ok */ {
                    sendEvent(new ScopeCompletionEvent());

                    if (_oscope.compensationHandler != null) {
                        CompensationHandler compensationHandler = new CompensationHandler(
                            _scopeFrame,
                            newChannel(CompensationChannel.class),
                            _startTime,
                            System.currentTimeMillis());
                        _self.parent.completed(null, Collections.singleton(compensationHandler));
                        instance(new COMPENSATIONHANDLER_(compensationHandler, _compensations));
                    } else /* no compensation handler */ {
                        _self.parent.completed(null, _compensations);
                    }
                }

                // DPE links needing DPE (i.e. the unselected catch blocks).
                dpe(linksNeedingDPE);
            }
        }

        private void terminateEventHandlers() {
            for (Iterator<EventHandlerInfo> i = _eventHandlers.iterator();i.hasNext(); ) {
                EventHandlerInfo ehi = i.next();
                if (!ehi.terminateRequested && !ehi.stopRequested) {
                    replication(ehi.tc).terminate();
                    ehi.terminateRequested = true;
                }
            }
        }

        private void stopEventHandlers() {
            for (Iterator<EventHandlerInfo> i = _eventHandlers.iterator();i.hasNext();) {
                EventHandlerInfo ehi = i.next();
                if (!ehi.stopRequested && !ehi.terminateRequested) {
                    ehi.cc.stop();
                    ehi.stopRequested = true;
                }
            }
        }

    }


    private static OCatch findCatch(OFaultHandler fh, QName faultName, OVarType faultType) {
        OCatch bestMatch = null;
        for (OCatch c : fh.catchBlocks) {
            // First we try to eliminate this catch block based on fault-name mismatches:
            if (c.faultName != null) {
                if (faultName == null)
                    continue;
                if (!faultName.equals(c.faultName))
                    continue;
            }

            // Then we try to eliminate this catch based on type incompatibility:
            if (c.faultVariable != null) {
                if (faultType == null)
                    continue;
                else if (c.faultVariable.type instanceof OMessageVarType) {
                    if (faultType instanceof OMessageVarType
                            && ((OMessageVarType) faultType).equals(c.faultVariable.type)) {
                        // Don't eliminate.
                    } else if (faultType instanceof OElementVarType
                            && ((OMessageVarType) c.faultVariable.type).docLitType != null
                            && !((OMessageVarType) c.faultVariable.type).docLitType.equals(faultType)) {
                        // Don't eliminate.
                    } else {
                        continue;  // Eliminate.
                    }
                } else if (c.faultVariable.type instanceof OElementVarType) {
                    if (faultType instanceof OElementVarType && faultType.equals(c.faultVariable.type)) {
                        // Don't eliminate
                    } else if (faultType instanceof OMessageVarType
                            && ((OMessageVarType) faultType).docLitType != null
                            && ((OMessageVarType) faultType).docLitType.equals(c.faultVariable.type)) {
                        // Don't eliminate
                    } else {
                        continue; // eliminate
                    }
                } else {
                    continue; // Eliminate
                }
            }

            // If we got to this point we did not eliminate this catch block. However, we don't just
            // use the first non-eliminated catch, we instead try to find the best match.
            if (bestMatch == null) {
                // Obviously something is better then nothing.
                bestMatch = c;
            } else {
                // Otherwise we prefer name and variable matches but prefer name-only matches to
                // variable-only matches.
                int existingScore = (bestMatch.faultName == null ? 0 : 2) + (bestMatch.faultVariable == null ? 0 : 1);
                int currentScore = (c.faultName == null ? 0 : 2) + (c.faultVariable == null ? 0 : 1);
                if (currentScore > existingScore) {
                    bestMatch = c;
                }
            }
        }
        return bestMatch;
    }

    static final class EventHandlerInfo implements Serializable {
        private static final long serialVersionUID = -9046603073542446478L;
        final OBase o;
        final EventHandlerControlChannel cc;
        final ParentScopeChannel psc;
        final TerminationChannel tc;
        boolean terminateRequested;
        boolean stopRequested;

        EventHandlerInfo(OBase o, EventHandlerControlChannel cc, ParentScopeChannel psc, TerminationChannel tc) {
            this.o = o;
            this.cc = cc;
            this.psc = psc;
            this.tc = tc;
        }
    }

}
