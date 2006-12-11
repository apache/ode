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

package org.apache.ode.jbi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.daohib.DataSourceConnectionProvider;
import org.apache.ode.daohib.HibernateTransactionManagerLookup;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.fs.TempFileManager;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;
import org.opentools.minerva.MinervaPool;
import org.opentools.minerva.MinervaPool.PoolType;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * This class implements ComponentLifeCycle. The JBI framework will start this
 * engine class automatically when JBI framework starts up.
 */
public class OdeLifeCycle implements ComponentLifeCycle {

    private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";
    private static final Messages __msgs = Messages.getMessages(Messages.class);
    private static final Log __log = LogFactory.getLog(OdeLifeCycle.class);

    private OdeSUManager _suManager = null;
    private boolean _initSuccess = false;
    private OdeContext _ode;
    private Receiver _receiver;
    private boolean _started;
    private boolean _needDerbyShutdown;
    private String _derbyUrl;
    private BpelServerConnector _connector;
    private String _dbType;

    ServiceUnitManager getSUManager() {
        return _suManager;
    }

    OdeContext getOdeContext() {
        return _ode;
    }

    public ObjectName getExtensionMBeanName() {
        return null;
    }

    public void init(ComponentContext context) throws JBIException {
        try {
            _ode = OdeContext.getInstance();
            _ode.setContext(context);
            _ode._consumer = new OdeConsumer(_ode);


            TempFileManager.setWorkingDirectory(new File(_ode.getContext()
                    .getWorkspaceRoot()));

            __log.debug("Loading properties.");
            initProperties();

            __log.debug("Initializing message mappers.");
            initMappers();

            __log.debug("Creating data source.");
            initDataSource();

            __log.debug("Starting Hibernate.");
            initHibernate();

            __log.info("Hibernate started.");

            __log.debug("Starting BPEL server.");
            initBpelServer();

            __log.debug("Starting JCA connector.");
            initConnector();

            _suManager = new OdeSUManager(_ode);
            _initSuccess = true;
            __log.info(__msgs.msgOdeInitialized());
        } finally {
            if (!_initSuccess) {
                // TODO ..then what? at least shutdown the scheduler
            }
        }
    }

    private void initMappers() throws JBIException  {
        Class mapperClass;
        try {
            mapperClass = Class.forName(_ode._config.getMessageMapper());
        } catch (ClassNotFoundException e) {
            String errmsg = __msgs.msgOdeInitMapperClassNotFound(_ode._config.getMessageMapper());
            __log.error(errmsg);
            throw new JBIException(errmsg, e);
        } catch (Throwable t) {
            String errmsg = __msgs.msgOdeInitMapperClassLoadFailed(_ode._config.getMessageMapper());
            __log.error(errmsg);
            throw new JBIException(errmsg, t);
        }
        try {
            _ode.registerMapper((Mapper) mapperClass.newInstance());
        } catch (Throwable t) {
            String errmsg = __msgs.msgOdeInitMapperInstantiationFailed(_ode._config.getMessageMapper());
            __log.error(errmsg);
            throw new JBIException(errmsg, t);
        }
    }

    private void initDataSource() throws JBIException {
        switch (_ode._config.getDbMode()) {
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

    private void initExternalDb() throws JBIException {
        try {
            _ode._dataSource = lookupInJndi(_ode._config.getDbDataSource());
            __log.info(__msgs.msgOdeUsingExternalDb(_ode._config.getDbDataSource()));
        } catch (Exception ex) {
            String msg = __msgs.msgOdeInitExternalDbFailed(_ode._config.getDbDataSource());
            __log.error(msg,ex);
            throw new JBIException(msg,ex);
        }
    }

    private void initInternalDb() throws JBIException {
        throw new JBIException("internalDb not supported!");
    }

    /**
     * Initialize embedded (DERBY) database.
     */
    private void initEmbeddedDb() throws JBIException {
        __log.info("Using DataSource Derby");

        String url =
                "jdbc:derby:" + _ode.getContext().getInstallRoot() + "/"
                        + _ode._config.getDbEmbeddedName();

        __log.debug("creating Minerva pool for " + url);

        MinervaPool minervaPool = new MinervaPool();
        minervaPool.setTransactionManager(_ode.getTransactionManager());
        minervaPool.getConnectionFactory().setConnectionURL(url);
        minervaPool.getConnectionFactory().setUserName("sa");
        minervaPool.getConnectionFactory().setDriver(
                org.apache.derby.jdbc.EmbeddedDriver.class.getName());

        minervaPool.getPoolParams().maxSize = _ode._config.getPoolMaxSize();
        minervaPool.getPoolParams().minSize = _ode._config.getPoolMinSize();
        minervaPool.getPoolParams().blocking = false;
        minervaPool.setType(PoolType.MANAGED);

        try {
            minervaPool.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(url);
            __log.error(errmsg,ex);
            throw new JBIException(errmsg,ex);
        }

        _ode._dataSource = minervaPool.createDataSource();
        _needDerbyShutdown = true;
        _derbyUrl = url;
    }

    /**
     * Load the "ode-jbi.properties" file from the install directory.
     *
     * @throws JBIException
     */
    private void initProperties() throws JBIException {
        OdeConfigProperties config = new OdeConfigProperties(_ode.getContext()
                .getInstallRoot());
        config.load();
        _ode._config = config;
    }

    private void initBpelServer() throws JBIException {
        if (__log.isDebugEnabled()) {
            __log.debug("ODE initializing");
        }

        _ode._server = new BpelServerImpl();
        // We don't want the server to automatically load deployed processes,
        // we'll do that explcitly
        _ode._eprContext = new EndpointReferenceContextImpl(_ode);
        _ode._mexContext = new MessageExchangeContextImpl(_ode);
        _ode._executorService = Executors.newCachedThreadPool();
        _ode._scheduler = new QuartzSchedulerImpl();
        _ode._scheduler.setBpelServer(_ode._server);
        _ode._scheduler.setExecutorService(_ode._executorService, 20);
        _ode._scheduler.setTransactionManager((TransactionManager) _ode
                .getContext().getTransactionManager());
        _ode._scheduler.setDataSource(_ode._dataSource);
        if ("sqlserver".equals(_dbType)) _ode._scheduler.setSqlServer(true);
        _ode._scheduler.init();

        _ode._store = new ProcessStoreImpl(_ode._dataSource);


        _ode._server.setInMemDaoConnectionFactory(new org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl());        
        _ode._server.setDaoConnectionFactory(_ode._daocf);
        _ode._server.setEndpointReferenceContext(_ode._eprContext);
        _ode._server.setMessageExchangeContext(_ode._mexContext);
        _ode._server.setBindingContext(new BindingContextImpl(_ode));
        _ode._server.setScheduler(_ode._scheduler);

        _ode._server.init();

    }

    /**
     * Initialize the Hibernate data store.
     *
     * @throws JBIException
     */
    private void initHibernate() throws JBIException {
        Properties properties = new Properties();
        properties.put(Environment.CONNECTION_PROVIDER,
                DataSourceConnectionProvider.class.getName());
        properties.put(Environment.TRANSACTION_MANAGER_STRATEGY,
                HibernateTransactionManagerLookup.class.getName());
        properties.put(Environment.SESSION_FACTORY_NAME, "jta");

        File hibernatePropFile = new File(_ode.getContext().getInstallRoot()
                + File.separatorChar + "hibernate.properties");

        if (hibernatePropFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(hibernatePropFile);
                properties.load(new BufferedInputStream(fis));
            } catch (IOException e) {
                String errmsg = __msgs
                        .msgOdeInitHibernateErrorReadingHibernateProperties(hibernatePropFile);
                __log.error(errmsg, e);
                throw new JBIException(errmsg, e);
            }
        } else {
            __log.info(__msgs
                    .msgOdeInitHibernatePropertiesNotFound(hibernatePropFile));
        }

        // Guess Hibernate dialect if not specified in hibernate.properties
        if (properties.get(Environment.DIALECT) == null) {
            try {
                properties.put(Environment.DIALECT, guessDialect(_ode._dataSource));
            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeInitHibernateDialectDetectFailed();
                if (__log.isDebugEnabled()) __log.error(errmsg,ex);
                else __log.error(errmsg);
            }
        }
        if (properties.get(Environment.DIALECT) != null) {
            String dialect = (String) properties.get(Environment.DIALECT);
            if (dialect.equals("org.hibernate.dialect.SQLServerDialect"))
                _dbType = "sqlserver";
            else _dbType = "other";
        }

        SessionManager sm = new SessionManager(properties, _ode._dataSource, _ode
                .getTransactionManager());
        _ode._daocf = new BpelDAOConnectionFactoryImpl(sm);
    }

    private void initConnector() throws JBIException {
        int port = _ode._config.getConnectorPort();
        if (port == 0) {
            __log.info("Skipping connector initialization.");
        } else {
            _connector = new BpelServerConnector();
            _connector.setBpelServer(_ode._server);
            _connector.setProcessStore(_ode._store);
            _connector.setPort(_ode._config.getConnectorPort());
            _connector.setId(_ode._config.getConnectorName());
            try {
                _connector.start();
            } catch (Exception e) {
                __log.error("Failed to initialize JCA connector.",e);
            }
        }
    }


    public synchronized void start() throws JBIException {
        if (_started)
            return;

        try {
            __log.info(__msgs.msgOdeStarting());

            if (!_initSuccess) {
                String errmsg = "attempt to call start() after init() failure.";
                IllegalStateException ex = new IllegalStateException(errmsg);
                __log.fatal(errmsg, ex);
                throw new JBIException(errmsg,ex);
            }

            if (_ode.getChannel() == null) {
                throw (new JBIException("No channel!", new NullPointerException()));
            }

            try {
                _ode._server.start();
            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeBpelServerStartFailure();
                __log.error(errmsg,ex);
                throw new JBIException(errmsg, ex);
            }

            _receiver = new Receiver(_ode);
            _receiver.start();
            _started = true;
            __log.info(__msgs.msgOdeStarted());
        } finally {
            if (!_started) {
                if (_receiver != null) {
                    _receiver.cease();
                }
                _receiver = null;
            }
        }
    }

    public synchronized void stop() throws JBIException {
        if (!_started) {
            return;
        }

        __log.info("Stopping ODE.");

        try {

            if (_receiver != null) {
                try {
                    _receiver.cease();
                } catch (Exception ex) {
                    __log.fatal("Error ceasing receiver.", ex);
                } finally {
                    _receiver = null;
                }
            }

            try {
                _ode._server.stop();

            } catch (Throwable ex) {
                __log.fatal("Error stopping services.", ex);
            }

            __log.info("ODE stopped.");
        } finally {
            _started = false;
        }
    }

    /**
     * Shutdown the service engine. This performs cleanup before the BPE is
     * terminated. Once this method has been called, init() must be called before
     * the transformation engine can be started again with a call to start().
     *
     * @throws javax.jbi.JBIException
     *           if the transformation engine is unable to shut down.
     */
    public void shutDown() throws JBIException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        if (_connector != null) {
            try {
                _connector.shutdown();
            } catch (Exception e) {
                __log.error("Error shutting down JCA server.",e);
            }
            _connector = null;
        }

        try {

            try {
                __log.debug("shutting down quartz scheduler.");
                _ode._scheduler.shutdown();
            } catch (Exception ex) {

            }

            __log.debug("cleaning up temporary files.");
            TempFileManager.cleanup();

            _suManager = null;
            _ode = null;

            if (_needDerbyShutdown) {
                __log.debug("shutting down derby.");
                EmbeddedDriver driver = new EmbeddedDriver();
                try {
                    driver.connect(_derbyUrl+";shutdown", new Properties());
                } catch (Exception ex) {
                    __log.error("Error shutting down derby.", ex);
                }
            }
            __log.info("Shutdown completed.");
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
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
                        ; // swallow
                        __log.error("Error closing JNDI connection.", ex1);
                    }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    private String guessDialect(DataSource dataSource) throws Exception {
        String dialect = null;
        // Open a connection and use that connection to figure out database
        // product name/version number in order to decide which Hibernate
        // dialect to use.
        Connection conn = dataSource.getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            if (metaData != null) {
                String dbProductName = metaData.getDatabaseProductName();
                int dbMajorVer = metaData.getDatabaseMajorVersion();
                __log.info("Using database " + dbProductName + " major version "
                        + dbMajorVer);
                DialectFactory.DatabaseDialectMapper mapper = (DialectFactory.DatabaseDialectMapper) HIBERNATE_DIALECTS
                        .get(dbProductName);
                if (mapper != null) {
                    dialect = mapper.getDialectClass(dbMajorVer);
                } else {
                    Dialect hbDialect = DialectFactory.determineDialect(
                            dbProductName, dbMajorVer);
                    if (hbDialect != null)
                        dialect = hbDialect.getClass().getName();
                }
            }
        } finally {
            conn.close();
        }

        if (dialect == null) {
            __log
                    .info("Cannot determine hibernate dialect for this database: using the default one.");
            dialect = DEFAULT_HIBERNATE_DIALECT;
        }

        assert dialect != null;

        return dialect;

    }

    private static final HashMap<String, DialectFactory.VersionInsensitiveMapper> HIBERNATE_DIALECTS = new HashMap<String, DialectFactory.VersionInsensitiveMapper>();

    static {
        // Hibernate has a nice table that resolves the dialect from the database
        // product name,
        // but doesn't include all the drivers. So this is supplementary, and some
        // day in the
        // future they'll add more drivers and we can get rid of this.
        // Drivers already recognized by Hibernate:
        // HSQL Database Engine
        // DB2/NT
        // MySQL
        // PostgreSQL
        // Microsoft SQL Server Database, Microsoft SQL Server
        // Sybase SQL Server
        // Informix Dynamic Server
        // Oracle 8 and Oracle >8
        HIBERNATE_DIALECTS.put("Apache Derby",
                new DialectFactory.VersionInsensitiveMapper(
                        "org.hibernate.dialect.DerbyDialect"));
    }

}
