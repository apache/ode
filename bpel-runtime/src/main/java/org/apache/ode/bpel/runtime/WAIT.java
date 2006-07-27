/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OWait;
import org.apache.ode.bpel.runtime.channels.TerminationML;
import org.apache.ode.bpel.runtime.channels.TimerResponseChannel;
import org.apache.ode.bpel.runtime.channels.TimerResponseML;
import org.apache.ode.utils.xsd.Duration;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstraction that performs the work of the <code>&lt;wait&gt;</code> activity.
 */
class WAIT extends ACTIVITY {
	private static final long serialVersionUID = 1L;
	private static final Log __log = LogFactory.getLog(WAIT.class);

  WAIT(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public final void self() {
    
  	Date dueDate = null;
  	try{
  		dueDate = getDueDate();
  	}catch(FaultException e){
  		__log.error("Fault while calculating due date: " 
  				+ e.getQName() 
  				+ "; Reason: " + e.getMessage());
  		_self.parent.completed(createFault(e.getQName(), _self.o), CompensationHandler.emptySet());
  		return;
  	} catch (EvaluationException ee) {
      String msg = "Unexpected error evaluating wait condition.";
      __log.error(msg, ee);
      throw new InvalidProcessException(msg,ee);
    }
  	

  	if(dueDate.getTime() > System.currentTimeMillis()){
	    final TimerResponseChannel timerChannel = newChannel(TimerResponseChannel.class);
	    getBpelRuntimeContext().registerTimer(timerChannel, dueDate);
	    
	    object(false, new TimerResponseML(timerChannel){
         private static final long serialVersionUID = 3120518305645437327L;

         public void onTimeout() {
           _self.parent.completed(null, CompensationHandler.emptySet());
         }

        public void onCancel() {
          _self.parent.completed(null, CompensationHandler.emptySet());
        }
      }.or(new TerminationML(_self.self) {
        private static final long serialVersionUID = -2791243270691333946L;

        public void terminate() {
          _self.parent.completed(null, CompensationHandler.emptySet());
          object(new TimerResponseML(timerChannel) {
            private static final long serialVersionUID = 677746737897792929L;

            public void onTimeout() {
              //ignore
            }

            public void onCancel() {
              //ingore
            }
          });
        }
      }));
    }else{
      _self.parent.completed(null, null);
    }
  }


  protected Date getDueDate() throws FaultException, EvaluationException {

    OWait wait = (OWait)_self.o;

    // Assume the data was well formed (we have a deadline or a duration)
    assert wait.hasFor() || wait.hasUntil();

    EvaluationContext evalCtx = getEvaluationContext();

    Date dueDate = null;
    if (wait.hasFor()) {
      Calendar cal = Calendar.getInstance();
      Duration duration = getBpelRuntimeContext().getExpLangRuntime().evaluateAsDuration(wait.forExpression, evalCtx);
      duration.addTo(cal);
      dueDate = cal.getTime();
    } else if (wait.hasUntil()) {
      Calendar cal = getBpelRuntimeContext().getExpLangRuntime().evaluateAsDate(wait.untilExpression, evalCtx);
      dueDate = cal.getTime();
    } else {
      throw new AssertionError("Static checks failed to find bad WaitActivity!");
    }

    // For now if we cannot evaluate a due date, we assume it is due now.
    // TODO: BPEL-ISSUE: verify BPEL spec for proper handling of these errors
    if (dueDate == null)
      dueDate = new Date();

    return dueDate;
  }

}
