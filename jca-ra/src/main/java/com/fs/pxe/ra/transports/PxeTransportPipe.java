/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra.transports;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Implemented by a communication transport to represent a single communication
 * pipe between client and server.
 */
public interface PxeTransportPipe extends Remote {

  /**
   * Get the domain identifier for the remote domain.
   * @return domain id
   *
   * @throws java.rmi.RemoteException DOCUMENTME
   */
  public String getDomainId()
          throws RemoteException;

  /**
   * Invoke a method on a service provier session.
   * @return DOCUMENTME
   *
   * @throws RemoteException RMI errors
   */
  Object invokeServiceProviderAPI(String spURI, Object sessionId, String name, Object[] args)
                                  throws RemoteException, InvocationTargetException;

  void closeServiceProviderSession(String spURI, Object sessionId)
          throws RemoteException;

  Object createServiceProviderSession(String serviceProviderUri, String className)
          throws RemoteException, ClassNotFoundException;

  void close() throws RemoteException;

}
