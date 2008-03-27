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

import org.w3c.dom.Element;

/**
 * Assignment L/R-value defined by a location within a BPEL variable.
 */
public class VariableVal extends ToFrom {
    public VariableVal(Element el) {
        super(el);
    }

    /**
     * Get the name of the variable.
     * 
     * @return variable name
     */
    public String getVariable() {
        return getAttribute("variable", null);
    }

    /**
     * Get the (optional) message part.
     * @return name of the message part, or <code>null</code>
     */
    public String getPart() {
        return getAttribute("part", null);
    }

    /**
     * Get the (optional) header part.
     * @return name of the header part, or <code>null</code>
     */
    public String getHeader() {
        return getAttribute("header", null);
    }

    public Expression getLocation() {
        return getFirstChild(Query.class);
    }

}
