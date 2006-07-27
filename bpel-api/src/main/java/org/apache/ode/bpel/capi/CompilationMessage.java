/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.capi;

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
  public Object source;

  /** Common internationalized words. */
  private static final CommonMessages __commonMsgs = MessageBundle.getMessages(CommonMessages.class);

  private static final String SEVERITY_LEVELS[] = {
    __commonMsgs.strInfo().toLowerCase() + ": ",
    __commonMsgs.strWarning().toLowerCase() + ": ",
    __commonMsgs.strError().toLowerCase() + ": "
  };
  
  public CompilationMessage setSource(Object source) {
    this.source = source;
    return this;
  }

  /** Convert to a human-readable error string. */
  public String toErrorString() {
    StringBuffer buf = new StringBuffer();
    if (source != null) {
      buf.append(source);
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
