package org.apache.ode.ra.transports.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for remote ODE server.
 */
public interface OdeRemote extends Remote {
  public OdeTransportPipeRemote newPipe() throws RemoteException ;
}
