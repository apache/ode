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

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;

/**
 * WSDL extension element for a BPEL <code>&lt;propertyAlias&gt;</code>
 * element.
 *
 * @see org.apache.ode.bpel.compiler.wsdl.PropertyAliasSerializer_11
 */
public class PropertyAlias extends BpelObject4WSDL implements ExtensibilityElement, Serializable {

    private static final long serialVersionUID = -1L;

    public PropertyAlias(Element el) {
        super(el);
    }

    /**
     * Get the name of the WSDL <code>message</code> type that this alias it
     * to apply to.
     *
     * @return the <code>QName</code> for the <code>messageType</code>
     */
    public QName getMessageType() {
        return getNamespaceContext().derefQName(getAttribute("messageType"));
    }

    /**
     * Get the name of the WSDL <code>part</code> that this alias is to apply
     * to (within the specified <code>message</code>).
     *
     * @return the name of the part
     * @see #getMessageType()
     */
    public String getPart() {
        return getAttribute("part");
    }
    
    public String getHeader() {
        return getAttribute("header");
    }

    /**
     * Get the <code>QName</code> of the property that this alias applies to.
     *
     * @return the property <code>QName</code>
     */
    public QName getPropertyName() {
        return getNamespaceContext().derefQName(getAttribute("propertyName"));
    }

    /**
     * Get the location path query for the <code>OPropertyAlias</code> as
     * originally specified in the WSDL.
     *
     * @return the query
     */
    public Expression getQuery() {
        return getFirstChild(Expression.class);
    }

    /**
     * Get the namespace context for the <code>&lt;propertyAlias&gt;</code>
     * element that created this object.
     *
     * @return the <code>NSContext</code> the encapsulates the namespace
     *         context
     */
    public NSContext getNSContext() {
        return getNamespaceContext();
    }
}
