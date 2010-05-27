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

public abstract class OLValueExpression extends OExpression {

    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether the expression, if it is a simple path, must
     * be created if missing By a simple path, we mean a path expression whose
     * steps are fully-qualified names separated by slashes. In case any of
     * the steps in the simple path is non-existent, then we must create it.
     */
    public boolean insertMissingData;

    /**
     * @param owner
     */
    public OLValueExpression(OProcess owner) {
        super(owner);
    }

    public abstract OScope.Variable getVariable();

}
