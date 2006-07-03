/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.core;

/**
 * Base nested exception class for the framework.
 */
public class PxeSystemException extends RuntimeException {

  /**
   * @see Exception#Exception(java.lang.String)
   */
  public PxeSystemException(String msg) {
    super(msg);
  }

  public PxeSystemException(Throwable cause) {
    super(cause);
  }

  /**
   * @see Exception#Exception(java.lang.String, java.lang.Throwable)
   */
  public PxeSystemException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
}
