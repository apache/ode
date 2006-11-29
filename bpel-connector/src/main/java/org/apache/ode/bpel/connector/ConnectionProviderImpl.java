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

package org.apache.ode.bpel.connector;

import org.apache.ode.bpel.engine.BpelManagementFacadeImpl;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.jca.server.ConnectionProvider;

/**
 * Implementation of the {@link org.apache.ode.jca.server.ConnectionProvider}
 * interface: provides {@link org.apache.ode.bpel.pmapi.BpelManagementFacade}
 * objects. 
 */
class ConnectionProviderImpl implements ConnectionProvider {

  private BpelServer _server;
  private ProcessStore _store;
  
  ConnectionProviderImpl(BpelServer server, ProcessStore store) {
    _server = server;
    _store = store;
  }
  
  public String [] getConnectionIntefaces() {
    return new String[] { "org.apache.ode.bpel.jca.clientapi.ProcessManagementConnection" };
  }

  public Object createConnectionObject() {
    return new BpelManagementFacadeImpl(_server, _store);
  }

  /**
   * Does nothing.
   */
  public void destroyConnectionObject(Object cobj) {
    ((BpelManagementFacade)cobj).toString();
  }


}
