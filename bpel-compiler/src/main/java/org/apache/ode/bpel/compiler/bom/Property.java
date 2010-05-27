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
 * Interim object representation of a BPEL <code>&lt;property</code> element.
 */
public class Property extends BpelObject4WSDL {

    private static final long serialVersionUID = -1L;

    public Property(Element el) {
        super(el);
    }


    /**
     * Get the name of the property; note that this name is in the target-namespace of the WSDL.
     *
     * @return the <code>QName</code> of the property.
     */
    public QName getName() {
        return new QName(getTargetNamespace(),getAttribute("name"));
    }

    /**
     * Get the name of the schema type for this property
     *
     * @return the <code>QName</code> for the schema type of this property.
     */
    public QName getPropertyType() {
        return getNamespaceContext().derefQName(getAttribute("type"));
    }


}
