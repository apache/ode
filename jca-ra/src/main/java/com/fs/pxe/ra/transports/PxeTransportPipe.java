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
   * Get the names of the client-side connection interfaces. 
   * @return
   */
  String[] getConnectionClassNames() throws RemoteException;
  
  /**
   * Invoke a method on a service provier session.
   * @return DOCUMENTME
   *
   * @throws RemoteException RMI errors
   */
  Object invokeConnectionMethod(String name, Object[] args)
    throws RemoteException, InvocationTargetException;


  void close() throws RemoteException;

}
