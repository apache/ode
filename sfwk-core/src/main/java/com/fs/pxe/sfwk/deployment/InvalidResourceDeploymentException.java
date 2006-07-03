/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

public class InvalidResourceDeploymentException extends DeploymentException {
  /**
   * Constructor.
   * @param msg the exception message
   * @param cause <code>Throwable</code> cause of this exception
   */
  public InvalidResourceDeploymentException(String msg, Throwable cause) {
    super(msg);
    initCause(cause);
  }
}
