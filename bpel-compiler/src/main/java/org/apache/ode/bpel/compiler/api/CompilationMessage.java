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

import org.apache.ode.utils.msg.CommonMessages;
import org.apache.ode.utils.msg.MessageBundle;

/**
 * Data structure representing messages emitted from the compiler. These
 * typically relate to the status of the compilation (e.g. compile
 * errors and the like).
 */
public class CompilationMessage {
  /** Informational message. */
  public static final short INFO = 0;
  /** Warning message. */
  public static final short WARN = 1;
  /** Error message. */
  public static final short ERROR = 2;

  /** Compilation phase. */
  public short phase;

  /** The severity severity. */
  public short severity;

  /** The message/error code for this message. */
  public String code;

  /** Internationalized message text. */
  public String messageText;

  /** The location in the source that caused this error/message/warning */
  public SourceLocation source;

  /** Common internationalized words. */
  private static final CommonMessages __commonMsgs = MessageBundle.getMessages(CommonMessages.class);

  private static final String SEVERITY_LEVELS[] = {
    __commonMsgs.strInfo().toLowerCase() + ": ",
    __commonMsgs.strWarning().toLowerCase() + ": ",
    __commonMsgs.strError().toLowerCase() + ": "
  };
  
  public CompilationMessage setSource(SourceLocation source) {
    this.source = source;
    return this;
  }

  /** Convert to a human-readable error string. */
  public String toErrorString() {
    StringBuffer buf = new StringBuffer();
    if (source != null) {
      buf.append(source.getURI());
      buf.append(':');
      buf.append(source.getLineNo());
      buf.append(": ");
    }
    buf.append(SEVERITY_LEVELS[severity]);
    buf.append('[');
    buf.append(code);
    buf.append("] ");
    buf.append(messageText);
    return buf.toString();
  }

  public String toString() {
    return "CompilationMessage: " + toErrorString();
  }

}
