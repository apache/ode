/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import com.fs.jacob.ML;
import com.fs.jacob.SynchChannel;
import org.apache.ode.bpel.evt.ScopeFaultEvent;
import org.apache.ode.bpel.evt.ScopeStartEvent;
import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.runtime.channels.*;

import java.io.Serializable;
import java.util.*;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

  public void self() {
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
    // TODO: sort out in terms of completion order
    return out;
  }


  class ACTIVE extends ACTIVITY {
    private static final long serialVersionUID = -5876892592071965346L;
    /** Links collected. */
    private boolean _terminated;
    private FaultData _fault;
    private long _startTime;
    private final HashSet<CompensationHandler> _compensations = new HashSet<CompensationHandler>();
    private boolean _childCompleted;
    private boolean _childTermRequested;

    ACTIVE() {
      super(SCOPE.this._self, SCOPE.this._scopeFrame, SCOPE.this._linkFrame);
      _startTime = System.currentTimeMillis();
    }

    public void self() {
      if (_child != null || !_eventHandlers.isEmpty()) {
        HashSet<ML> mlSet = new HashSet<ML>();

        // Listen to messages from our parent.
        mlSet.add(new TerminationML(_self.self) {
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
        if (_child != null)
          mlSet.add(new ParentScopeML(_child.parent) {
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


            public void completed(FaultData flt, Set<CompensationHandler> compenstations) {
              // Set the fault to the activity's choice, if and only if no previous fault
              // has been detected (first fault wins).
              if (flt != null && _fault == null)
                _fault = flt;
              _child = null;
              _compensations.addAll(compenstations);

              if (flt == null)
                stopEventHandlers();
              else
                terminateEventHandlers();

              instance(ACTIVE.this);
            }
          });

        // Similarly, handle messages from the event handler, if one exists
        // and if it has not completed.
        for (Iterator<EventHandlerInfo> i = _eventHandlers.iterator();i.hasNext();) {
          final EventHandlerInfo ehi = i.next();

          mlSet.add(new ParentScopeML(ehi.psc) {
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
        // need to activate the termination handler:
        if (_terminated) {
//          throw new UnsupportedOperationException("termination handler todo ");
        }

        // else-if we have a fault, we will need to activate the fault handler.
        else if (_fault != null) {

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
            if (__log.isDebugEnabled())
              __log.debug(_self + ": has no fault handler for "
                      + _fault.getFaultName() + "; scope will propagate FAULT!");


            _self.parent.completed(_fault, _compensations);
          } else /* catchBlock != null */ {
            if (__log.isDebugEnabled()) __log.debug(_self + ": has a fault handler for "
                    + _fault.getFaultName() + ": "+ catchBlock);

            linksNeedingDPE.removeAll(catchBlock.outgoingLinks);

            // We have to create a scope for the catch block.
            BpelRuntimeContext ntive = getBpelRuntimeContext();

            ActivityInfo faultHandlerActivity = new ActivityInfo(genMonotonic(),
                    catchBlock,
                    newChannel(TerminationChannel.class,"FH"), newChannel(ParentScopeChannel.class,"FH"));

            ScopeFrame faultHandlerScopeFrame = new ScopeFrame(catchBlock,
                    ntive.createScopeInstance(_scopeFrame.scopeInstanceId, catchBlock),
                    _scopeFrame,
                    _compensations,
                    _fault);
            if (catchBlock.faultVariable != null)
              try {
                ntive.initializeVariable(faultHandlerScopeFrame.resolve(catchBlock.faultVariable),
                        _fault.getFaultMessage());
              } catch (Exception ex) {
                __log.fatal(ex);
                throw new InvalidProcessException(ex);
              }


            // Create the fault handler scope.
            instance(new SCOPE(faultHandlerActivity,faultHandlerScopeFrame, SCOPE.this._linkFrame));

            object(new ParentScopeML(faultHandlerActivity.parent) {
            private static final long serialVersionUID = -6009078124717125270L;

            public void compensate(OScope scope, SynchChannel ret) {
                // This should never happen.
                throw new AssertionError("received compensate request!");
              }

              public void completed(FaultData fault, Set<CompensationHandler> compensations) {
                // The compensations that have been registered here, will never be activated,
                // so we'll forget them as soon as possible.
                for(Iterator<CompensationHandler> i = compensations.iterator();i.hasNext();)
                  i.next().compChannel.forget();

                _self.parent.completed(fault, CompensationHandler.emptySet());
              }
            });
          }
        } else /* completed ok */ {
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
    for (Iterator<OCatch> i = fh.catchBlocks.iterator(); i.hasNext();) {
      OCatch c = i.next();

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
              && ((OMessageVarType)faultType).equals(c.faultVariable.type)) {
            // Don't eliminate.
          }
          else if (faultType instanceof OElementVarType
                  && ((OMessageVarType)c.faultVariable.type).docLitType !=null
                  && !((OMessageVarType)c.faultVariable.type).docLitType.equals(faultType)) {
            // Don't eliminate.
          }
          else {
            continue;  // Eliminate.
          }
        } else if (c.faultVariable.type instanceof OElementVarType) {
          if (faultType instanceof OElementVarType && faultType.equals(c.faultVariable.type)) {
            // Don't eliminate
          }
          else if (faultType instanceof OMessageVarType
                  && ((OMessageVarType)faultType).docLitType != null
                  && ((OMessageVarType)faultType).docLitType.equals(c.faultVariable.type)) {
            // Don't eliminate
          }
          else {
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