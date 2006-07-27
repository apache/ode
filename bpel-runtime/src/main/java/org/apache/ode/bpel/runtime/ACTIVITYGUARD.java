/*
 * File:      $RCSfile: ACTIVITYGUARD.java,v $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import com.fs.jacob.ML;
import com.fs.jacob.SynchChannel;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ActivityEnabledEvent;
import org.apache.ode.bpel.evt.ActivityExecEndEvent;
import org.apache.ode.bpel.evt.ActivityExecStartEvent;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class ACTIVITYGUARD extends ACTIVITY {
	private static final long serialVersionUID = 1L;

	private static final Log __log = LogFactory.getLog(ACTIVITYGUARD.class);

  private static final ActivityTemplateFactory __activityTemplateFactory = new ActivityTemplateFactory();
  private OActivity _oactivity;

  /** Link values. */
  private Map<OLink, Boolean> _linkVals = new HashMap<OLink, Boolean>();
  
  /** Flag to prevent duplicate ActivityEnabledEvents */
  private boolean _firstTime = true;

  public ACTIVITYGUARD(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
    _oactivity = self.o;
  }

  public void self() {
    // Send a notification of the activity being enabled, 
    if (_firstTime) {
      sendEvent(new ActivityEnabledEvent());
      _firstTime = false;
    }
    
    if (_linkVals.keySet().containsAll(_oactivity.targetLinks)) {
      if (evaluateJoinCondition()) {

        ActivityExecStartEvent aese = new ActivityExecStartEvent();
        sendEvent(aese);
        // intercept completion channel in order to execute transition conditions.
        ActivityInfo activity = new ActivityInfo(genMonotonic(),_self.o,_self.self, newChannel(ParentScopeChannel.class));
        instance(createActivity(activity));
        instance(new TCONDINTERCEPT(activity.parent));
      } else {
        // Join Failure.
        _self.parent.completed(createFault(_oactivity.getOwner().constants.qnJoinFailure,_oactivity),
                CompensationHandler.emptySet());

        // Dead path activity.
        dpe(_oactivity);
      }
    } else /* don't know all our links statuses */ {
      Set<ML> mlset = new HashSet<ML>();
      mlset.add(new TerminationML(_self.self) {
        private static final long serialVersionUID = 5094153128476008961L;

        public void terminate() {
          // Complete immediately, without faulting or registering any comps.
          _self.parent.completed(null, CompensationHandler.emptySet());

          // Dead-path activity
          dpe(_oactivity);
        }
      });
      for (Iterator<OLink> i = _oactivity.targetLinks.iterator();i.hasNext();) {
        final OLink link = i.next();
        mlset.add(new LinkStatusML(_linkFrame.resolve(link).sub) {
        private static final long serialVersionUID = 1024137371118887935L;

        public void linkStatus(boolean value) {
            _linkVals.put(link, Boolean.valueOf(value));
            instance(ACTIVITYGUARD.this);
          }
        });
      }

      object(false, mlset);
    }
  }


  private boolean evaluateTransitionCondition(OExpression transitionCondition)
          throws FaultException {
    if (transitionCondition == null)
      return true;

    try {
      return getBpelRuntimeContext().getExpLangRuntime().evaluateAsBoolean(transitionCondition,
              new ExprEvaluationContextImpl(_scopeFrame, getBpelRuntimeContext()));
    } catch (EvaluationException e) {
      String msg = "Error in transition condition detected at runtime; condition=" + transitionCondition;
      __log.error(msg,e);
      throw new InvalidProcessException(msg, e);
    }
  }

  /**
   * Evaluate an activity's join condition.
   * @return <code>true</code> if join condition evaluates to true.
   */
  private boolean evaluateJoinCondition() {
    // For activities with no link targets, the join condition is always satisfied.
    if (_oactivity.targetLinks.size() == 0)
      return true;

    // For activities with no join condition, an OR condition is assumed.
    if (_oactivity.joinCondition == null)
      return _linkVals.values().contains(Boolean.TRUE);

    try {
      return getBpelRuntimeContext().getExpLangRuntime().evaluateAsBoolean(_oactivity.joinCondition,
              new ExprEvaluationContextImpl(null,null,_linkVals));
    } catch (Exception e) {
      String msg = "Unexpected error evaluating a join condition: " + _oactivity.joinCondition;
      __log.error(msg,e);
      throw new InvalidProcessException(msg,e);
    }
  }

  private static ACTIVITY createActivity(ActivityInfo activity, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    return __activityTemplateFactory.createInstance(activity.o,activity, scopeFrame, linkFrame);
  }

  private ACTIVITY createActivity(ActivityInfo activity) {
    return createActivity(activity,_scopeFrame, _linkFrame);
  }


  /**
   * Intercepts the
   * {@link ParentScopeChannel#completed(org.apache.ode.bpel.runtime.channels.FaultData, java.util.Set<org.apache.ode.bpel.runtime.CompensationHandler>)}
   * call, to evaluate transition conditions before returning to the parent.
   */
  private class TCONDINTERCEPT extends BpelAbstraction {
    private static final long serialVersionUID = 4014873396828400441L;
    ParentScopeChannel _in;

    public TCONDINTERCEPT(ParentScopeChannel in) {
      _in = in;
    }

    public void self() {
      object(new ParentScopeML(_in) {
        private static final long serialVersionUID = 2667359535900385952L;

        public void compensate(OScope scope, SynchChannel ret) {
          _self.parent.compensate(scope,ret);
          instance(TCONDINTERCEPT.this);
        }

        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
        	sendEvent(new ActivityExecEndEvent());
          if (faultData != null) {
            dpe(_oactivity.sourceLinks);
            _self.parent.completed(faultData, compensations);
          } else {
            FaultData fault = null;
            for (Iterator<OLink> i = _oactivity.sourceLinks.iterator();i.hasNext();) {
              OLink olink = i.next();
              LinkInfo linfo = _linkFrame.resolve(olink);
              try {
                boolean val = evaluateTransitionCondition(olink.transitionCondition);
                linfo.pub.linkStatus(val);
              } catch (FaultException e) {
                linfo.pub.linkStatus(false);
                if (fault == null)
                  fault = createFault(e.getQName(),olink.transitionCondition);
              }
            }
            _self.parent.completed(fault, compensations);
          }
        }
      });

    }
  }
}
