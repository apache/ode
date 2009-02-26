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

import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompilationMessageBundle;

public class PickGeneratorMessages extends CompilationMessageBundle {

    /**
     * Attempt to use multiple non-initiate correlation sets; second set was
     * "{0}".
     */
    public CompilationMessage errSecondNonInitiateOrJoinCorrelationSet(String setName) {
        return this.formatCompilationMessage("Attempt to use multiple non-initiate or join correlation sets;"
                + " second set was \"{0}\".", setName);
    }

    // TODO: better error message
    public CompilationMessage errForOrUntilMustBeGiven() {
        return this.formatCompilationMessage("errForOrUntilMustBeGiven");
    }

    public CompilationMessage errEmptyOnMessage() {
        return this.formatCompilationMessage("An onMessage element declared in a pick can't be empty.");
    }

    // TODO: better error message
    public CompilationMessage errOnAlarmWithCreateInstance() {
        return this.formatCompilationMessage("errOnAlarmWithCreateInstance");
    }

    public CompilationMessage errRendezvousNotSupported() {
        return this.formatCompilationMessage("Rendezvous correlation mode not supported in this context.");
    }

}
