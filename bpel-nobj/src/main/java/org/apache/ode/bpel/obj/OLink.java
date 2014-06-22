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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of a BPEL control link.
 */
public class OLink extends OBase {
	/** The flow in which the link is declared. */
	private static final String DECLARINGFLOW = "declaringFlow";

	/** The name of the link. */
	private static final String NAME = "name";

	/** The link's transition condition. */
	private static final String TRANSITIONCONDITION = "transitionCondition";

	/** The source activity. */
	private static final String SOURCE = "source";

	/** The target activity. */
	private static final String TARGET = "target";

	public OLink(OProcess owner) {
		super(owner);
	}

	@JsonIgnore
	public OFlow getDeclaringFlow() {
		return (OFlow) fieldContainer.get(DECLARINGFLOW);
	}

	@JsonIgnore
	public String getName() {
		return (String) fieldContainer.get(NAME);
	}

	@JsonIgnore
	public OActivity getSource() {
		return (OActivity) fieldContainer.get(SOURCE);
	}

	@JsonIgnore
	public OActivity getTarget() {
		return (OActivity) fieldContainer.get(TARGET);
	}

	@JsonIgnore
	public OExpression getTransitionCondition() {
		return (OExpression) fieldContainer.get(TRANSITIONCONDITION);
	}

	public void setDeclaringFlow(OFlow declaringFlow) {
		fieldContainer.put(DECLARINGFLOW, declaringFlow);
	}

	public void setName(String name) {
		fieldContainer.put(NAME, name);
	}

	public void setSource(OActivity source) {
		fieldContainer.put(SOURCE, source);
	}

	public void setTarget(OActivity target) {
		fieldContainer.put(TARGET, target);
	}

	public void setTransitionCondition(OExpression transitionCondition) {
		fieldContainer.put(TRANSITIONCONDITION, transitionCondition);
	}
}
