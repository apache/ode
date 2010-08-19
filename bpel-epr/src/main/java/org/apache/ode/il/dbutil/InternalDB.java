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

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.derby.jdbc.EmbeddedDriver;
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

public class InternalDB extends Database {

    protected GenericConnectionManager _connectionManager;

    protected boolean _needDerbyShutdown;

    protected String _derbyUrl;
    
    public InternalDB(OdeConfigProperties props) {
        super(props);
    }
    
    @Override
    public synchronized void start() throws DatabaseConfigException {
        if (_started)
            return;

        _needDerbyShutdown = false;
        _datasource = null;
        _connectionManager = null;

        initDataSource();
        _started = true;
    }
    
    @Override
    public synchronized void shutdown() {
        if (!_started)
            return;

        if (_connectionManager != null)
            try {
                __log.debug("Stopping connection manager");
                _connectionManager.doStop();
            } catch (Throwable t) {
                __log.warn("Exception while stopping connection manager: " + t.getMessage());
            } finally {
                _connectionManager = null;
            }

        if (_needDerbyShutdown) {
            __log.debug("shutting down derby.");
            EmbeddedDriver driver = new EmbeddedDriver();
            try {
                driver.connect(_derbyUrl + ";shutdown=true", new Properties());
            } catch (SQLException ex) {
                // Shutdown will always return an exeption!
                if (ex.getErrorCode() != 45000)
                    __log.error("Error shutting down Derby: " + ex.getErrorCode(), ex);

            } catch (Throwable ex) {
                __log.debug("Error shutting down Derby.", ex);
            }
        }

        _needDerbyShutdown = false;
        _datasource = null;
        _started = false;
    }
    
    protected void initDataSource() throws DatabaseConfigException {
        __log.info(__msgs.msgOdeUsingInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass()));
        initInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass(),
                _odeConfig.getDbInternalUserName(), _odeConfig.getDbInternalPassword());
    }

    protected void initInternalDb(String url, String driverClass, String username,String password) throws DatabaseConfigException {

        __log.debug("Creating connection pool for " + url + " with driver " + driverClass);
        if (!(_txm instanceof RecoverableTransactionManager)) {
            throw new RuntimeException("TransactionManager is not recoverable.");
        }

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

        _connectionManager = new GenericConnectionManager(
                    transactionSupport,
                    poolingSupport,
                    null,
                    connectionTracker,
                    (RecoverableTransactionManager) _txm,
                    getClass().getName(),
                    getClass().getClassLoader());


        try {
            javax.resource.spi.ManagedConnectionFactory mcf = null;
            String mcfClass = _odeConfig.getDbInternalMCFClass();
            if (mcfClass != null) {
                Properties dbInternalMCFProps = _odeConfig.getDbInternalMCFProperties();
                if (__log.isDebugEnabled()) {
                    __log.debug("Using internal DB MCF " + mcfClass + " " + dbInternalMCFProps);
                }
                mcf = (javax.resource.spi.ManagedConnectionFactory) Class.forName(mcfClass).newInstance();
                BeanUtils.copyProperties(mcf, dbInternalMCFProps);
            } else {
                if (__log.isDebugEnabled()) {
                    __log.debug("Using internal DB JDBCDriverMCF");
                }
                JDBCDriverMCF mcf2 = new JDBCDriverMCF();
                mcf = mcf2;
                mcf2.setDriver(driverClass);
                mcf2.setConnectionURL(url);
                if (username != null) {
                    mcf2.setUserName(username);
                }
                if (password != null) {
                    mcf2.setPassword(password);
                }
            }
            _connectionManager.doStart();
            _datasource = (DataSource) mcf.createConnectionFactory(_connectionManager);
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(url);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }
    }
}
