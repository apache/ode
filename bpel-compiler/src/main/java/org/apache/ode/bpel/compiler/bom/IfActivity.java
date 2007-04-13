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

import java.util.List;

/**
 * Representation of the BPEL <code>&lt;switch&gt;</code> activity.
 */
public class IfActivity extends Activity {
    public IfActivity(Element el) {
        super(el);
    }
    
    public Expression getCondition() {
        return getFirstChild(Expression.class);
    }
    
    /**
     * Get the activity for this if. BPEL 2.0 draft mandated the inclusion of the
     * condition success activity in a <then> element. In that case this will be
     * null. For BPEL 2.0 final this should return the condition success activity.
     *
     * @return activity enabled when case is satisfied
     */
    public Activity getActivity() {
        return getFirstChild(Activity.class);
    }

    /**
     * Get the cases for this switch.
     * 
     * @return the cases
     */
    public List<Case> getCases() {
        return getChildren(Case.class);
    }

    /**
     * BPEL object model representation of a <code>&lt;case&gt;</code>.
     */
    public static class Case extends BpelObject {

        public Case(Element el) {
            super(el);
        }

        /**
         * Get the activity for this case.
         * 
         * @return activity enabled when case is satisfied
         */
        public Activity getActivity() {
            return getFirstChild(Activity.class);
        }

        /**
         * Get the condition associated with this case.
         * 
         * @return the condition
         */
        public Expression getCondition() {
            return getFirstChild(Expression.class);
        }

    }
}
