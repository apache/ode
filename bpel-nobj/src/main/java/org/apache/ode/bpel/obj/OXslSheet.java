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

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of an XSL sheet.
 */
public class OXslSheet extends OBase {
	private static final String URI = "uri";

	private static final String SHEETBODY = "sheetBody";

	public OXslSheet(OProcess owner) {
		super(owner);
	}

	@JsonIgnore
	public String getSheetBody() {
		return (String) fieldContainer.get(SHEETBODY);
	}

	@JsonIgnore
	public URI getUri() {
		return (URI) fieldContainer.get(URI);
	}

	public void setSheetBody(String sheetBody) {
		fieldContainer.put(SHEETBODY, sheetBody);
	}

	public void setUri(URI uri) {
		fieldContainer.put(URI, uri);
	}

}
