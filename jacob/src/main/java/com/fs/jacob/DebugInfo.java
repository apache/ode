/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob;

import java.io.PrintStream;
import java.io.Serializable;


/**
 * Standard debug information for channels, objects (channel reads), and
 * messages (channel writes).
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class DebugInfo implements Serializable {
  /** Stringified representation of the instance. */
  private String _creator = "unknown";

  /** Stack trace */
  private StackTraceElement[] _stackTrace = new StackTraceElement[0];

  public void setCreator(String creator) {
    _creator = creator;
  }

  public String getCreator() {
    return _creator;
  }

  public void setLocation(StackTraceElement[] location) {
    _stackTrace = location;
  }

  public StackTraceElement[] getLocation() {
    return _stackTrace;
  }

  public void printStackTrace(PrintStream pw) {
    pw.println(_creator);

    for (int i = 0; i < _stackTrace.length; i++)
      pw.println("\tat " + _stackTrace[i]);
  }
}
