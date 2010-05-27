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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.o.OActivity;


/**
 * Interface implemented by classes providing activity-generating logic.
 * Implementations of this interface are used to convert an
 * activity description object ({@link org.apache.ode.bom.impl.nodes.ActivityImpl})
 * into a <em>compiled</em> BPEL representation.
 */
public interface ActivityGenerator {
    public void setContext(CompilerContext context);

    /**
     * Generate compiled representation for the given activity definition.
     *
     * @param src activity definition
     */
    public void compile(OActivity output, Activity src);

    public OActivity newInstance(Activity src);
}
