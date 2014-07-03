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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * base class for compiled BPEL objects. It gives some common fields.
 * 
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class OBase extends ExtensibleImpl{
	/** Our identifier, in terms of our parent. */
	private static String ID = "_id";
	/** Owner OProcess */
	private static String OWNER = "_owner";
	private static String DEBUG_INFO = "debugInfo";
	
	/**
	 * This should only be used by jackson when deserialize
	 */
	protected OBase(){
		setId(0);
	}
	
	protected OBase(OProcess owner) {
		super();
		init(owner);
	}

	protected OBase(OProcess owner, Map<String, Object> map) {
		super(map);
		init(owner);
	}

	private void init(OProcess owner) {
		setOwner(owner);
		if (owner == null) {
			setId(0);
		} else {
			owner.setChildIdCounter(owner.getChildIdCounter() + 1);
			setId(owner.getChildIdCounter());
			owner.getChildren().add(this);
		}
	}

	@JsonIgnore
	public int getId() {
		return (Integer) fieldContainer.get(ID);
	}

	private void setId(int id) {
		fieldContainer.put(ID, id);
	}

	@JsonIgnore
	public OProcess getOwner() {
		Object owner = fieldContainer.get(OWNER);
		return (OProcess) (owner == null ? this : owner);
	}

	private void setOwner(OProcess process) {
		fieldContainer.put(OWNER, process);
	}

	@JsonIgnore
	public DebugInfo getDebugInfo() {
		return (DebugInfo) fieldContainer.get(DEBUG_INFO);
	}

	public void setDebugInfo(DebugInfo debugInfo) {
		fieldContainer.put(DEBUG_INFO, debugInfo);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(getClass().getSimpleName());
		buf.append('#');
		buf.append(getId());
		return buf.toString();
	}

	public void dehydrate() {
		DebugInfo debugInfo = getDebugInfo();
		if (debugInfo != null) {
			debugInfo.setDescription(null);
			debugInfo.setExtensibilityElements(null);
			debugInfo = null;
		}
	}

	public String digest() {
		return "";
	}

	public int hashCode() {
		return getId();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof OBase))
			return false;
		OBase other = (OBase) obj;
		return (getId() == 0 && other.getId() == 0) || getId() == other.getId()
				&& other.getOwner().equals(getOwner());
	}
}
