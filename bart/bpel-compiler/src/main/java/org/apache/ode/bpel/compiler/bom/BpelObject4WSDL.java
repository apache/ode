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

import org.w3c.dom.Element;

public abstract class BpelObject4WSDL extends BpelObject  implements ExtensibilityElement, Serializable  {
    private static final long serialVersionUID = 1L;

    private QName _elType;
    private Boolean _required;
    private String _targetNamespace;
    


    public BpelObject4WSDL(Element el) {
        super(el);
        _elType = new QName(el.getNamespaceURI(), el.getLocalName());
    }

    public QName getElementType() {
        return _elType;
    }

    public void setRequired(Boolean required) {
        _required = required;
    }

    public void setElementType(QName eltype) {
        _elType = eltype;
    }

    public Boolean getRequired() {
        return _required;
    }

    public void setTargetNamespace(String tns) {
        _targetNamespace = tns;
    }
    
    public String getTargetNamespace() {
        return _targetNamespace;
    }
}
