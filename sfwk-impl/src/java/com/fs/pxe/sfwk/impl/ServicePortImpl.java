/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.ServicePort;

import java.util.Properties;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;


/**
 * Implementation of the {@link ServicePort} interface.
 */
class ServicePortImpl implements ServicePort {
  /** Name of the port. */
  private String _name;

  /** Type (portType) of the port. */
  private PortType _portType;

  /** Deployment properties */
  private Properties _properties;

  /**
   * Create a new instance with the supplied data.
   * 
   * @param name
   *          the internal name of the port (as distinct from the {@link QName}
   *          of the WSDL ServicePort)
   * @param portType
   *          the WSDL {@link PortType} for this
   */
  public ServicePortImpl(String name, PortType portType,  Properties properties) {
    _name = name;
    _portType = portType;
    _properties = properties;
  }

  public String getPortName() {
    return _name;
  }

  public PortType getPortType() {
    return _portType;
  }
  
  public String getDeploymentProperty(String propertyName){
  	return _properties.getProperty(propertyName);
  }

}
