/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

/**
 * A transaction.
 */
abstract class Tx {
  boolean _rollbackOnly;

  protected abstract Object run();
  
  protected void setRollbackOnly() {
    _rollbackOnly = true;
  }
}
