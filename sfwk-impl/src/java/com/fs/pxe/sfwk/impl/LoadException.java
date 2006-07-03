/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.core.PxeSystemException;

/**
 * Local exception used to communicate a failure loading the state of a system.
 */
class LoadException extends PxeSystemException {
  LoadException(String msg) {
    super(msg);
  }

  LoadException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
