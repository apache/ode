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

public class FlowGeneratorMessages extends CompilationMessageBundle {

    /** The link "{0}" does not have a source. */
    public CompilationMessage errLinkMissingSourceActivity(String linkName) {
        return this.formatCompilationMessage("The link \"{0}\" does not have a source.", linkName);
    }

    /** The link "{0}" does not have a target. */
    public CompilationMessage errLinkMissingTargetActivity(String linkName) {
        return this.formatCompilationMessage("The link \"{0}\" does not have a target.", linkName);
    }

    /** Duplicate declaration of link "{0}". */
    public CompilationMessage errDuplicateLinkDecl(String linkName) {
        return this.formatCompilationMessage("Duplicate declaration of link \"{0}\".", linkName);
    }

}
