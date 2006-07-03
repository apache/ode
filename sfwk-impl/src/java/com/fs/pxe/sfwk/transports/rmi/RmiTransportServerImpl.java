/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.transports.rmi;

import com.fs.pxe.sfwk.bapi.DomainNode;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-side of the RMI transport. Manages a collection of communication
 * "pipes", each represented by its own remote object.
 */
public class RmiTransportServerImpl implements PxeRemote {
  private DomainNode _domainNode;
  private List<RmiPipeServerImpl> _pipes = new ArrayList<RmiPipeServerImpl>();
  private int _port = 1099;
  private Remote _remote;

  public RmiTransportServerImpl(DomainNode domainNode) {
    _domainNode = domainNode;
  }

  public void setPort(int port) {
    _port = port;
  }

  public int getPort() {
    return _port;
  }

  public synchronized void start() throws RemoteException {
    _remote = UnicastRemoteObject.exportObject(this,0);

    // Bind the RMI-server to the registry, creating one if necessary
    try {
      LocateRegistry.createRegistry(_port);
    } catch (Exception ex) {
      /*ignore*/
    }

    Registry registry = LocateRegistry.getRegistry(_port);
    registry.rebind(_domainNode.getDomainId().toString(), _remote);
  }

  public synchronized void stop() throws RemoteException {
    UnicastRemoteObject.unexportObject(this, false);
  }


  public synchronized PxeTransportPipeRemote newPipe() throws RemoteException  {
    RmiPipeServerImpl pipe = new RmiPipeServerImpl(this, _domainNode);
    PxeTransportPipeRemote remote = (PxeTransportPipeRemote) UnicastRemoteObject.exportObject(pipe,0);
    pipe.remote = remote;
    _pipes.add(pipe);
    return remote;
  }

  void pipeClosed(RmiPipeServerImpl pipe) {
    try {
      UnicastRemoteObject.unexportObject(pipe.remote,false);
    } catch (RemoteException re) {
      // ignore
    }

    synchronized(this) {
      _pipes.remove(pipe);
    }
  }
}
