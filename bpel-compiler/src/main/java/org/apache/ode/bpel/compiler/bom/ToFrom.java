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

public class ToFrom extends BpelObject {

    public ToFrom(Element el) {
        super(el);
    }
    
    public VariableVal getAsVariableVal() {
        if (getAttribute("variable", null) != null)
            return new VariableVal(getElement());
        // TODO fix for 11
        return null;
    }
    
    public boolean isVariableVal() {
        return getAsVariableVal() != null;
    }
    
    public PartnerLinkVal getAsPartnerLinkVal() {
        if (getAttribute("partnerLink",null) != null)
            return new PartnerLinkVal(getElement());
        return null;
    }
    
    public boolean isPartnerLinkVal() {
        return getAsPartnerLinkVal() != null;
    }

    public boolean isPropertyVal() {
        return getAsPropertyVal() != null;
    }

    public PropertyVal getAsPropertyVal() {
        if (getAttribute("property",null) != null)
            return new PropertyVal(getElement());
        return null;
    }
    
    public Expression getAsExpression() {
        // BPEL 1.1 will have an expression only for the /from/ nodes.
        if (is11()) 
            return null;
        
        return new Expression(getElement());
    }

}
