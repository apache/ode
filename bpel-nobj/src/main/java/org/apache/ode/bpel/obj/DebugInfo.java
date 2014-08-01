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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Information about the source that was used to create a compiled object.
 */
public class DebugInfo extends ExtensibleImpl  implements Serializable{
	public static final long serialVersionUID = -1L;
	/**
	 * Change log of class version
	 * initial 1
	 * current 2
	 * 
	 * 1->2:
	 * 	sourceURI attribute is more meaningful
	 *  */
	public static final int CURRENT_CLASS_VERSION = 2;

	/** Source file / resource name. */
	private static final String SOURCEURI = "sourceURI";
	/** Source line number (start). */
	private static final String STARTLINE = "startLine";
	private static final String DESCRIPTION = "description";
	private static final String EXTENSIBILITYELEMENTS = "extensibilityElements";
	/** Source line number (end). */
	private static final String ENDLINE = "endLine";

	@JsonCreator
	public DebugInfo(){
		setStartLine(0);
		setEndLine(0);
	}
	public DebugInfo(String sourceURI, int startLine, int endLine,
			Map<QName, Object> extElmt) {
		setSourceURI(sourceURI);
		setStartLine(startLine);
		setEndLine(endLine);
		if (extElmt != null && extElmt.size() > 0) {
			setExtensibilityElements(new HashMap<QName, Object>(extElmt));
		} else {
			setExtensibilityElements(new HashMap<QName, Object>());
		}
	}

	public DebugInfo(String sourceURI, int line, Map<QName, Object> extElmt) {
		this(sourceURI, line, line, extElmt);
	}

	@JsonIgnore
	public String getDescription() {
		Object o = fieldContainer.get(DESCRIPTION);
		return o == null ? null : (String)o;
	}

	@JsonIgnore
	public int getEndLine() {
		Object o = fieldContainer.get(ENDLINE);
		return o == null ? 0 : (Integer)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<QName, Object> getExtensibilityElements() {
		return (HashMap<QName, Object>) fieldContainer
				.get(EXTENSIBILITYELEMENTS);
	}

	@JsonIgnore
	public String getSourceURI() {
		Object o = fieldContainer.get(SOURCEURI);
		return o == null ? null : (String)o;
	}

	@JsonIgnore
	public int getStartLine() {
		Object o = fieldContainer.get(STARTLINE);
		return o == null ? 0 : (Integer)o;
	}

	public void setDescription(String description) {
		fieldContainer.put(DESCRIPTION, description);
	}

	public void setEndLine(int endLine) {
		fieldContainer.put(ENDLINE, endLine);
	}

	public void setExtensibilityElements(
			HashMap<QName, Object> extensibilityElements) {
		fieldContainer.put(EXTENSIBILITYELEMENTS, extensibilityElements);
	}

	public void setSourceURI(String sourceURI) {
		fieldContainer.put(SOURCEURI, sourceURI);
	}

	public void setStartLine(int startLine) {
		fieldContainer.put(STARTLINE, startLine);
	}

	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof DebugInfo)) return false;
		DebugInfo other = (DebugInfo)obj;
		boolean eq = this.getStartLine() == other.getStartLine() &&
				this.getEndLine() == other.getEndLine();
		if (this.getOriginalVersion() == 0 || other.getOriginalVersion() == 0){
			return eq;
		}else{
			return eq && this.getSourceURI().equals(other.getSourceURI());
		}
	}
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
		fieldContainer.remove(DESCRIPTION);
	}
}
