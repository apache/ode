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

import org.apache.ode.bpel.obj.OScope.Variable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of an external variable declaration.
 *
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class OExtVar extends OBase {

	/**
	 * Unique identifier for the external variable. Will be referenced in the deployment descriptor.
	 */
	private static final String EXTERNALVARIABLEID = "externalVariableId";

	/** Related variable containing the identifying information. */
	private static final String RELATED = "related";

	@JsonCreator
	public OExtVar(){}
	
	public OExtVar(OProcess owner) {
		super(owner);
	}

	@JsonIgnore
	public String getExternalVariableId() {
		Object o = fieldContainer.get(EXTERNALVARIABLEID);
		return o == null ? null : (String)o;
	}

	@JsonIgnore
	public Variable getRelated() {
		Object o = fieldContainer.get(RELATED);
		return o == null ? null : (Variable)o;
	}

	public void setExternalVariableId(String externalVariableId) {
		fieldContainer.put(EXTERNALVARIABLEID, externalVariableId);
	}

	public void setRelated(Variable related) {

		fieldContainer.put(RELATED, related);
	}

}
