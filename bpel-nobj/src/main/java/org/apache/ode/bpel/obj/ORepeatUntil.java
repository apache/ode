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
package org.apache.ode.bpel.obj;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of a <code>&lt;repeatUntil&gt;</code> activity.
 *
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class ORepeatUntil extends OActivity {

	/** The repeat until condition. */
	private static final String UNTILCONDITION = "untilCondition";

	private static final String ACTIVITY = "activity";

	@JsonCreator
	public ORepeatUntil(){}
	public ORepeatUntil(OProcess owner, OActivity parent) {
		super(owner, parent);
	}

	@JsonIgnore
	public OActivity getActivity() {
		Object o = fieldContainer.get(ACTIVITY);
		return o == null ? null : (OActivity)o;
	}

	@JsonIgnore
	public OExpression getUntilCondition() {
		Object o = fieldContainer.get(UNTILCONDITION);
		return o == null ? null : (OExpression)o;
	}

	public void setActivity(OActivity activity) {
		fieldContainer.put(ACTIVITY, activity);
	}

	public void setUntilCondition(OExpression untilCondition) {
		fieldContainer.put(UNTILCONDITION, untilCondition);
	}
}
