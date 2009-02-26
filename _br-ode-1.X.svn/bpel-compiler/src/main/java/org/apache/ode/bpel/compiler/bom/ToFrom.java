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
        if (getAttribute("variable", null) != null) {
            if (is11()) return new VariableVal11(getElement());
            else return new VariableVal(getElement());
        }
        return null;
    }
    
    /**
     * Cast this tofrom to an "extension" to/from. This is NOT part of the BPEL spec, and 
     * is used to provide access to custom extensions (for example reading/writing SOAP
     * message headers)... Yes. it's evil. 
     * 
     * @author mszefler
     * @return the object cast to {@link ExtensionVal} if appropriate, null otherwise.
     */
    public ExtensionVal getAsExtensionVal() {
        if (getAttribute("extension",null) != null)
            return new ExtensionVal(getElement());
        return null;
    }
    
    /**
     * Test whether this to/from is an "extension" to-from (i.e. does it have the "extension" 
     * attribute). 
     * @return
     */
    public boolean isExtensionVal() {
        return getAsExtensionVal() != null;
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
