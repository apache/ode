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
package org.apache.ode.bpel.o;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Compiled representation of a BPEL <code>&lt;switch&gt;</code>. The
 * BPEL compiler generates instances of this class.
 */
public class OSwitch extends OActivity {
    static final long serialVersionUID = -1L  ;

    /**
     * The cases declared within the <code>&lt;switch&gt;</code> actvity.
     */
    private final List<OCase> _cases = new ArrayList<OCase> ();

    public OSwitch(OProcess owner, OActivity parent) {
        super(owner, parent);
    }

    public void addCase(OCase acase) {
        _cases.add(acase);
    }

    public List<OCase> getCases() {
        return Collections.unmodifiableList(_cases);
    }

    public static class OCase extends OBase {
        private static final long serialVersionUID = 1L;
        public OExpression expression;
        public OActivity activity;

        public OCase(OProcess owner) {
            super(owner);
        }
    }
}
