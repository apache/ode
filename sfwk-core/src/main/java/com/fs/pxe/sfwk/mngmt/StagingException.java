/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.mngmt;

/**
 * An <code>Exception</code> that occurs while staging the deployment of a
 * syste on the target machine, e.g., the inability to write
 * a temporary file.
 */
public class StagingException extends Exception {

  /**
   * @see Exception#Exception()
   */
  public StagingException() {
    super();
  }

  /**
   * @see Exception#Exception(java.lang.String)
   */
  public StagingException(String message) {
    super(message);
  }

  /**
   * @see Exception#Exception(java.lang.String, java.lang.Throwable)
   */
  public StagingException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @see Exception#Exception(java.lang.Throwable)
   */
  public StagingException(Throwable cause) {
    super(cause);
  }
}
