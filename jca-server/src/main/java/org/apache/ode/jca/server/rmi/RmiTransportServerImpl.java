/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jca.server.rmi;

import org.apache.ode.jca.server.ConnectionProvider;
import org.apache.ode.ra.transports.rmi.OdeRemote;
import org.apache.ode.ra.transports.rmi.OdeTransportPipeRemote;

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
public class RmiTransportServerImpl implements OdeRemote {
  private List<RmiPipeServerImpl> _pipes = new ArrayList<RmiPipeServerImpl>();
  private int _port = 1099;
  private Remote _remote;
  private ConnectionProvider _connProvider;
  private String _id;

  public RmiTransportServerImpl() {
  }

  public void setId(String id) {
    _id = id;
  }
  
  public void setConnectionProvider(ConnectionProvider connprovider) {
    _connProvider = connprovider;;
  }
  
  public void setPort(int port) {
    _port = port;
  }

  public int getPort() {
    return _port;
  }

  public synchronized void start() throws RemoteException {
    if (_id == null)
      throw new IllegalStateException("Must set id!");
    if (_connProvider == null)
      throw new IllegalStateException("Must set connection provider.");
    
    _remote = UnicastRemoteObject.exportObject(this,0);

    // Bind the RMI-server to the registry, creating one if necessary
    try {
      LocateRegistry.createRegistry(_port);
    } catch (Exception ex) {
      /*ignore*/
    }

    Registry registry = LocateRegistry.getRegistry(_port);
    registry.rebind(_id, _remote);
  }

  public synchronized void stop() throws RemoteException {
    UnicastRemoteObject.unexportObject(this, false);
  }


  public synchronized OdeTransportPipeRemote newPipe() throws RemoteException  {
    RmiPipeServerImpl pipe = new RmiPipeServerImpl(this, _connProvider.createConnectionObject(),_connProvider.getConnectionIntefaces());
    OdeTransportPipeRemote remote = (OdeTransportPipeRemote) UnicastRemoteObject.exportObject(pipe,0);
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
    
    _connProvider.destroyConnectionObject(pipe.target);

  }
}
