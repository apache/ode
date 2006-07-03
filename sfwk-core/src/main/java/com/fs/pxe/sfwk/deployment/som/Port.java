/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som;

import java.io.Serializable;
import javax.xml.namespace.QName;


/**
 * Parse-time and load-time description of a port on a service in a PXE system.
 */
public interface Port extends Serializable, Marshallable{
 
  /**
   * Set the name of the port.
   * @param s the name
   */
  public void setName(String s);
  
  /**
   * @return the name of the port
   */
  public String getName();
  
  /**
   * Set the type for this port.
   * @param type the <code>QName</code> of the WSDL <code>portType</code> for this
   * port.
   */
  public void setType(QName type);
  
  /**
   * @return the <code>QName</code> of the WSDL <code>portType</code> for this port.
   */
  public QName getType();
  
  /**
   * Add a port-specific property.
   * @param p the <code>Property</code> to add.
   */
  public void addProperty(Property p);
  
  /**
   * @return the <code>Channel</code> connected to this port or <code>null</code>
   * if none is.
   */
  public String getChannelRef();

  /**
   * @return get an array containing the <code>Property</code> settings for this
   * port.
   */
  public Property[] getProperties();
}
