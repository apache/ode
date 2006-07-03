/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools;

public class ExecutionException extends Exception {

  public ExecutionException(String msg) {
    super(msg);
  }

  public ExecutionException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public ExecutionException(Throwable cause) {
    super(cause);
  }
}
