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

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.LoggingInterceptor;
import org.tranql.connector.jdbc.JDBCDriverMCF;

/**
 * Does the dirty work of setting up / obtaining a DataSource based on the configuration in the {@link OdeConfigProperties} object.
 *
 */
public class Database {
    private static final Log __log = LogFactory.getLog(Database.class);

    private static final Log __logSql = LogFactory.getLog("org.apache.ode.sql");

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private static final int CONNECTION_MAX_WAIT_MILLIS = 30000;

    private static final int CONNECTION_MAX_IDLE_MINUTES = 5;

    private OdeConfigProperties _odeConfig;

    private boolean _started;

    private GenericConnectionManager _connectionManager;

    private TransactionManager _txm;

    private DataSource _datasource;

    private File _workRoot;

    private boolean _needDerbyShutdown;

    private String _derbyUrl;

    public Database(OdeConfigProperties props) {
        if (props == null)
            throw new NullPointerException("Must provide a configuration.");

        _odeConfig = props;
    }

    public void setWorkRoot(File workRoot) {
        _workRoot = workRoot;
    }

    public void setTransactionManager(TransactionManager txm) {
        _txm = txm;
    }

    public synchronized void start() throws DatabaseConfigException {
        if (_started)
            return;

        _needDerbyShutdown = false;
        _datasource = null;
        _connectionManager = null;

        initDataSource();
        _started = true;
    }

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

    public DataSource getDataSource() {
        //return __logSql.isDebugEnabled() ? new LoggingDataSourceWrapper(_datasource, __logSql) : _datasource;
        return __logSql.isDebugEnabled() ? LoggingInterceptor.createLoggingDS(_datasource, __logSql) : _datasource;
    }

    private void initDataSource() throws DatabaseConfigException {
        switch (_odeConfig.getDbMode()) {
        case EXTERNAL:
            initExternalDb();
            break;
        case EMBEDDED:
            initEmbeddedDb();
            break;
        case INTERNAL:
            initInternalDb();
            break;
        default:
            break;
        }
    }

    private void initExternalDb() throws DatabaseConfigException {
        try {
            _datasource = (DataSource) lookupInJndi(_odeConfig.getDbDataSource());
            __log.info(__msgs.msgOdeUsingExternalDb(_odeConfig.getDbDataSource()));
        } catch (Exception ex) {
            String msg = __msgs.msgOdeInitExternalDbFailed(_odeConfig.getDbDataSource());
            __log.error(msg, ex);
            throw new DatabaseConfigException(msg, ex);
        }
    }

    private void initInternalDb() throws DatabaseConfigException {
        __log.info(__msgs.msgOdeUsingInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass()));
        initInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass(),
                _odeConfig.getDbInternalUserName(), _odeConfig.getDbInternalPassword());
    }

    private void initInternalDb(String url, String driverClass, String username,String password) throws DatabaseConfigException {

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

    /**
     * Initialize embedded (DERBY) database.
     */
    private void initEmbeddedDb() throws DatabaseConfigException {

        String db = _odeConfig.getDbEmbeddedName();

        String url = "jdbc:derby:" + _workRoot + "/" + db ;
        __log.info("Using Embedded Derby: " + url);
        _derbyUrl = url;
        initInternalDb(url, org.apache.derby.jdbc.EmbeddedDriver.class.getName(),"sa",null);
        _needDerbyShutdown = true;
    }

    @SuppressWarnings("unchecked")
    private <T> T lookupInJndi(String objName) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            InitialContext ctx = null;
            try {
                ctx = new InitialContext();
                return (T) ctx.lookup(objName);
            } finally {
                if (ctx != null)
                    try {
                        ctx.close();
                    } catch (Exception ex1) {
                        __log.error("Error closing JNDI connection.", ex1);
                    }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public BpelDAOConnectionFactoryJDBC createDaoCF() throws DatabaseConfigException  {
        String pClassName = _odeConfig.getDAOConnectionFactory();

        __log.info(__msgs.msgOdeUsingDAOImpl(pClassName));

        BpelDAOConnectionFactoryJDBC cf;
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(pClassName);
            cf = (BpelDAOConnectionFactoryJDBC) clazz.newInstance();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(pClassName);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }

        cf.setDataSource(getDataSource());
        cf.setTransactionManager(_txm);
        cf.init(_odeConfig.getProperties());
        return cf;
    }

}
