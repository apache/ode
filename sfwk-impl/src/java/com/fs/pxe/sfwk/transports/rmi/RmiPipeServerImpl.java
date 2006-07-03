/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */


package com.fs.pxe.sfwk.transports.rmi;

import com.fs.pxe.sfwk.bapi.DomainNode;
import com.fs.pxe.sfwk.spi.PxeException;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;


/**
 * Implementation of an RMI-based transport pipe.
 */
class RmiPipeServerImpl implements PxeTransportPipeRemote {

  private DomainNode _domain;
  private RmiTransportServerImpl _server;

  final long createTime = System.currentTimeMillis();
  long lastActivityTime = createTime;
  PxeTransportPipeRemote remote;

  /** Constructor. */
  public RmiPipeServerImpl(RmiTransportServerImpl server, DomainNode domainNode) {
    _server = server;
    _domain = domainNode;
  }

  public String getDomainId()
                           throws RemoteException {
    return _domain.getDomainId();
  }

  public void closeServiceProviderSession(String spURI, Object sessionId) throws RemoteException {
    _domain.closeServiceProviderSession(spURI,  sessionId);
  }

  public Object createServiceProviderSession(String serviceProviderUri, String interactionClass)
          throws RemoteException,ClassNotFoundException {
    lastActivityTime = System.currentTimeMillis();
    Class cls = Class.forName(interactionClass);
    return _domain.createServiceProviderSession(serviceProviderUri, cls);
  }

  public Object invokeServiceProviderAPI(String spURI, Object sessionId, String name, Object[] args)
          throws RemoteException, PxeException, InvocationTargetException {
    lastActivityTime = System.currentTimeMillis();
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    Thread.currentThread()
          .setContextClassLoader(getClass().getClassLoader());
    try {
      return _domain.onServiceProviderInvoke(spURI, sessionId, name, args);
    } finally {
      Thread.currentThread().setContextClassLoader(old);
    }
  }

  public void close() {
    _server.pipeClosed(this);
    _domain = null;
    remote = null;
  }
}
