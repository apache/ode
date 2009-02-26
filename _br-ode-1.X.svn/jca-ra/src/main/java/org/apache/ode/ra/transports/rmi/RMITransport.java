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

package org.apache.ode.ra.transports.rmi;

import org.apache.ode.ra.transports.OdeTransport;
import org.apache.ode.ra.transports.OdeTransportPipe;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * A very simple RMI-based communication transport.
 */
public class RMITransport implements OdeTransport {

  public OdeTransportPipe createPipe(String url, Properties properties) throws RemoteException {
    OdeRemote remoteServer;
    try {
      remoteServer = (OdeRemote) Naming.lookup(url);
    } catch (MalformedURLException e) {
      throw new RemoteException("Invalid URL: "  + url, e);
    } catch (NotBoundException e) {
      throw new RemoteException("Unable to connect to: " + url, e);
    } catch (ClassCastException cce) {
      throw new RemoteException("Protocol error: unexpected remote object type!", cce);
    }

    return remoteServer.newPipe();
  }

}
