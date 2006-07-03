/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.jacob.ML;
import com.fs.jacob.SynchChannel;
import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.o.OEventHandler;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.channels.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Message event handler.
 */
class EH_EVENT extends BpelAbstraction {

	private static final long serialVersionUID = 1L;

	private static final Log __log = LogFactory.getLog(EH_EVENT.class);

  private EventHandlerControlChannel _ehc;
  private TerminationChannel _tc;
  private ParentScopeChannel _psc;
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


  EH_EVENT(ParentScopeChannel psc,TerminationChannel tc, EventHandlerControlChannel ehc, OEventHandler.OEvent o, ScopeFrame scopeFrame) {
    _scopeFrame = scopeFrame;
    _oevent = o;
    _tc = tc;
    _psc = psc;
    _ehc = ehc;
  }


  public void self() {
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
  class SELECT extends BpelAbstraction {

		private static final long serialVersionUID = 1L;

		/**
     * @see com.fs.jacob.Abstraction#self()
     */
    public void self() {
      Selector selector;
      try {
        PickResponseChannel pickResponseChannel = newChannel(PickResponseChannel.class);
        CorrelationKey key;
        PartnerLinkInstance pLinkInstance = _scopeFrame.resolve(_oevent.partnerLink);
        if (_oevent.matchCorrelation == null) {
          // Adding a route for opaque correlation. In this particular case, correlation is done
          // on the epr session identifier.
          if (!getBpelRuntimeContext().isEndpointReferenceInitialized(pLinkInstance, true))
            throw new FaultException(_oevent.getOwner().constants.qnCorrelationViolation,
                    "Endpoint reference for myRole on partner link " + _oevent.partnerLink + " has never been" +
                    "initialized even though it's necessary for opaque correlations to work.");
          String sessionId = getBpelRuntimeContext().fetchEndpointSessionId(pLinkInstance, true);
          key = new CorrelationKey(-1, new String[] {sessionId});
        } else {
          if (!getBpelRuntimeContext().isCorrelationInitialized(_scopeFrame.resolve(_oevent.matchCorrelation))) {
            throw new FaultException(_oevent.getOwner().constants.qnCorrelationViolation,"Correlation not initialized.");
          }
          key = getBpelRuntimeContext().readCorrelation(_scopeFrame.resolve(_oevent.matchCorrelation));
          assert key != null;
        }

        selector =  new Selector(0,pLinkInstance,_oevent.operation.getName(), _oevent.operation.getOutput() == null, _oevent.messageExchangeId, key);
        getBpelRuntimeContext().select(pickResponseChannel, null, false, new Selector[] { selector} );
        instance(new WAITING(pickResponseChannel));
      } catch(FaultException e){
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
  private class WAITING extends BpelAbstraction {
		private static final long serialVersionUID = 1L;
		private PickResponseChannel _pickResponseChannel;

    private WAITING(PickResponseChannel pickResponseChannel) {
      _pickResponseChannel = pickResponseChannel;
    }

    public void self() {

      if (!_active.isEmpty() || _pickResponseChannel != null) {
        HashSet<ML> mlset = new HashSet<ML>();

        if (!_terminated) {
          mlset.add(new TerminationML(_tc) {
            private static final long serialVersionUID = 7666910462948788042L;

            public void terminate() {
              terminateActive();
              _terminated = true;
              if (_pickResponseChannel != null)
                getBpelRuntimeContext().cancel(_pickResponseChannel);
              instance(WAITING.this);
            }
          });

        }

        if (!_stopped) {
          mlset.add(new EventHandlerControlML(_ehc) {
            private static final long serialVersionUID = -1050788954724647970L;

            public void stop() {
              _stopped = true;
              if (_pickResponseChannel != null)
                getBpelRuntimeContext().cancel(_pickResponseChannel);
              instance(WAITING.this);
            }
          });

        }

        for (final ActivityInfo ai : _active) {
          mlset.add(new ParentScopeML(ai.parent) {
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
              }

              instance(WAITING.this);
            }
          });
        }

        if (_pickResponseChannel != null)
          mlset.add(new PickResponseML(_pickResponseChannel) {
            private static final long serialVersionUID = -4929999153478677288L;


            public void onRequestRcvd(int selectorIdx, String mexId) {
              Element msgEl = getBpelRuntimeContext().getMyRequest(mexId);
              try {
                getBpelRuntimeContext().initializeVariable(_scopeFrame.resolve(_oevent.variable),msgEl);
              } catch (Exception ex) {
                __log.error(ex);
                throw new InvalidProcessException(ex);
              }

              try {
                for (OScope.CorrelationSet cset : _oevent.initCorrelations) {
                  initializeCorrelation(_scopeFrame.resolve(cset), _scopeFrame.resolve(_oevent.variable));
                }
                if (_oevent.partnerLink.hasPartnerRole()) {
                  // Trying to initialize partner epr based on a message-provided epr/session.
                  Node fromEpr = getBpelRuntimeContext().getSourceEPR(mexId);
                  if (fromEpr != null) {
                    // TODO: EPR fixme 
                    throw new AssertionError("todo");
//                    getBpelRuntimeContext().updatePartnerEndpointReference(
//                            _scopeFrame.resolve(_oevent.partnerLink), fromEpr);
                  }
                }
              } catch (FaultException e) {
                if (_fault == null) {
                  _fault = createFault(e.getQName(), _oevent);
                  terminateActive();
                }
                instance(new WAITING(null));
                return;
              }

              // activate 'onMessage' activity; we'll do this even if a stop/terminate has been
              // requested becasue we cannot undo the receipt of the message at this point.
              ActivityInfo child = new ActivityInfo(genMonotonic(),
                      _oevent.activity,
                      newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
              _active.add(child);
              // If we previously terminated the other activiites, then we do the same
              // here; this is easier then undoing the receive.
              if (_childrenTerminated)
                replication(child.self).terminate();
              instance(createChild(child,_scopeFrame, new LinkFrame(null)));

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
          });

        object(false, mlset);
      } else /* Nothing more to do. */ {
        _psc.completed(_fault, _comps);
      }
    }

  }
}