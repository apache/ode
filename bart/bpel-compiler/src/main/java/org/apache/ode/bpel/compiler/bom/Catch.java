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
 * Representation of a BPEL fault handler catch block.
 */
public class Catch extends Scope {
    public Catch(Element el) {
        super(el);
    }

    /**
     * Get the activity for this catch block. This is the activity that is
     * activated if this catch block is enabled.
     * 
     * @return catch activity fault handling activity
     */
    public Activity getActivity() {
        return getFirstChild(Activity.class);
    }

    /**
     * Get the name of the fault. May be <code>null</code>.
     * 
     * @return fault name or <code>null</code>
     */
    public QName getFaultName() {
        return getNamespaceContext().derefQName(getAttribute("faultName", null));
    }

    /**
     * Get the fault variable. May be <code>null</code>
     * 
     * @return name of the fault variable
     */
    public String getFaultVariable() {
        return getAttribute("faultVariable", null);
    }

    /**
     * Get the fault variable type. The fault variable type must be specified in
     * BPEL 2.0 if the fault variable is set.
     * 
     * @return fault variable type or <code>null</code> if none specified.
     */
    public QName getFaultVariableMessageType() {
        return getNamespaceContext().derefQName(getAttribute("faultMessageType", null));
    }

    /**
     * Get the fault variable type. The fault variable type must be specified in
     * BPEL 2.0 if the fault variable is set.
     * 
     * @return fault variable type or <code>null</code> if none specified.
     */
    public QName getFaultVariableElementType() {
        return getNamespaceContext().derefQName(getAttribute("faultElement", null));
    }

}
