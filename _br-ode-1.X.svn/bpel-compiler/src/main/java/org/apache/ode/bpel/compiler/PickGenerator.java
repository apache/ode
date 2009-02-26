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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.OnAlarm;
import org.apache.ode.bpel.compiler.bom.OnMessage;
import org.apache.ode.bpel.compiler.bom.PickActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OPickReceive;


/**
 * Generates code for <code>&lt;pick&gt;</code> activities.
 */
class PickGenerator extends PickReceiveGenerator {

    public OActivity newInstance(Activity src) {
        return new OPickReceive(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity src) {
        OPickReceive opick = (OPickReceive) output;
        PickActivity pickDef = (PickActivity) src;

	    opick.createInstanceFlag = pickDef.isCreateInstance();
	    for (OnMessage sOnMessage : pickDef.getOnMessages()) {
	      OPickReceive.OnMessage oOnMessage = compileOnMessage(sOnMessage.getVariable(),
	              sOnMessage.getPartnerLink(),
	              sOnMessage.getOperation(),
	              sOnMessage.getMessageExchangeId(),
	              sOnMessage.getPortType(),
	              pickDef.isCreateInstance(),
	              sOnMessage.getCorrelations(),
	              sOnMessage.getRoute());
	      if (sOnMessage.getActivity() == null)
	          throw new CompilationException(__cmsgs.errEmptyOnMessage().setSource(sOnMessage));
	      oOnMessage.activity = _context.compile(sOnMessage.getActivity());
	      opick.onMessages.add(oOnMessage);
	    }
	
	    try {
	        for(OnAlarm onAlarmDef : pickDef.getOnAlarms()){
	            OPickReceive.OnAlarm oalarm = new OPickReceive.OnAlarm(_context.getOProcess());
	            oalarm.activity = _context.compile(onAlarmDef.getActivity());
	            if (onAlarmDef.getFor() != null && onAlarmDef.getUntil() == null) {
	                oalarm.forExpr = _context.compileExpr(onAlarmDef.getFor());
	            } else if (onAlarmDef.getFor() == null && onAlarmDef.getUntil() != null) {
	                oalarm.untilExpr = _context.compileExpr(onAlarmDef.getUntil());
	            } else {
	                throw new CompilationException(__cmsgs.errForOrUntilMustBeGiven().setSource(onAlarmDef));
	            }
	
	            if (pickDef.isCreateInstance())
	                throw new CompilationException(__cmsgs.errOnAlarmWithCreateInstance().setSource(onAlarmDef));
	
	            opick.onAlarms.add(oalarm);
	        }
	    } catch (CompilationException ce) {
	        _context.recoveredFromError(pickDef, ce);
	    }
    }
}
