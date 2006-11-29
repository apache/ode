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

import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.jca.server.rmi.RmiTransportServerImpl;

public class BpelServerConnector {

    private BpelServer _server;

    private ProcessStore _store;

    private RmiTransportServerImpl _transport;

    public BpelServerConnector() {
        _transport = new RmiTransportServerImpl();
        _transport.setId("ode/BpelEngine");
    }

    public void setBpelServer(BpelServer server) {
        _server = server;
    }

    public void setProcessStore(ProcessStore store) {
        _store = store;
    }

    public void start() throws Exception {
        if (_server == null)
            throw new IllegalStateException("Server not set!");
        if (_store == null)
            throw new IllegalStateException("Store not set!");

        _transport.setConnectionProvider(new ConnectionProviderImpl(_server,_store));
        _transport.start();

    }

    public void shutdown() throws Exception {
        _transport.stop();
    }

    public void setPort(int connectorPort) {
        _transport.setPort(connectorPort);
    }

    public void setId(String connectorName) {
        _transport.setId(connectorName);
    }

}
