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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.OnAlarm;
import org.apache.ode.utils.NSContext;

/**
 * BPEL object model representation of an <code>onAlarm</code> decleration.
 */
public class OnAlarmImpl extends ScopeImpl implements OnAlarm {

  private static final long serialVersionUID = -1L;

  private Expression _for;
  private Expression _until;
  private Expression _repeatEvery;
  private Activity _activity;

  public OnAlarmImpl() {
    super();
  }

  public String getType() {
    return "onAlarm";
  }

  public OnAlarmImpl(NSContext nsContext) {
    super(nsContext);
  }

  public Activity getActivity() {
    return _activity;
  }

  public void setActivity(org.apache.ode.bom.api.Activity activity) {
    _activity = activity;
  }

  public void setFor(Expression for1) {
    _for = for1;
  }

  public Expression getFor() {
    return _for;
  }

  public void setUntil(Expression until) {
    _until = until;
  }

  public Expression getUntil() {
    return _until;
  }

	public Expression getRepeatEvery() {
		return _repeatEvery;
  }
  
	public void setRepeatEvery(Expression repeatEvery) {
		_repeatEvery = repeatEvery;
	}
  
}
