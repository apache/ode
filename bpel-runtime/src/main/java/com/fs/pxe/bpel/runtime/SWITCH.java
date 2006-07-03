/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.explang.EvaluationContext;
import com.fs.pxe.bpel.explang.EvaluationException;
import com.fs.pxe.bpel.o.OSwitch;
import com.fs.pxe.bpel.runtime.channels.FaultData;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Runtime implementation of the <code>&lt;switch&gt;</code> activity.
 */
class SWITCH extends ACTIVITY {
	private static final long serialVersionUID = 1L;
	private static final Log __log = LogFactory.getLog(SWITCH.class);

  public SWITCH(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public final void self() {
    OSwitch oswitch = (OSwitch)_self.o;
    OSwitch.OCase matchedOCase = null;
    FaultData faultData = null;
    
    EvaluationContext evalCtx = getEvaluationContext();
    for (Iterator i = oswitch.getCases().iterator(); i.hasNext();) {
      OSwitch.OCase ocase = (OSwitch.OCase) i.next();
      try{
      	if(getBpelRuntimeContext().getExpLangRuntime().evaluateAsBoolean(ocase.expression, evalCtx)){
          matchedOCase = ocase;
          break;
        }
      } catch (EvaluationException e) {
        String msg = "Unexpected evaluation exception.";
        __log.error(msg,e);
        // TODO: Better location information.
        throw new InvalidProcessException(msg,e);
      }catch(FaultException e){
      	__log.error(e.getMessage(),e);
        faultData = createFault(e.getQName(), ocase);
        _self.parent.completed(faultData, CompensationHandler.emptySet());

        // Dead path all the child activiites:
        for (Iterator<OSwitch.OCase> j = oswitch.getCases().iterator(); j.hasNext(); )
          dpe(j.next().activity);
        return;
      }
    }

    // Dead path cases not chosen
    for (Iterator<OSwitch.OCase> i = oswitch.getCases().iterator(); i.hasNext(); ) {
      OSwitch.OCase cs = i.next();
      if (cs != matchedOCase)
        dpe(cs.activity);
    }

    // no conditions satisfied, we're done.
    if (matchedOCase == null) {
      _self.parent.completed(null, CompensationHandler.emptySet());
    } else /* matched case */ {
      // Re-use our current channels.
      ActivityInfo child = new ActivityInfo(genMonotonic(),matchedOCase.activity, _self.self, _self.parent);
      instance(createChild(child,_scopeFrame,_linkFrame));
    }
  }
}