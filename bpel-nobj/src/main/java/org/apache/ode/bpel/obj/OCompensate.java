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
 * Compiled representation of the BPEL <code>&lt;compensate&gt;</code> activity.
 */
public class OCompensate extends OActivity  implements Serializable{
	public static final long serialVersionUID = -1L;

	/** The scope that is compensated by this activity. */
	private static final String COMPENSATEDSCOPE = "compensatedScope";

	@JsonCreator
	public OCompensate(){}
	
	@JsonIgnore
	public OScope getCompensatedScope() {
		Object o = fieldContainer.get(COMPENSATEDSCOPE);
		return o == null ? null : (OScope)o;
	}

	public void setCompensatedScope(OScope compensatedScope) {
		fieldContainer.put(COMPENSATEDSCOPE, compensatedScope);
	}

	public OCompensate(OProcess owner, OActivity parent) {
		super(owner, parent);
	}
}
