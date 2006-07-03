/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som;

import java.io.Serializable;
import java.net.URI;


/**
 * A parse-time and load-time representation of a system descriptor.
 */
public interface SystemDescriptor extends Serializable, Marshallable {
  
  /**
   * Add a <code>Channel</code> to the system.
   * @param c the <code>Channel</code> to add.
   */
  public void addChannel(Channel c);
  
  /**
   * @return an array containing the <code>Channel</code>s in the system.
   */
  public Channel[] getChannels();
  
  /**
   * Add a <code>Service</code> to the system.
   * @param s
   */
  public void addService(Service s);
  
  /**
   * @return an array containing the <code>Service</code>s in the system.
   */
  public Service[] getServices();
  
  /**
   * Set the name of the system.  This name will be used as a unique identified for
   * the system, so it should be unique among the other systems to be deployed within
   * the same domain.
   * @param s the name.
   */
  public void setName(String s);
  
  /**
   * @return the name of the system.
   */
  public String getName();
  
  /**
   * Set the URI handle of the root WSDL document for the system.  This URI will be
   * used to look-up the WSDL document within the root resource repository for the
   * system.
   * @param u the <code>URI</code> for the root WSDL document
   * @see com.fs.pxe.sfwk.rr.ResourceRepository
   */
  public void setWsdlUri(URI u);

  /**
   * @return the <code>URI</code> of the root WSDL document for the system. 
   */
  public URI getWsdlUri(); 
    
}
