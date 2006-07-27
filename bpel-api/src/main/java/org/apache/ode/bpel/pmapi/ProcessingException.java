/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.pmapi;

/**
 * Exception indicating that a processing error preventing the
 * completion of the requested operation.
 */
public class ProcessingException extends ManagementException {

  public ProcessingException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public ProcessingException(String msg) {
    super(msg);
  }

}
