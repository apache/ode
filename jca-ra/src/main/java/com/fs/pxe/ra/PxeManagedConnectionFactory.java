/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import com.fs.pxe.ra.transports.PxeTransport;
import com.fs.pxe.ra.transports.rmi.RMITransport;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * JCA {@link ManagedConnectionFactory} implementation.
 */
public class PxeManagedConnectionFactory implements ManagedConnectionFactory {
  private static final long serialVersionUID = 1L;
  private PrintWriter _logWriter;

  /** Default connection request information. */
  private PxeConnectionRequestInfo _defaultCRI = new PxeConnectionRequestInfo(null,"");

  public PxeManagedConnectionFactory() {
    try {
      setTransport(RMITransport.class.getName());
    } catch (ResourceException re) {
      //ignore (perhaps we should log)
    }
  }

  public void setTransport(String transportClassName) throws ResourceException {
    try {
      Class tclass = Class.forName(transportClassName);
      _defaultCRI.transport = (PxeTransport) tclass.newInstance();
    } catch (IllegalAccessException e) {
      ResourceException re = new ResourceException("Class-access error for transport class \"" + transportClassName + "\". ", e);
      throw re;
    } catch (InstantiationException e) {
      ResourceException re = new ResourceException("Error instantiating transport class \"" + transportClassName + "\". ", e );
      throw re;
    } catch (ClassNotFoundException e) {
      ResourceException re = new ResourceException("Transport class \"" + transportClassName + "\" not found in class path. ", e);
      throw re;

    }
  }

  public void setURL(String url) throws ResourceException {
    _defaultCRI.url = url;
  }

  public void setProperty(String key, String val) throws ResourceException {
    if (key.equals("URL"))
      setURL(val);
    else if (key.equals("Transport"))
      setTransport(val);
    else
      _defaultCRI.properties.setProperty(key,val);
  }

  public Object createConnectionFactory() throws ResourceException {
    return new PxeConnectionFactoryImpl(this, new PxeConnectionManager());
  }

  public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
    return new PxeConnectionFactoryImpl(this, connectionManager);
  }

  public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
    PxeConnectionRequestInfo cri = (PxeConnectionRequestInfo) (connectionRequestInfo != null ? connectionRequestInfo : _defaultCRI);

    if (cri.transport == null)
      throw new ResourceException("No transport.");

    try {
      return new PxeManagedConnectionImpl(cri.transport.createPipe(cri.url, cri.properties), subject, connectionRequestInfo);
    } catch (RemoteException ex) {
      ResourceException re = new ResourceException("Unable to create connection: " + ex.getMessage(), ex);
      throw re;
    }
  }

  public ManagedConnection matchManagedConnections(Set candidates, Subject subject, ConnectionRequestInfo connectionRequestInfo)
          throws ResourceException
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public PrintWriter getLogWriter() throws ResourceException {
    return _logWriter;
  }

  public void setLogWriter(PrintWriter printWriter) throws ResourceException {
    _logWriter = printWriter;
  }


}
