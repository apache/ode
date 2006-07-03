/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi;

/**
 * Exception thrown by the BDI.
 */
public class DebuggerException extends RuntimeException {
  private static final long serialVersionUID = 3543717663269483911L;

  public DebuggerException(String msg) {
    super(msg);
  }

  public DebuggerException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public DebuggerException(Exception e) {
    super(e.getMessage(), e);
  }
}
