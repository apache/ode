/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * Default {@link ConnectionManager} for use in non-managed environments.
 */
class OdeConnectionManager implements ConnectionManager {
  private static final long serialVersionUID = 1L;

  public Object allocateConnection(ManagedConnectionFactory managedConnectionFactory, ConnectionRequestInfo connectionRequestInfo)
          throws ResourceException {
    OdeManagedConnectionImpl managedConnection = (OdeManagedConnectionImpl) managedConnectionFactory.createManagedConnection(null, connectionRequestInfo);
    return managedConnection.getConnection(null, connectionRequestInfo);
  }
}
