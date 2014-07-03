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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Compiled representation of a BPEL fault handler.
 */
public class OFaultHandler extends OBase {
	private static final String CATCHBLOCKS = "catchBlocks";

	@JsonCreator
	public OFaultHandler(){}
	public OFaultHandler(OProcess owner) {
		super(owner);
		setCatchBlocks(new ArrayList<OCatch>());
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OCatch> getCatchBlocks() {
		return (List<OCatch>) fieldContainer.get(CATCHBLOCKS);
	}

	public Collection<OLink> outgoinglinks() {
		throw new UnsupportedOperationException(); // TODO: implement me!
	}

	public void setCatchBlocks(List<OCatch> catchBlocks) {
		if (getCatchBlocks() == null){
			fieldContainer.put(CATCHBLOCKS, catchBlocks);
		}
	}
}
