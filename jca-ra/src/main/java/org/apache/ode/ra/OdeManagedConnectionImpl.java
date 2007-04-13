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

import org.apache.ode.ra.transports.OdeTransportPipe;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * JCA {@link ManagedConnection} implementation.
 */
class OdeManagedConnectionImpl implements ManagedConnection {
  private final List<OdeConnectionImpl> _connections = new ArrayList<OdeConnectionImpl>();
  private final ConnectionEventListenerSupport _eventListenerSupport = new ConnectionEventListenerSupport();

  private OdeConnectionImpl _activeConnection;
  private PrintWriter _logWriter;

  /** Physical communication pipe. */
  private OdeTransportPipe _transportPipe;
  private Class<?> _connectionClasses[];

  OdeManagedConnectionImpl() {}

  public OdeManagedConnectionImpl(OdeTransportPipe pipe, Subject subject, ConnectionRequestInfo connectionRequestInfo) 
  throws ResourceException {
    _transportPipe = pipe;
    String[] classNames;
    try {
      classNames = _transportPipe.getConnectionClassNames();
    } catch (RemoteException e1) {
      throw new ResourceException("Unable to obtain interface names from server.", e1);
    }
    _connectionClasses = new Class[classNames.length];
    for (int i= 0; i < classNames.length; ++i)
      try {
        _connectionClasses[i] = Class.forName(classNames[i]);
      } catch (ClassNotFoundException e) {
        throw new ResourceException("Connection class " + classNames[i]  + " could not be found in classpath.");
      }
  }

  public void associateConnection(Object o) throws ResourceException {
    if (_activeConnection != null) {
      _activeConnection.associate(null);
    }
    _activeConnection = null;
    _activeConnection = (OdeConnectionImpl) o;
    _activeConnection.associate(this);
  }

  public void cleanup() throws ResourceException {
    if (_activeConnection != null) {
      _activeConnection.associate(null);
    }
    _activeConnection = null;
    _connections.clear();
  }

  public void destroy() throws ResourceException {
    cleanup();
  }

  public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo)
    throws ResourceException {
    OdeConnectionImpl conn = new OdeConnectionImpl(subject, connectionRequestInfo);
    _connections.add(conn);
    _activeConnection = conn;
    conn.associate(this);
    return Proxy.newProxyInstance(getClass().getClassLoader(),_connectionClasses,conn);
  }

  public LocalTransaction getLocalTransaction() throws ResourceException {
    throw new ResourceException("Not supported.");
  }

  public ManagedConnectionMetaData getMetaData() throws ResourceException {
    throw new ResourceException("Not supported.");
  }

  public XAResource getXAResource() throws ResourceException {
    throw new ResourceException("Not supported.");
  }

  public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
    _eventListenerSupport.addListener(connectionEventListener);
  }

  public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
    _eventListenerSupport.removeListener(connectionEventListener);
  }

  public PrintWriter getLogWriter() throws ResourceException {
    return _logWriter;
  }

  public void setLogWriter(PrintWriter logWriter) throws ResourceException {
    _logWriter = logWriter;
  }

  /**
   * Called by connection handle to indicate it has been closed.
   * @param odeConnection
   */
  void connectionClosed(OdeConnectionImpl odeConnection) {
    _eventListenerSupport.connectionClosed(new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED));
  }

  OdeTransportPipe getTransport() {
    return _transportPipe;
  }
}
