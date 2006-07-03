/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

/**
 */
class PxeConnectionFactoryImpl implements PxeConnectionFactory {
  private Reference _reference;
  private ConnectionManager _manager;
  private ManagedConnectionFactory _managedConnectionFactory;

  PxeConnectionFactoryImpl(ManagedConnectionFactory mcf, ConnectionManager manager) {
    _manager = manager;
    _managedConnectionFactory = mcf;
  }

  public Connection getConnection() throws ResourceException {
    return (PxeConnection) _manager.allocateConnection(_managedConnectionFactory,  null);
  }

  public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
    return (PxeConnection) _manager.allocateConnection(_managedConnectionFactory, null);
  }

  public ResourceAdapterMetaData getMetaData() throws ResourceException {
    throw new ResourceException("Unsupported.");
  }

  public RecordFactory getRecordFactory() throws ResourceException {
    throw new ResourceException("Unsupported.");
  }

  public void setReference(Reference reference) {
    _reference = reference;
  }

  public Reference getReference() throws NamingException {
    return _reference;
  }
}
