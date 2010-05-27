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
 * BPEL-1.1 overrides for the expression object. In BPEL 1.1 we had these things appear in an
 * attribute, so we'll return the attribute node for the expression node.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class Expression11 extends Expression {

    private Node _expression;

    public Expression11(Element el, Node expression) {
        super(el);
        _expression = expression;
    }

    public Node getExpression() {
        return _expression;
    }

    public String getExpressionLanguage() {
        return getAttribute("queryLanguage", null);
    }


}
