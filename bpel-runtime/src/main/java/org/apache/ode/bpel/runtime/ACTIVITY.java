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

import org.apache.ode.bpel.evt.ActivityEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OLink;

import java.util.Collection;
import java.util.Iterator;


/**
 * Base template for activities.
 */
abstract class ACTIVITY extends BpelAbstraction {
  protected ActivityInfo _self;

  /**
   * Permeability flag, if <code>false</code> we defer outgoing links until
   * succesfull completion.
   */
  protected boolean _permeable = true;

  protected ScopeFrame _scopeFrame;

  protected LinkFrame _linkFrame;

  public ACTIVITY(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    assert self != null;
    assert scopeFrame != null;
    assert linkFrame != null;

    _self = self;
    _scopeFrame = scopeFrame;
    _linkFrame = linkFrame;
  }


  protected void sendEvent(ActivityEvent event) {
    event.setActivityName(_self.o.name);
    event.setActivityType(_self.o.getType());
    event.setActivityDeclarationId(_self.o.getId());
    event.setActivityId(_self.aId);
    if (event.getLineNo() == -1) {
      event.setLineNo(getLineNo());
    }
    sendEvent((ScopeEvent)event);
  }

  protected void sendEvent(ScopeEvent event) {
    if (event.getLineNo() == -1 && _self.o.debugInfo != null) {
      event.setLineNo(_self.o.debugInfo.startLine);
    }
    _scopeFrame.fillEventInfo(event);
    getBpelRuntimeContext().sendEvent(event);
  }

  protected void dpe(Collection<OLink> links) {
    // Dead path all of the ougoing links (nothing has been activated yet!)
    for (Iterator<OLink> i = links.iterator(); i.hasNext(); )
      _linkFrame.resolve(i.next()).pub.linkStatus(false);
  }

  /**
   * Perform dead-path elimination on an activity that was <em>not started</em>.
   * @param activity
   */
  protected void dpe(OActivity activity) {
    dpe(activity.sourceLinks);
    dpe(activity.outgoingLinks);
    // TODO: register listeners for target / incoming links
  }

  protected EvaluationContext getEvaluationContext() {
    return new ExprEvaluationContextImpl(_scopeFrame,getBpelRuntimeContext());
  }


  private int getLineNo() {
    if (_self.o.debugInfo != null && _self.o.debugInfo.startLine != -1) {
      return _self.o.debugInfo.startLine;
    }
    return -1;
  }

}
