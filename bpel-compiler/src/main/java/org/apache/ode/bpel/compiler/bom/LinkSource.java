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
 * BOM representation of a "link source" (i.e. a <code>&lt;source...&gt;</code>
 * element) declaration.
 */
public class LinkSource extends BpelObject {

    public LinkSource(Element el) {
        super(el);
    }



    /**
     * Get the refernced link.
     *
     * @return name of referenced link
     */
    public String getLinkName() {
        return getAttribute("linkName", null);
    }

    /**
     * Get the link transition condition.
     *
     * @return transition condition {@link Expression}
     */
    public Expression getTransitionCondition() {
        if (is11()){
            return isAttributeSet("transitionCondition") ? new Expression11(getElement(),
                    getElement().getAttributeNode("transitionCondition")) : null;
        }

        return getFirstChild(Expression.class);
    }

}
