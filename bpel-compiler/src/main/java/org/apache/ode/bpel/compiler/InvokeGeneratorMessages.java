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

import javax.xml.namespace.QName;

import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompilationMessageBundle;

public class InvokeGeneratorMessages extends CompilationMessageBundle {

    /**
     * Invoke doesn't define an output variable even though the operation {0}
     * declares an output message.
     */
    public CompilationMessage errInvokeNoOutputMessageForOutputOp(String operation) {
        return this.formatCompilationMessage(
            "Invoke doesn't define an output variable even though the operation \"{0}\" "
                + "declares an output message.", operation);
    }

    /**
     * Invoke doesn't define an input variable even though the operation {0}
     * declares an input message.
     */
    public CompilationMessage errInvokeNoInputMessageForInputOp(String operation) {
        return this.formatCompilationMessage(
                "Invoke doesn't define an output variable even though the operation \"{0}\" "
                    + "declares an output message.", operation);
    }

    public CompilationMessage errPortTypeMismatch(QName iptype, QName pltype) {
        return this.formatCompilationMessage(
                "The portType \"{0}\" specified on the <invoke> does not match \"{1}\", the port type declared in the" +
                "partner link.", iptype, pltype);

    }

}
