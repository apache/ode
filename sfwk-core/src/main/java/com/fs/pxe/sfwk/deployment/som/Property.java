/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som;

import java.io.Serializable;


/**
 * Parse-time and load-time representation of a property for a <code>ServicePort</code> or
 * <code>Service</code>.
 * @see com.fs.pxe.sfwk.deployment.som.Port
 * @see com.fs.pxe.sfwk.deployment.som.Service
 */
public interface Property extends Serializable, Marshallable {
  
  public void setName(String s);
  
  /**
   * @return the name of the <code>Property</code>
   */
  public String getName();
  
  /**
   * Set the <code>Property</code>'s value.
   * @param s the value to set or <code>null</code> for none.
   */
  public void setValue(String s);
  
  /**
   * @return the value of the <code>Property</code> or <code>null</code> if none is
   * set.
   */
  public String getValue();
}
