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

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * XSD-typed variable type.
 */
public class OXsdTypeVarType extends OVarType {
	private static final String XSDTYPE = "xsdType";
	private static final String SIMPLE = "simple";

	public OXsdTypeVarType(OProcess owner) {
		super(owner);
	}

	@JsonIgnore
	public boolean isSimple() {
		return (Boolean) fieldContainer.get(SIMPLE);
	}

	@JsonIgnore
	public QName getXsdType() {
		return (QName) fieldContainer.get(XSDTYPE);
	}

	public Node newInstance(Document doc) {
		if (isSimple())
			return doc.createTextNode("");
		else {
			Element el = doc.createElementNS(null, "xsd-complex-type-wrapper");
			return el;
		}
	}

	public void setSimple(boolean simple) {
		fieldContainer.put(SIMPLE, simple);
	}

	public void setXsdType(QName xsdType) {
		fieldContainer.put(XSDTYPE, xsdType);
	}
}
