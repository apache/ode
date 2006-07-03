/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.pmapi;

/**
 * Exception indicating an invalid request.
 */
public class InvalidRequestException extends ManagementException {

  public InvalidRequestException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public InvalidRequestException(String msg) {
    super(msg);
  }

}
