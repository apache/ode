/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi;

import com.fs.pxe.bpel.common.CorrelationKey;

import javax.xml.namespace.QName;

/**
 * Access to a correlation set for inspection and modification purposes.  Represents a
 * single correlation set associated with a scope.
 */
public interface Correlation {

  /**
   * Get the values for a correlation set.
   * @return the correlation key
   */
  public CorrelationKey getCorrelationKey() throws DebuggerException;

  /**
   * Set the values for a correlation set.
   * @param propertyNames
   * @param ckey
   */
  public void setCorrelationKey(QName[] propertyNames, CorrelationKey ckey) throws DebuggerException;
}
