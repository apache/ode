/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.PxeException;

/**
 * Locally-accessible {@link com.fs.pxe.sfwk.spi.PxeException} implementation.
 */
class PxeExceptionImpl extends PxeException {
  PxeExceptionImpl(String msg, Throwable cause) {
    super(msg, cause);
  }
}
