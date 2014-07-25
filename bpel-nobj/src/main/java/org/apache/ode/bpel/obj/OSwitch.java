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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of a BPEL <code>&lt;switch&gt;</code>. The
 * BPEL compiler generates instances of this class.
 */
public class OSwitch extends OActivity  implements Serializable{
	public static final long serialVersionUID = -1L;

	/**
	 * The cases declared within the <code>&lt;switch&gt;</code> actvity.
	 */
	private static final String CASES = "_cases";

	public OSwitch(){}
	public OSwitch(OProcess owner, OActivity parent) {
		super(owner, parent);
		setCases(new ArrayList<OCase>());
	}

	@SuppressWarnings("unchecked")
	public void addCase(OCase acase) {
		((List<OCase>)fieldContainer.get(CASES)).add(acase);
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OCase> getCases() {
		return (List<OCase>)fieldContainer.get(CASES);
	}
	private void setCases(List<OCase> cases){
		fieldContainer.put(CASES, cases);
	}

	public static class OCase extends OBase  implements Serializable{
	public static final long serialVersionUID = -1L;
		private static final String EXPRESSION = "expression";
		private static final String ACTIVITY = "activity";

		@JsonCreator
		public OCase(){}
		public OCase(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public OActivity getActivity() {
			Object o = fieldContainer.get(ACTIVITY);
		return o == null ? null : (OActivity)o;
		}

		@JsonIgnore
		public OExpression getExpression() {
			Object o = fieldContainer.get(EXPRESSION);
		return o == null ? null : (OExpression)o;
		}

		public void setActivity(OActivity activity) {
			fieldContainer.put(ACTIVITY, activity);
		}

		public void setExpression(OExpression expression) {
			fieldContainer.put(EXPRESSION, expression);
		}
	}
}