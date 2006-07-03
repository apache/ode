/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.jacob.SynchChannel;
import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.explang.EvaluationContext;
import com.fs.pxe.bpel.explang.EvaluationException;
import com.fs.pxe.bpel.o.OEventHandler;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.channels.*;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Alarm event handler. This process template manages a single alarm event handler.
 * It acts like an activity in that it can be terminated, but also adds a channel for
 * "soft" termination (aka stopping) to deal with the case when the scope that owns
 * the event handler completes naturally.
 */
class EH_ALARM extends BpelAbstraction {
	private static final long serialVersionUID = 1L;

	private ParentScopeChannel _psc;
  private TerminationChannel _tc;
  private OEventHandler.OAlarm _oalarm;
  private ScopeFrame _scopeFrame;
  private EventHandlerControlChannel _cc;
  private Set<CompensationHandler> _comps = new HashSet<CompensationHandler>();

  /**
   * Concretion constructor.
   * @param psc a link to our parent.
   * @param tc channel we listen on for termination requests
   * @param cc channel we listen on for "stop" requests
   * @param o our prototype / compiled representation
   * @param scopeFrame the {@link ScopeFrame} in which we are executing
   */
  EH_ALARM(ParentScopeChannel psc, TerminationChannel tc, EventHandlerControlChannel cc, OEventHandler.OAlarm o, ScopeFrame scopeFrame) {
    _psc = psc;
    _tc = tc;
    _cc = cc;
    _scopeFrame = scopeFrame;
    _oalarm  = o;
  }

  public void self() {

    Calendar alarm = Calendar.getInstance();

    if (_oalarm.forExpr != null)
      try {
        getBpelRuntimeContext().getExpLangRuntime().evaluateAsDuration(_oalarm.forExpr, getEvaluationContext()).addTo(alarm);
      } catch (EvaluationException e) {
        throw new InvalidProcessException(e);
      } catch (FaultException e) {
        _psc.completed(createFault(e.getQName(),_oalarm.forExpr), _comps);
        return;
      }
    else if (_oalarm.untilExpr != null)
      try {
        alarm.setTime(getBpelRuntimeContext().getExpLangRuntime().evaluateAsDate(_oalarm.untilExpr, getEvaluationContext()).getTime());
      } catch (EvaluationException e) {
        throw new InvalidProcessException(e);
      } catch (FaultException e) {
        _psc.completed(createFault(e.getQName(),_oalarm.untilExpr), _comps);
        return;
      }

    // We reduce to waiting for the alarm to be triggered.
    instance(new WAIT(alarm));
  }

  protected EvaluationContext getEvaluationContext() {
    return new ExprEvaluationContextImpl(_scopeFrame,getBpelRuntimeContext());
  }

  /**
   * Template used to wait until a given time, reduing to a {@link FIRE} after the
   * elapsed time. This template also monitors the termination and event-control channels
   * for requests from parent.
   */
  private class WAIT extends BpelAbstraction {
    private static final long serialVersionUID = -1426724996925898213L;
    Calendar _alarm;

    /**
     * Concretion constructor.
     * @param alarm date at which time to fire
     */
    WAIT(Calendar alarm) {
      _alarm = alarm;
    }

    public void self() {
      Calendar now = Calendar.getInstance();

      if (now.before(_alarm)) {
        TimerResponseChannel trc = newChannel(TimerResponseChannel.class);
        getBpelRuntimeContext().registerTimer(trc,_alarm.getTime());
        object(false,new TimerResponseML(trc){
        private static final long serialVersionUID = 1110683632756756017L;

        public void onTimeout() {
            // This is what we are waiting for, fire the activity
            instance(new FIRE());
          }

          public void onCancel() {
            _psc.completed(null, _comps);
          }
        }.or(new EventHandlerControlML(_cc) {
        private static final long serialVersionUID = -7750428941445331236L;

        public void stop() {
            _psc.completed(null, _comps);
          }

        }.or(new TerminationML(_tc) {
        private static final long serialVersionUID = 6100105997983514609L;

        public void terminate() {
            _psc.completed(null, _comps);
          }
        })));
      } else /* now is later then alarm time */ {
        // If the alarm has passed we fire the nested activity
        instance(new FIRE());
      }

    }
  }

  /**
   * Snipped that fires the alarm activity.
   */
  private class FIRE extends BpelAbstraction {
    private static final long serialVersionUID = -7261315204412433250L;

    public void self() {
      // Start the child activity.
      ActivityInfo child = new ActivityInfo(genMonotonic(),
              _oalarm.activity,
              newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
      instance(createChild(child, _scopeFrame, new LinkFrame(null) ));
      instance(new ACTIVE(child));
    }
  }

  /**
   * Snippet that is used to monitor a running activity.
   */
  private class ACTIVE extends BpelAbstraction {
    private static final long serialVersionUID = -2166253425722769701L;

    private ActivityInfo _activity;

    /** Indicates whether our parent has requested a stop. */
    private boolean _stopped = false;

    ACTIVE(ActivityInfo activity) {
      _activity = activity;
    }

    public void self() {
      object(false,new ParentScopeML(_activity.parent){
        private static final long serialVersionUID = -3357030137175178040L;

        public void compensate(OScope scope, SynchChannel ret) {
          _psc.compensate(scope,ret);
          instance(ACTIVE.this);
        }

        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
          _comps.addAll(compensations);
          if (!_stopped && _oalarm.repeatExpr != null) {
            Calendar next = Calendar.getInstance();
            try {
              getBpelRuntimeContext().getExpLangRuntime().evaluateAsDuration(_oalarm.forExpr, getEvaluationContext()).addTo(next);
            } catch (EvaluationException e) {
              throw new InvalidProcessException(e);
            } catch (FaultException e) {
              _psc.completed(createFault(e.getQName(),_oalarm.forExpr), _comps);
              return;
            }
            instance(new WAIT(next));
          } else {
            _psc.completed(faultData, _comps);
          }
        }

      }.or(new EventHandlerControlML(_cc) {
        private static final long serialVersionUID = -3873619538789039424L;

        public void stop() {
          _stopped = true;
          instance(ACTIVE.this);
        }

      }.or(new TerminationML(_tc) {
        private static final long serialVersionUID = -4566956567870652885L;

        public void terminate() {
          replication(_activity.self).terminate();
          _stopped = true;
          instance(ACTIVE.this);
        }
      })));

    }
  }
}
