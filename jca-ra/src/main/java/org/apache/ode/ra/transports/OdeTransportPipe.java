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
package org.apache.ode.ra.transports;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Implemented by a communication transport to represent a single communication
 * pipe between client and server.
 */
public interface OdeTransportPipe extends Remote {

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
