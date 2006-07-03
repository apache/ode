/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som;

import java.io.Serializable;
import java.net.URI;


/**
 * Parse-time and load-time definition of a service in a PXE system.
 */
public interface Service extends Serializable, Marshallable{
  
  /**
   * Set the name of the service.
   * @param name the name to set.
   */
  public void setName(String name);
  
  /**
   * @return the name of the service.
   */
  public String getName();
  
  /**
   * Set the (domain-defined) URI of the <code>ServiceProvider</code> that provides
   * this service.
   * @param u the <code>URI</code>.
   * @see com.fs.pxe.sfwk.spi.ServiceProvider
   * @see com.fs.pxe.sfwk.bapi.DomainNode
   */
  public void setProviderUri(URI u);
  
  /**
   * @return the (domain-defined) URI of the <code>ServiceProvider</code> that
   * provides this service.
   */
  public URI getProviderUri();

  /**
   * Add a specific configuration <code>Property</code> for this service.
   * @param p the <code>Property</code> to add
   */
  public void addProperty(Property p);
  
  /**
   * @return an array containing the <code>Property</code> settings for this service.
   */
  public Property[] getProperties();
  
  /**
   * Add a <code>ServicePort</code> object to the list of ports exported by this service.
   * @param p the <code>ServicePort</code> to add.
   */
  public void addImportedPort(Port p);
  
  /**
   * @return an array of the <code>ServicePort</code> objects that represent the imported
   * ports on the service.
   */
  public Port[] getImportedPorts();
  
  /**
   * Add a <code>ServicePort</code> object to the list of ports exported by this service.
   * @param p the <code>ServicePort</code> to add.
   */
  public void addExportedPort(Port p);

  /**
   * @return an array of the <code>ServicePort</code> objects that represent the imported
   * ports on the service.
   */  
  public Port[] getExportedPorts();
  
}
