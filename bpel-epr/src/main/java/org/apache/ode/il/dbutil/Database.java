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

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.bpel.BpelDAOConnectionFactory;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.LoggingInterceptor;


/**
 * Does the dirty work of setting up / obtaining a DataSource based on the configuration in the {@link OdeConfigProperties} object.
 *
 */
public class Database {
    private static final Log __log = LogFactory.getLog(Database.class);

    private static final Log __logSql = LogFactory.getLog("org.apache.ode.sql");

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private OdeConfigProperties _odeConfig;

    private boolean _started;

    private DatabaseConnectionManager _connectionManager;

    private TransactionManager _txm;

    private DataSource _datasource;

    private File _workRoot;

    private boolean _needShutdown;

    private EmbeddedDatabase _embeddedDB;

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

        _needShutdown = false;
        _datasource = null;
        _connectionManager = null;

        initDataSource();
        _started = true;
    }

    public synchronized void shutdown() {
       if (!_started) {
        return;
      }

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

      if (_needShutdown) {
        __log.debug("shutting down database.");
       _embeddedDB.shutdown();

      }

        _needShutdown = false;
        _datasource = null;
        _started = false;
    }

    public DataSource getDataSource() {
        DataSource ds =  __logSql.isDebugEnabled() ? LoggingInterceptor.createLoggingDS(_datasource, __logSql) : _datasource;
        return ds;
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
            __log.debug(__msgs.msgOdeUsingExternalDb(_odeConfig.getDbDataSource()));
        } catch (Exception ex) {
            String msg = __msgs.msgOdeInitExternalDbFailed(_odeConfig.getDbDataSource());
            __log.error(msg, ex);
            throw new DatabaseConfigException(msg, ex);
        }
    }

    private void initInternalDb() throws DatabaseConfigException {
        __log.debug(__msgs.msgOdeUsingInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass()));
        initInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass(),
                _odeConfig.getDbInternalUserName(), _odeConfig.getDbInternalPassword());

    }

    private void initInternalDb(String url, String driverClass, String username,String password) throws DatabaseConfigException {
        _connectionManager = new DatabaseConnectionManager(_txm,_odeConfig);
        _connectionManager.init(url, driverClass, username, password);
        _datasource = _connectionManager.getDataSource();
    }

    /**
     * Initialize embedded (DERBY) database.
     */
   private void initEmbeddedDb() throws DatabaseConfigException {

    switch (_odeConfig.getDbEmbeddedType()) {
      case DERBY:
        _embeddedDB = new DerbyDatabase();
        break;
      default:
        _embeddedDB = new H2Database();
    }
    _embeddedDB.init(_workRoot,_odeConfig,_txm);
    _datasource = _embeddedDB.getDataSource();
    _needShutdown = true;
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

    public BpelDAOConnectionFactory createDaoCF() throws DatabaseConfigException  {
        String pClassName = _odeConfig.getDAOConnectionFactory();

        __log.debug(__msgs.msgOdeUsingDAOImpl(pClassName));

        BpelDAOConnectionFactory cf;
        try {
            cf = (BpelDAOConnectionFactory) Class.forName(pClassName).newInstance();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(pClassName);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }

        cf.init(_odeConfig.getProperties(),_txm,getDataSource());
        return cf;
    }


    public ConfStoreDAOConnectionFactory createDaoStoreCF() throws DatabaseConfigException  {
        String pClassName = _odeConfig.getDAOConfStoreConnectionFactory();

        __log.debug(__msgs.msgOdeUsingDAOImpl(pClassName));

        ConfStoreDAOConnectionFactory cf;
        try {
            cf = (ConfStoreDAOConnectionFactory) Class.forName(pClassName).newInstance();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(pClassName);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }

        cf.init(_odeConfig.getProperties(),_txm,getDataSource());
        return cf;
    }

}
