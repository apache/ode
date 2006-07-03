/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import javax.xml.namespace.QName;

/**
 * BPEL correlation set declaration. A correlation set is--like a variable--declared in
 * a scope-like construct (see {@link Scope}.
 */
public interface CorrelationSet extends BpelObject {

  /**
   * Get the scope in which this correlation set is declared.
   *
   * @return declaring scope
   */
  Scope getDeclaringScope();

  /**
   * Get the name of this correlation set.
   *
   * @return correlation set name
   */
  String getName();


  /**
   * Set the name of this correlation set.
   * @param name correlation set name
   */
  void setName(String name);

  /**
   * Get the (ordered) set of properties that define this correlation set.
   * Properties are returned by their qualified name.
   * @return set of defining properties
   */
  QName[] getProperties();

  /**
   * Set the (ordered) set of properties that define this correlation set.
   * @param properties set of defining properties
   */
  void setProperties(QName[] properties);

}
