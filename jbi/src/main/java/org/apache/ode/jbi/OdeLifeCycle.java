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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.fs.TempFileManager;
import org.opentools.minerva.MinervaPool;
import org.opentools.minerva.MinervaPool.PoolType;

/**
 * This class implements ComponentLifeCycle. The JBI framework will start this
 * engine class automatically when JBI framework starts up.
 */
public class OdeLifeCycle implements ComponentLifeCycle {
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


            if (_ode.getContext().getWorkspaceRoot() != null)
                TempFileManager.setWorkingDirectory(new File(_ode.getContext()
                        .getWorkspaceRoot()));

            __log.debug("Loading properties.");
            initProperties();

            __log.debug("Initializing message mappers.");
            initMappers();

            __log.debug("Creating data source.");
            initDataSource();

            __log.debug("Starting Dao.");
            initDao();

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
        _ode._scheduler.setJobProcessor(_ode._server);
        _ode._scheduler.setExecutorService(_ode._executorService, 20);
        _ode._scheduler.setTransactionManager((TransactionManager) _ode
                .getContext().getTransactionManager());
        _ode._scheduler.setDataSource(_ode._dataSource);
        _ode._scheduler.init();

        _ode._store = new ProcessStoreImpl(_ode._dataSource);
        _ode._store.loadAll();


        _ode._server.setInMemDaoConnectionFactory(new org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl());
        _ode._server.setDaoConnectionFactory(_ode._daocf);
        _ode._server.setEndpointReferenceContext(_ode._eprContext);
        _ode._server.setMessageExchangeContext(_ode._mexContext);
        _ode._server.setBindingContext(new BindingContextImpl(_ode));
        _ode._server.setScheduler(_ode._scheduler);

        _ode._server.init();

    }

    /**
     * Initialize the data store.
     *
     * @throws JBIException
     */
    private void initDao() throws JBIException {
    
        Properties properties = new Properties();
        File daoPropFile;
        String confDir = _ode.getContext().getInstallRoot();
        daoPropFile = new File((confDir != null) ? new File(confDir) : new File(""), "bpel-dao.properties");
    
        if (daoPropFile.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(daoPropFile);
                properties.load(new BufferedInputStream(fis));
            } catch (IOException e) {
                String errmsg = __msgs.msgOdeInitDAOErrorReadingProperties(daoPropFile);
                __log.error(errmsg, e);
                throw new JBIException(errmsg, e);
            }
        } else {
            __log.info(__msgs.msgOdeInitDAOPropertiesNotFound(daoPropFile));
        }
    
        BpelDAOConnectionFactoryJDBC cf = createDaoCF();
        cf.setDataSource(_ode._dataSource);
        cf.setTransactionManager(_ode.getTransactionManager());
        cf.init(properties);


        _ode._daocf = cf;
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

                __log.error("Failed to initialize JCA connector (check security manager configuration)");
                __log.debug("Failed to initialize JCA connector (check security manager configuration)",e);

            }
        }
    }

    private BpelDAOConnectionFactoryJDBC createDaoCF() throws JBIException {
        String pClassName = _ode._config.getDAOConnectionFactory();

        __log.info(__msgs.msgOdeUsingDAOImpl(pClassName));

        BpelDAOConnectionFactoryJDBC cf;
        try {
            cf = (BpelDAOConnectionFactoryJDBC) Class.forName(pClassName).newInstance();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(pClassName);
            __log.error(errmsg, ex);
            throw new JBIException(errmsg, ex);
        }

        return cf;
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
                    driver.connect(_derbyUrl+";shutdown=true", new Properties());
                } catch (SQLException ex) {
                    // Shutdown will always return an exeption!
                    if (ex.getErrorCode() != 45000)
                        __log.error("Error shutting down Derby: " + ex.getErrorCode(), ex);
                        
                } catch (Exception ex) {
                    __log.error("Error shutting down Derby.", ex);
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

  
}
