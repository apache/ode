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
package org.apache.ode.bpel.compiler.bom;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * BPEL Variable declaration.
 */
public class Variable extends BpelObject {

    public Variable(Element el) {
        super(el);
    }

    public enum Kind {
        SCHEMA, ELEMENT, MESSAGE
    }

    /**
     * Get the name of the variable.
     *
     * @return variable name
     */
    public String getName() {
        return getAttribute("name", null);
    }

    /**
     * Get the type name of this variable.
     *
     * @return an XML element, XML schema type, or WSDL message type name.
     */
    public QName getTypeName() {
        String typename = null;
        switch (getKind()) {
        case MESSAGE:
            typename = getAttribute("messageType", null);
            break;
        case SCHEMA:
            typename = getAttribute("type", null);
            break;
        case ELEMENT:
            typename = getAttribute("element", null);
        }

        if (typename == null)
            return null;

        return getNamespaceContext().derefQName(typename);

    }

    /**
     * Get the type of declaration; one of: {@link #TYPE_SCHEMA},
     * {@link #TYPE_ELEMENT}, or {@link #TYPE_MESSAGE}.
     *
     * @return type of variable decleration
     */
    public Kind getKind() {
        if (getAttribute("messageType", null) != null)
            return Kind.MESSAGE;
        if (getAttribute("type", null) != null)
            return Kind.SCHEMA;
        if (getAttribute("element", null) != null)
            return Kind.ELEMENT;

        return null;

    }

    //
    // Stuff related to external variables.
    //

    /**
     * Get the external variable identifier (each one will be defined in the deployment descriptor)
     */
    public String getExternalId() {
        return getAttribute(ExtensibilityQNames.EXTVAR_ATTR, null);
    }

    /**
     * Is this an external variable? It is if it has the above attribute.
     * @return
     */
    public boolean isExternal() {
        return null != getExternalId();
    }

    /**
     * External variable support - get the "related" variable name.
     * @return
     */
    public String getRelated() {
        return  getAttribute(ExtensibilityQNames.EXTVAR_RELATED, null);
    }
}
