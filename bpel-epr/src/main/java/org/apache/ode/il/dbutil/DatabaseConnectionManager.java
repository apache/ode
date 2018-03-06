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


import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.ode.il.config.OdeConfigProperties;
import org.tranql.connector.jdbc.JDBCDriverMCF;

public class DatabaseConnectionManager {

    private static final long serialVersionUID = 1L;
    private static final Logger __log = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private static final Messages __msgs = Messages.getMessages(Messages.class);
    private static final int CONNECTION_MAX_WAIT_MILLIS = 30000;
    private static final int CONNECTION_MAX_IDLE_MINUTES = 5;
    private GenericConnectionManager _connectionManager;
    private TransactionManager _txm = null;
    private String _url = null;
    private OdeConfigProperties _odeConfig = null;
    private DataSource _dataSource = null;

    public DatabaseConnectionManager(TransactionManager txm, OdeConfigProperties odeConfig) {
        _txm = txm;
        _odeConfig = odeConfig;
    }

    public void init(String url, String driverClass, String username, String password) throws DatabaseConfigException {
        __log.debug("Creating connection pool for " + url + " with driver " + driverClass);

        if (!(_txm instanceof RecoverableTransactionManager)) {
            throw new RuntimeException("TransactionManager is not recoverable.");
        }

        _url = url;
        TransactionSupport transactionSupport = LocalTransactions.INSTANCE;
        ConnectionTracker connectionTracker = new ConnectionTrackingCoordinator();

        PoolingSupport poolingSupport = new SinglePool(
                _odeConfig.getPoolMaxSize(),
                _odeConfig.getPoolMinSize(),
                CONNECTION_MAX_WAIT_MILLIS,
                CONNECTION_MAX_IDLE_MINUTES,
                true, // match one
                false, // match all
                false); // select one assume match

        JDBCDriverMCF mcf = new JDBCDriverMCF();

        _connectionManager = new GenericConnectionManager(
                transactionSupport,
                poolingSupport,
                null,
                connectionTracker,
                (RecoverableTransactionManager) _txm,
                mcf,
                getClass().getName(),
                getClass().getClassLoader());


        try {
            mcf.setDriver(driverClass);
            mcf.setConnectionURL(url);
            if (username != null) {
                mcf.setUserName(username);
            }
            if (password != null) {
                mcf.setPassword(password);
            }
            _connectionManager.doStart();
            _dataSource = (DataSource) _connectionManager.createConnectionFactory();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(url);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }
    }

    public void shutdown() throws DatabaseConfigException {
        try {
            _connectionManager.doStop();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(_url);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }
    }

    public DataSource getDataSource() {
        return _dataSource;
    }
}
