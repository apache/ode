/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import com.fs.pxe.ra.transports.PxeTransportPipe;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * JCA {@link ManagedConnection} implementation.
 */
class PxeManagedConnectionImpl implements ManagedConnection {
  private final List<PxeConnection> _connections = new ArrayList<PxeConnection>();
  private final ConnectionEventListenerSupport _eventListenerSupport = new ConnectionEventListenerSupport();

  private PxeConnectionImpl _activeConnection;
  private PrintWriter _logWriter;
  private ConnectionRequestInfo _cri;
  private Subject _subject;

  /** Physical communication pipe. */
  private PxeTransportPipe _transportPipe;

  PxeManagedConnectionImpl() {}

  public PxeManagedConnectionImpl(PxeTransportPipe pipe, Subject subject, ConnectionRequestInfo connectionRequestInfo) {
    _transportPipe = pipe;
    _subject = subject;
    _cri = connectionRequestInfo;

  }

  public void associateConnection(Object o) throws ResourceException {
    if (_activeConnection != null) {
      _activeConnection.associate(null);
    }
    _activeConnection = null;
    _activeConnection = (PxeConnectionImpl) o;
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
    PxeConnectionImpl conn = new PxeConnectionImpl(subject, connectionRequestInfo);
    _connections.add(conn);
    conn.associate(this);
    return conn;
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
   * @param pxeConnection
   */
  void connectionClosed(PxeConnectionImpl pxeConnection) {
    assert _activeConnection == pxeConnection;
    _eventListenerSupport.connectionClosed(new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED));
  }

  PxeTransportPipe getTransport() {
    return _transportPipe;
  }

}
