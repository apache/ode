/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * Default {@link ConnectionManager} for use in non-managed environments.
 */
class PxeConnectionManager implements ConnectionManager {
  public Object allocateConnection(ManagedConnectionFactory managedConnectionFactory, ConnectionRequestInfo connectionRequestInfo)
          throws ResourceException {
    PxeManagedConnectionImpl managedConnection = (PxeManagedConnectionImpl) managedConnectionFactory.createManagedConnection(null, connectionRequestInfo);
    return managedConnection.getConnection(null, connectionRequestInfo);
  }
}
