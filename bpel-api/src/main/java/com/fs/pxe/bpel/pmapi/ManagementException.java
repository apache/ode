/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.pmapi;

/**
 * Exception thrown by the Process Management API to indicate errors.
 */
public class ManagementException extends RuntimeException {

  public ManagementException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public ManagementException(String msg) {
    super(msg);
  }

}
