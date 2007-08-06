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

package org.apache.ode.bpel.compiler.api;

import org.apache.ode.utils.msg.MessageBundle;

public abstract class CompilationMessageBundle extends MessageBundle {

  // TODO turn CompilationMessage.CODE into a proper Java5 enum
  private static String[] PREFIXES = {"inf", "warn", "err"};
  private static short[] SEVERITIES = {CompilationMessage.INFO, CompilationMessage.WARN,
    CompilationMessage.ERROR};

  protected CompilationMessage formatCompilationMessage(String message, Object... args) {
    String methodName = "errUnknown";
    String methodKey = null;
    short severity = 0;

    // format the regular message text
    String msg = this.format(message, args);

    // I will *so* burn in hell for this
    StackTraceElement[] stack = new Throwable().getStackTrace();
    if (stack.length > 1) {
      methodName = stack[1].getMethodName();
    }
    else {
      // bummer: no stack trace - create an unknown error as fallback.
    }

    // find method prefix
    for (int i = 0; i < PREFIXES.length; i++) {
      if (methodName.startsWith(PREFIXES[i])) {
        methodKey = methodName.substring(PREFIXES[i].length());
        severity = SEVERITIES[i];
        break;
      }
    }

    // found a prefixed method?
    if (methodKey == null) {
      throw new UnsupportedOperationException("unexpected caller method: " + methodName);
    }

    CompilationMessage cmsg = new CompilationMessage();
    cmsg.severity = severity;
    cmsg.phase = 0;
    cmsg.messageText = msg;
    cmsg.code = methodKey;

    return cmsg;
  }

}
