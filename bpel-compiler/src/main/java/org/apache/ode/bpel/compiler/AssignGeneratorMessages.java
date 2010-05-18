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

import javax.xml.namespace.QName;

public class AssignGeneratorMessages extends CompilationMessageBundle {

    /** Copy missing from-spec. */
    public CompilationMessage errMissingFromSpec() {
        return this.formatCompilationMessage("Copy missing from-spec.");
    }

    /** Copy to message variable {0} requires a message for the r-value. */
    public CompilationMessage errCopyToMessageFromNonMessage(String lval) {
        return this.formatCompilationMessage(
            "Copy to message variable {0} requires a message for the r-value.", lval);
    }

    /** Copy from message variable {0} requires a message for the l-value. */
    public CompilationMessage errCopyFromMessageToNonMessage(String rval) {
        return this.formatCompilationMessage(
            "Copy from message variable {0} requires a message for the l-value.", rval);
    }

    /**
     * Copy to message variable {0} of type {1} from message {2} of type {3} is
     * not permitted (the message types do not match).
     */
    public CompilationMessage errMismatchedMessageAssignment(String lvar, QName tlvalue,
                                                             String rval, QName trvalue) {
        return this.formatCompilationMessage(
            "Copy to message variable {0} of type {1} from message {2}" +
            " of type {3} is not permitted (the message types do not match).", lvar,
            tlvalue, rval, trvalue);
    }

    /** Copy to partner link {0} requires the partnerRole to be defined on partner link. */
    public CompilationMessage errCopyToUndeclaredPartnerRole(String lval) {
        return this.formatCompilationMessage(
            "Copy to partner link {0} requires the partnerRole to be defined on partner link.", lval);
    }

    /**
     * Copy from partner link {0} with enpoint reference {1} requires
     * the corresponding role to be defined on partner link.
     */
    public CompilationMessage errCopyFromUndeclaredPartnerRole(String rval, String epr) {
        return this.formatCompilationMessage(
            "Copy from partner link {0} with enpoint reference {1} requires" +
            " the corresponding role to be defined on partner link.", rval, epr);
    }

    /** Copy missing to-spec. */
    public CompilationMessage errMissingToSpec() {
        return this.formatCompilationMessage("Copy missing to-spec.");
    }

    /** From-spec format is unrecognized. */
    public CompilationMessage errUnkownFromSpec() {
        return this.formatCompilationMessage("From-spec format is unrecognized.");
    }

    /** To-spec format is unrecognized. */
    public CompilationMessage errUnknownToSpec() {
        return this.formatCompilationMessage("To-spec format is unrecognized.");
    }

}
