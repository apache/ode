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
 * Representation of the BPEL <code>&lt;forEach&gt;</code> activity.
 */
public class ForEachActivity extends CompositeActivity {

    public ForEachActivity(Element el) {
        super(el);
    }

    /**
     * Gets the counter variable name used for iteration.
     * 
     * @return counter variable name
     */
    public String getCounterName() {
        return getAttribute("counterName", null);
    }

    /**
     * Returns whether this forEach executes iterations on nested scopes
     * parrallely or sequentially
     * 
     * @return true if parrallel, false if sequential
     */
    public boolean isParallel() {
        return getAttribute("parallel", "no").equals("yes");
    }

    /**\
     * Gets the expression that will be used as a start value for the iteration
     * counter.
     * 
     * @return start iteration counter
     */
    public Expression getStartCounter() {
        return (Expression) getFirstChild(rewriteTargetNS(Bpel20QNames.START_COUNTER_VALUE));
    }

    /**
     * Sets the expression that will be used as a termination value for the
     * forEach iterations.
     * 
     * @return final counter expression
     */
    public Expression getFinalCounter() {
        return (Expression) getFirstChild(rewriteTargetNS(Bpel20QNames.FINAL_COUNTER_VALUE));
    }

    /**
     * Gets a completion condition defining how many child scope completions can
     * occur before the forEach completes.
     * 
     * @return completion condition
     */
    public CompletionCondition getCompletionCondition() {
        return getFirstChild(CompletionCondition.class);
    }

    /**
     * Gets the scope activity that we will iterate on.
     * 
     * @return child scope activity
     */
    public ScopeActivity getChild() {
        return getFirstChild(ScopeActivity.class);
    }
}
