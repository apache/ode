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
 * Wait object
 */
public class OWait extends OActivity {
	private static final String FOREXPRESSION = "forExpression";
	private static final String UNTILEXPRESSION = "untilExpression";

	@JsonCreator
	public OWait(){}
	
	public OWait(OProcess owner, OActivity parent) {
		super(owner, parent);
	}

	@JsonIgnore
	public OExpression getForExpression() {
		return (OExpression) fieldContainer.get(FOREXPRESSION);
	}

	@JsonIgnore
	public OExpression getUntilExpression() {
		return (OExpression) fieldContainer.get(UNTILEXPRESSION);
	}

	/**
	 * Is wait a duration?
	 * @return
	 */
	public boolean hasFor() {
		return getForExpression() != null;
	}

	/**
	 * Is wait an until?
	 * @return
	 */
	public boolean hasUntil() {
		return getUntilExpression() != null;
	}

	public void setForExpression(OExpression forExpression) {
		fieldContainer.put(FOREXPRESSION, forExpression);
	}

	public void setUntilExpression(OExpression untilExpression) {
		fieldContainer.put(UNTILEXPRESSION, untilExpression);
	}
}
