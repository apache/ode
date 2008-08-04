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
package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.rapi.CorrelationSet;

public class CorrelationSetInstance implements CorrelationSet {
    public OScope.CorrelationSet declaration;
    public Long scopeInstance;

    public CorrelationSetInstance(Long scopeInstanceId, OScope.CorrelationSet cset) {
        this.scopeInstance = scopeInstanceId;
        this.declaration = cset;
    }

    public String getName() {
        return declaration.name;
    }

    public long getScopeId() {
        return scopeInstance;
    }
}
