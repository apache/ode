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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of the BPEL <code>&lt;sequence&gt;</code> activity.
 */
public class OSequence extends OActivity {
	private static final String SEQUENCE = "sequence";

	public OSequence(OProcess owner, OActivity parent) {
		super(owner, parent);
		setSequence(new ArrayList<OActivity>());
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OActivity> getSequence() {
		return (List<OActivity>) fieldContainer.get(SEQUENCE);
	}

	public void setSequence(List<OActivity> sequence) {
		fieldContainer.put(SEQUENCE, sequence);
	}
}
