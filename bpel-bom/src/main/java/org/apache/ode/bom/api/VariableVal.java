/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;


/**
 * Assignment L/R-value defined by a location within a BPEL
 * variable.
 */
public interface VariableVal extends From, To {
  /**
   * Get the name of the variable.
   *
   * @return variable name
   */
  String getVariable();

  /**
   * Set the name of the varName.
   *
   * @param varName varName name
   */
  void setVariable(String varName);

  /**
   * Get the (optional) message part.
   *
   * @return name of the message part, or <code>null</code>
   */
  String getPart();

  /**
   * Set the (optional) message part.
   *
   * @param part name of the message part, or <code>null</code>
   */
  void setPart(String part);

  /**
   * Get the (optional) location query.
   *
   * @return location query, or <code>null</code>
   */
  Query getLocation();

  /**
   * Set the (optional) location query.
   *
   * @param location location query, or <code>null</code>
   */
  void setLocation(Query location);
}
