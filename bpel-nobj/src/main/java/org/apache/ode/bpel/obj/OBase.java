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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ode.bpel.o.DebugInfo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * base class for compiled BPEL objects. It gives some common fields.
 * 
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class OBase {
	/** The wrapper wraps fields. Fields can be deleted, added or updated */
	protected Map<String, Object> fieldContainer;

	/** Our identifier, in terms of our parent. */
	private static String ID = "_id";
	/** Version of this class*/
	private static String CLASS_VERSION = "classVersion";
	/** Owner OProcess */
	private static String OWNER = "_owner";
	private static String DEBUG_INFO = "debugInfo";

	protected OBase(OProcess owner) {
		setOwner(owner);
		if (owner == null) {
			setId(0);
		} else {
			setId(++owner._childIdCounter);
			owner.getChildren().add(this);
		}
		fieldContainer = new LinkedHashMap<>();
	}

	protected OBase(OProcess owner, Map<String, Object> map) {
		this(owner);
		fieldContainer = map;
	}

	@JsonAnyGetter
	public Map<String, Object> getFieldContainer() {
		return fieldContainer;
	}

	/**
	 * Convenient method to add or set field dynamically.
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void addField(String name, Object value) {
		fieldContainer.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getField(String name) {
		return (T) fieldContainer.get(name);
	}

	@JsonIgnore
	public int getModelVersion() {
		return (int) fieldContainer.get(CLASS_VERSION);
	}

	public void setModelVersion(int version) {
		fieldContainer.put(CLASS_VERSION, version);
	}

	@JsonIgnore
	public int getId() {
		return (int) fieldContainer.get(ID);
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
			debugInfo.description = null;
			debugInfo.extensibilityElements = null;
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
