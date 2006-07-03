/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

import com.fs.pxe.sfwk.core.PxeSystemException;

/**
 * Thrown when a system cannot be deployed.
 */
public class DeploymentException extends PxeSystemException {
  /**
   * Constructor.
   * @param msg exception message
   * @param cause <code>Throwable</code> cause of this exception
   */
  public DeploymentException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Constructor.
   * @param msg exception message
   */
  public DeploymentException(String msg) {
    super(msg);
  }

  /**
   * Constructor.
   */
  DeploymentException() {
    super("deploymenterr");
  }
}
