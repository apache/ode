/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.ra;

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
class OdeConnectionFactoryImpl implements OdeConnectionFactory {
  private static final long serialVersionUID = 1L;
  
  private Reference _reference;
  private ConnectionManager _manager;
  private ManagedConnectionFactory _managedConnectionFactory;

  OdeConnectionFactoryImpl(ManagedConnectionFactory mcf, ConnectionManager manager) {
    _manager = manager;
    _managedConnectionFactory = mcf;
  }

  public Connection getConnection() throws ResourceException {
    return (Connection) _manager.allocateConnection(_managedConnectionFactory,  null);
  }

  public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
    return (Connection) _manager.allocateConnection(_managedConnectionFactory, null);
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
