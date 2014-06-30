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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Information about the source that was used to create a compiled object.
 */
public class DebugInfo extends ExtensibleImpl {
	/** Source file / resource name. */
	private static final String SOURCEURI = "sourceURI";
	/** Source line number (start). */
	private static final String STARTLINE = "startLine";
	private static final String DESCRIPTION = "description";
	private static final String EXTENSIBILITYELEMENTS = "extensibilityElements";
	/** Source line number (end). */
	private static final String ENDLINE = "endLine";

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
		return (String) fieldContainer.get(DESCRIPTION);
	}

	@JsonIgnore
	public int getEndLine() {
		return (Integer) fieldContainer.get(ENDLINE);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<QName, Object> getExtensibilityElements() {
		return (HashMap<QName, Object>) fieldContainer
				.get(EXTENSIBILITYELEMENTS);
	}

	@JsonIgnore
	public String getSourceURI() {
		return (String) fieldContainer.get(SOURCEURI);
	}

	@JsonIgnore
	public int getStartLine() {
		return (Integer) fieldContainer.get(STARTLINE);
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

	//TODO: check legacy OModel DebugInfo#readObject.
}
