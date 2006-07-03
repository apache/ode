/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.dao;

import com.fs.pxe.bpel.common.CorrelationKey;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * <p>
 * Data access object representing a BPEL correlation set.
 * Correlation sets are late-bound constants that "belong"
 * either to the process or to a scope.
 * </p>
 */
public interface CorrelationSetDAO {

  public Long getCorrelationSetId();

  /**
   * Get the name of the correlation set.
   * @return name of the correlation set
   */
  public String getName();

  /**
   * Get the scope instance to which this correlation set belongs.
   *
   * @see {@link ScopeDAO}
   * @return owning scope instance
   */
  public ScopeDAO getScope();

  /**
   * Sets the value of the correlation set.
   * @param names qualified names of the correlation set properties
   * @param values
   */
  public void setValue(QName[] names, CorrelationKey values);

  /**
   * Get the value of the correlation set.
   *
   * @return valu of correlation set
   */
  public CorrelationKey getValue();

  /**
   * Get correlation set properties with their values as a Map.
   * @return Map with the property qualified name as key and value as Map value.
   */
  public Map<QName, String> getProperties();
}
