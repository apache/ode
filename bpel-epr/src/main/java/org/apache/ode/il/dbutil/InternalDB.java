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

package org.apache.ode.il.dbutil;

import org.apache.ode.il.config.OdeConfigProperties;

public class InternalDB extends Database {
    protected static final int CONNECTION_MAX_WAIT_MILLIS = 30000;
    protected static final int CONNECTION_MAX_IDLE_MINUTES = 5;

    protected DatabaseConnectionManager _connectionManager;
    
    public InternalDB(OdeConfigProperties props) {
        super(props);
    }
    
    @Override
    public synchronized void start() throws DatabaseConfigException {
        if (_started)
            return;

        _datasource = null;
        _connectionManager = null;

        initDataSource();
        _started = true;
    }
    
    @Override
    public synchronized void shutdown() {
        if (!_started)
            return;

        if (_connectionManager != null) {
            try {
                __log.debug("Stopping connection manager");
                _connectionManager.shutdown();
            } catch (Throwable t) {
                __log.warn("Exception while stopping connection manager: " + t.getMessage());
            } finally {
                _connectionManager = null;
            }
        }
        
        shutdownDB();
    
        _datasource = null;
        _started = false;
    }
    
    protected void shutdownDB() {}
    
    protected void initDataSource() throws DatabaseConfigException {
        __log.info(__msgs.msgOdeUsingInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass()));
        initInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass(),
                _odeConfig.getDbInternalUserName(), _odeConfig.getDbInternalPassword());
    }

    protected void initInternalDb(String url, String driverClass, String username,String password) throws DatabaseConfigException {
        _connectionManager = new DatabaseConnectionManager(_txm,_odeConfig);
        _connectionManager.init(url, driverClass, username, password);
        _datasource = _connectionManager.getDataSource();
     }
}
