/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashSet;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.Reflect;

/**
 */
class OdeConnectionImpl implements InvocationHandler, Connection {

  private final Subject _subject;

  private final ConnectionRequestInfo _cri;

  private OdeManagedConnectionImpl _managedConnection;

  private HashSet<Method> _myMethods = new HashSet<Method>();

  public OdeConnectionImpl(Subject subject,
      ConnectionRequestInfo connectionRequestInfo) {
    _subject = subject;
    _cri = connectionRequestInfo;
    for (Method m : Connection.class.getMethods())
      _myMethods.add(m);
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

  /**
   * Associate with a managed conneciton, or clear the association.
   */
  void associate(OdeManagedConnectionImpl mconn) {
    _managedConnection = mconn;
  }

  private final OdeManagedConnectionImpl mc() throws ResourceException {
    if (_managedConnection == null)
      throw new OdeConnectionException("Not connected.");

    return _managedConnection;
  }

  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    // If this is a local method, we do it.
    if (_myMethods.contains(method))
      try {
        return method.invoke(this, args);
      } catch (InvocationTargetException ite) {
        throw ite.getTargetException();
      }

    // otherwise, we have to go remote.
    try {
      String methodSig = Reflect.generateMethodSignature(method);
      return mc().getTransport().invokeConnectionMethod(methodSig, args);
    } catch (RuntimeException pe) {
      pe.printStackTrace();
      throw new OdeConnectionException("Unexpected RuntimeException", pe);
    } catch (RemoteException re) {
      re.printStackTrace();
      throw new OdeConnectionException("Unexpected RemoteException.", re);
    } catch (InvocationTargetException ite) {
      throw ite.getTargetException();
    }
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {});
  }

}
