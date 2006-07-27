/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.capi;

/**
 * Exception indicating a compilation error. 
 */
public class CompilationException extends RuntimeException {
  private static final long serialVersionUID = -4683674811787611083L;
  private CompilationMessage _msg;

  public CompilationException(CompilationMessage msg, Throwable cause) {
    super(msg.toErrorString(),cause);
    _msg = msg;
  }
  /**
   * @see Exception#Exception(String,Throwable)
   */
  public CompilationException(CompilationMessage msg) {
    this(msg, null);
  }

  public String toErrorMessage() {
    return _msg.toErrorString();
  }

  /** Get the {@link CompilationMessage} associated with this exception}. */
  public CompilationMessage getCompilationMessage() {
    return _msg;
  }
}
