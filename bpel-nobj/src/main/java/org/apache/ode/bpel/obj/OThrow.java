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
import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OScope.Variable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of the BPEL <code>&lt;throw&gt;</code> activity.
 */
public class OThrow extends OActivity  implements Serializable{
	public static final long serialVersionUID = -1L;
	private static final String FAULTVARIABLE = "faultVariable";

	private static final String FAULTNAME = "faultName";

	@JsonCreator
	public OThrow(){}
	public OThrow(OProcess owner, OActivity parent) {
		super(owner, parent);
	}

	@JsonIgnore
	public QName getFaultName() {
		Object o = fieldContainer.get(FAULTNAME);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public Variable getFaultVariable() {
		Object o = fieldContainer.get(FAULTVARIABLE);
		return o == null ? null : (Variable)o;
	}

	public void setFaultName(QName faultName) {
		fieldContainer.put(FAULTNAME, faultName);
	}

	public void setFaultVariable(Variable faultVariable) {
		fieldContainer.put(FAULTVARIABLE, faultVariable);
	}
}
