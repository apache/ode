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
 * Assignment L/R-value defined in terms of message variable extensions. This is a 
 * BPEL hack (not standard BPEL) that allows the process to access custom message 
 * "extensions", for example SOAP headers and the like. Evil, use sparingly. 
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class ExtensionVal extends ToFrom {

    public ExtensionVal(Element el) {
        super(el);
    }

    public String getVariable() {
        return getAttribute("variable", null);
    }

    public QName getExtension() {
        return getNamespaceContext().derefQName(getAttribute("extension", null));
    }
}

