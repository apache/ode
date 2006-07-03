/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

/**
 * An error occurred during the invocation of a management operation.
 */
public class ManagementException extends Exception {
  /**
   * Constructor.
   * @param msg the exception message
   * @param cause <code>Throwable</code> cause of the exception
   */
  public ManagementException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
