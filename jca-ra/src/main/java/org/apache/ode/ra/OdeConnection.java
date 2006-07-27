/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

import javax.resource.cci.Connection;

/**
 * Interface providing access to a ODE domain from "external" entities such as
 * administrative consoles, protocol stacks, and client applications.
 */
public interface OdeConnection extends Connection {

  /**
   * Obtain access to a service provider belonging to this ODE domain through
   * its client session interface.
   *
   * @param serviceProviderUri URI of the Service Provider
   * @return possibly remote proxy to the Service Provider's client session interface.
   */
  public ServiceProviderSession createServiceProviderSession(String serviceProviderUri, Class sessionInterface)
          throws OdeConnectionException;

}
