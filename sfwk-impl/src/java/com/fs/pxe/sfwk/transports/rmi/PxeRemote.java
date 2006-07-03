package com.fs.pxe.sfwk.transports.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for remote PXE server.
 */
public interface PxeRemote extends Remote {
  public PxeTransportPipeRemote newPipe() throws RemoteException ;
}
