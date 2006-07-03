/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Exception thrown by PXE interfaces indicating that the requested operation
 * could not be completed.
 */
public abstract class PxeException extends RuntimeException
{
  protected PxeException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
