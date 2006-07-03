/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel;

/**
 * Exception indicating an error with a PXE kernel module.
 */
public class PxeKernelModException extends Exception {

  private static final long serialVersionUID = 1L;

  public PxeKernelModException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public PxeKernelModException(String msg) {
    super(msg);
  }

  public PxeKernelModException(Throwable cause) {
    super(cause);
  }
}
