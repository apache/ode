/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import com.fs.pxe.ra.transports.PxeTransportPipe;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.Reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 */
class PxeConnectionImpl implements PxeConnection {

  private final Subject _subject;
  private final ConnectionRequestInfo _cri;

  private PxeManagedConnectionImpl _managedConnection;

  public PxeConnectionImpl(Subject subject, ConnectionRequestInfo connectionRequestInfo) {
    _subject = subject;
    _cri = connectionRequestInfo;
  }

  public void close() throws ResourceException {
    mc().connectionClosed(this);
  }

  public Interaction createInteraction() throws ResourceException {
    throw new ResourceException("Unsupported.");
  }

  public LocalTransaction getLocalTransaction() throws ResourceException {
    throw new ResourceException("Unsupported.");
  }

  public ConnectionMetaData getMetaData() throws ResourceException {
    throw new ResourceException("Unsupported.");
  }

  public ResultSetInfo getResultSetInfo() throws ResourceException {
    throw new ResourceException("Unsupported.");
  }

  public ServiceProviderSession createServiceProviderSession(String serviceProviderUri, Class sessionInterface)
          throws PxeConnectionException {

    Object sessionId;
    try {
      sessionId = xport().createServiceProviderSession(serviceProviderUri, sessionInterface.getName());
    } catch (RemoteException re) {
      throw new PxeConnectionException("Communication Error." , re);
    } catch (Exception ex) {
      throw new PxeConnectionException("Unable to create ServiceProvider session.", ex);
    }
    InvocationHandler handler = new SPSessionImpl(serviceProviderUri, sessionId);

    return (ServiceProviderSession) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[] { sessionInterface, ServiceProviderSession.class }, handler);
  }

  /**
   * Associate with a managed conneciton, or clear the association.
   */
  void associate(PxeManagedConnectionImpl mconn) {
    _managedConnection = mconn;
  }

  private final PxeManagedConnectionImpl mc() throws ResourceException {
    if (_managedConnection == null)
      throw new PxeConnectionException("Not connected.");

    return _managedConnection;
  }

  private final PxeTransportPipe xport() throws ResourceException {
    return mc().getTransport();
  }
  /**
   * Invocation handler for the dynamic proxy.
   */
  class SPSessionImpl implements InvocationHandler {
    String _spURI;
    Object _sessionId;

    SPSessionImpl(String spURI, Object sessionId) {
      _spURI = spURI;
      _sessionId = sessionId;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
                  throws Throwable {

      if (method.getName().equals("close")) {
        if (_sessionId != null)
          mc().getTransport().closeServiceProviderSession(_spURI, _sessionId);
        _sessionId = null;
        return null;
      }

      if (_sessionId == null)
        throw new PxeConnectionException("Session is closed.");

      try {
        String methodSig = Reflect.generateMethodSignature(method);
        return mc().getTransport().invokeServiceProviderAPI(_spURI, _sessionId, methodSig, args);
      } catch (RuntimeException pe) {
        pe.printStackTrace();
        throw new PxeConnectionException("Unexpected RuntimeException", pe);
      } catch (RemoteException re) {
        re.printStackTrace();
        throw new PxeConnectionException("Unexpected RemoteException." ,re);
      } catch (InvocationTargetException ite) {
        throw ite.getTargetException();
      }
    }

    public String toString() {
      return ObjectPrinter.toString(this, new Object[] { "spURI", _spURI, "sessionId", _sessionId}  );
    }
  }

}
