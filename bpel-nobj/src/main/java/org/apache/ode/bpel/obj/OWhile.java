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

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of a <code>&lt;while&gt;</code> activity.
 */
public class OWhile extends OActivity implements Serializable{
	private static final long serialVersionUID = -1L;

	/** The while condition. */
	private static final String WHILECONDITION = "whileCondition";

	private static final String ACTIVITY = "activity";

	@JsonCreator
	public OWhile(){}
	public OWhile(OProcess owner, OActivity parent) {
		super(owner, parent);
	}

	@JsonIgnore
	public OActivity getActivity() {
		Object o = fieldContainer.get(ACTIVITY);
		return o == null ? null : (OActivity)o;
	}

	@JsonIgnore
	public OExpression getWhileCondition() {
		Object o = fieldContainer.get(WHILECONDITION);
		return o == null ? null : (OExpression)o;
	}

	public void setActivity(OActivity activity) {
		fieldContainer.put(ACTIVITY, activity);
	}

	public void setWhileCondition(OExpression whileCondition) {
		fieldContainer.put(WHILECONDITION, whileCondition);
	}
}
