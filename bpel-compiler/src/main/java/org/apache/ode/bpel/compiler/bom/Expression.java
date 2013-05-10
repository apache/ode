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
import org.w3c.dom.Node;

/**
 * BOM representation of a BPEL expression language expression.
 */
public class Expression extends BpelObject {

    public Expression(Element el) {
        super(el);
    }

    /**
     * Get the expression language for this expression.
     * 
     * @return expression langauge URI or <code>null</code> if none specified
     */
    public String getExpressionLanguage() {
        return getAttribute("expressionLanguage", null);
    }
    
    public Node  getExpression(){
        getElement().normalize();
        for (Node n = getElement().getFirstChild(); n != null; n = n.getNextSibling()) {
            switch (n.getNodeType()) {
            case Node.TEXT_NODE:
                if(n.getNodeValue().trim().length() > 0) return n;
                else if(n.getNextSibling() != null) continue;
                else return n;
            case Node.ELEMENT_NODE:
            case Node.CDATA_SECTION_NODE:
                return n;
            }
        }

        return null;
    }

}
