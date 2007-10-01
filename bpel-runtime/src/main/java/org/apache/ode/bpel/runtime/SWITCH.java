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

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OSwitch;
import org.apache.ode.bpel.runtime.channels.FaultData;

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

  public final void run() {
    OSwitch oswitch = (OSwitch)_self.o;
    OSwitch.OCase matchedOCase = null;
    FaultData faultData = null;
    
    EvaluationContext evalCtx = getEvaluationContext();
    for (Iterator i = oswitch.getCases().iterator(); i.hasNext();) {
      OSwitch.OCase ocase = (OSwitch.OCase) i.next();
      try{
    	  try {
	      	if(getBpelRuntimeContext().getExpLangRuntime().evaluateAsBoolean(ocase.expression, evalCtx)){
	          matchedOCase = ocase;
	          break;
	        }
	      } catch (EvaluationException e) {
	    	  __log.error("Sub-Language execution failure evaluating " + ocase.expression, e);
	        throw new FaultException(oswitch.getOwner().constants.qnSubLanguageExecutionFault, e.getMessage());
	      }
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