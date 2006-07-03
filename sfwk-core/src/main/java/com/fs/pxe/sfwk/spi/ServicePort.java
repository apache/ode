/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import javax.wsdl.PortType;

/**
 */
public interface ServicePort {

  /**
   * Get the name of this port.  (Note that this is an <i>internal</i> name.)
   *
   * @return port name
   */
  String getPortName();

  /**
   * Get the (WSDL) type of this port.
   *
   * @return the {@link javax.wsdl.PortType} that describes this port.
   */
  PortType getPortType();

  /**
   * Get the name of a deployment property as configured in the service
   * deployment descriptor
   *
   * @param propertyName name of the property
   *
   * @return value of the property
   */
  String getDeploymentProperty(String propertyName);

}
