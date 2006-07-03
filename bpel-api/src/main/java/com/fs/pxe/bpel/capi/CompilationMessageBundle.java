/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.capi;

import com.fs.utils.msg.MessageBundle;

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
