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
package org.apache.ode.bpel.o;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * XSD-typed variable type.
 */
public class OXsdTypeVarType extends OVarType {
    private static final long serialVersionUID = 1L;

    public QName xsdType;

    public boolean simple;

    public OXsdTypeVarType(OProcess owner) {
        super(owner);
    }

    public Node newInstance(Document doc) {
        if (simple)
            return doc.createTextNode("");
        else {
            Element el = doc.createElementNS(null, "xsd-complex-type-wrapper");
            return el;
        }
    }
}
