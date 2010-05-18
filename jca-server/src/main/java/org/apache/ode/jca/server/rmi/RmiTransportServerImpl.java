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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.jca.server.ConnectionProvider;
import org.apache.ode.ra.transports.rmi.OdeRemote;
import org.apache.ode.ra.transports.rmi.OdeTransportPipeRemote;

/**
 * Server-side of the RMI transport. Manages a collection of communication
 * "pipes", each represented by its own remote object.
 */
public class RmiTransportServerImpl implements OdeRemote {
    private static final Log __log = LogFactory.getLog(RmiTransportServerImpl.class);

    private List<RmiPipeServerImpl> _pipes = new ArrayList<RmiPipeServerImpl>();

    private int _port = 1099;

    private Remote _remote;

    private ConnectionProvider _connProvider;

    private String _id;

    private Registry _registry;

    public RmiTransportServerImpl() {
    }

    public void setId(String id) {
        _id = id;
    }

    public void setConnectionProvider(ConnectionProvider connprovider) {
        _connProvider = connprovider;
        ;
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

        _remote = UnicastRemoteObject.exportObject(this, 0);

        // Bind the RMI-server to the registry, creating one if necessary
        try {
            _registry = LocateRegistry.createRegistry(_port);
            __log.debug("Created registry on port " + _port);
        } catch (Exception ex) {
            __log.debug("Could not create registry on port " + _port + " (perhaps it's already there)");
            /* ignore */
        }

        Registry registry = LocateRegistry.getRegistry(_port);
        
        registry.rebind(_id, _remote);
        
        __log.debug("Bound JCA server as \"" + _id + "\" on registry port " + _port);
    }

    public synchronized void stop() throws RemoteException {
        for (RmiPipeServerImpl pipe: _pipes) {
            unexport(pipe);
        }
        if (_registry != null) {
            unexport(_registry);
        }
        unexport(this);
    }

    public synchronized OdeTransportPipeRemote newPipe() throws RemoteException {
        RmiPipeServerImpl pipe = new RmiPipeServerImpl(this, _connProvider.createConnectionObject(), _connProvider
                .getConnectionIntefaces());
        OdeTransportPipeRemote remote = (OdeTransportPipeRemote) UnicastRemoteObject.exportObject(pipe, 0);
        pipe.remote = remote;
        _pipes.add(pipe);
        return remote;
    }

    void pipeClosed(RmiPipeServerImpl pipe) {
        if (__log.isDebugEnabled())
            __log.debug("Closing RMI pipe " + pipe);
        unexport(pipe);
        synchronized (this) {
            _pipes.remove(pipe);
        }
        _connProvider.destroyConnectionObject(pipe.target);
    }

    void unexport(Remote remote) {
        try {
            UnicastRemoteObject.unexportObject(remote, false);
        } catch (Exception e) {
            // ignore
        }
    }
}
