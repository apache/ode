/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.common;

/**
 * Exception class indicating that the received message did not conform
 * to the expections of the process, and therefore cannot be processed.
 */
public class InvalidMessageException extends RuntimeException {
  private static final long serialVersionUID = 17580980430874179L;

  public InvalidMessageException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
