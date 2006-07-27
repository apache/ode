/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime.monitor;

/**
 * Error occured while monitoring process.
 */
public class MonitorException extends Exception {
  private static final long serialVersionUID = 545154231227628391L;

  /**
   * @see Exception#Exception(String)
   */
  public MonitorException(String message) {
    super(message);
  }

  /**
   * @see Exception#Exception(String,Throwable)
   */
  public MonitorException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @see Exception#Exception(Throwable)=
   */
  public MonitorException(Throwable cause) {
    super(cause);
  }
}
