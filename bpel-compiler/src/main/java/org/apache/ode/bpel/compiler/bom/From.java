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
 * Marker interface for assignment R-values.
 */
public class From extends ToFrom {
    
    public From(Element el) {
        super(el);
    }

    public LiteralVal getAsLiteralVal() {
        return getFirstChild(LiteralVal.class);
    }

    public boolean isLiteralVal() {
        return getAsLiteralVal() != null;
    }

    @Override
    public Expression getAsExpression() {
        // BPEL 1.1 fixups. In 1.1 the expression was an attribute not the child element.
        if (is11()) {
            String expr = getAttribute("expression" , null);
            return expr == null  ? null : new Expression11(getElement(),getElement().getAttributeNode("expression"));
        }
        
        return super.getAsExpression();
    }

    
}
