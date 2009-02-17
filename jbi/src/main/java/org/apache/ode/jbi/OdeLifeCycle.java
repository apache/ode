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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.extension.ExtensionValidator;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.bpel.extension.ExtensionBundleValidation;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.ProcessAndInstanceManagementMBean;
import org.apache.ode.bpel.evtproc.DebugBpelEventListener;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.rtrep.common.extension.AbstractExtensionBundle;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.il.dbutil.DatabaseConfigException;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.scheduler.simple.JdbcDelegate;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.fs.TempFileManager;

/**
 * This class implements ComponentLifeCycle. The JBI framework will start this engine class automatically when JBI framework starts
 * up.
 */
public class OdeLifeCycle implements ComponentLifeCycle {
    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private static final Log __log = LogFactory.getLog(OdeLifeCycle.class);

    private OdeSUManager _suManager = null;

    private boolean _initSuccess = false;

    private OdeContext _ode;

    private Receiver _receiver;

    private boolean _started;

    private BpelServerConnector _connector;

    private Database _db;
    
    private ObjectName _mbeanName;

    ServiceUnitManager getSUManager() {
        return _suManager;
    }

    OdeContext getOdeContext() {
        return _ode;
    }

    public ObjectName getExtensionMBeanName() {
        return _mbeanName;
    }

    public void init(ComponentContext context) throws JBIException {
        try {
            _ode = OdeContext.getInstance();
            _ode.setContext(context);
            
            _ode._consumer = new OdeConsumer(_ode);
            
            if (_ode.getContext().getWorkspaceRoot() != null)
                TempFileManager.setWorkingDirectory(new File(_ode.getContext().getWorkspaceRoot()));

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

            // Register BPEL event listeners configured in ode-jbi.properties.
            registerEventListeners();

            registerMexInterceptors();
            
            registerExtensionActivityBundles();
            
            registerMBean();

            __log.debug("Starting JCA connector.");
            initConnector();

            __log.debug("Register ProcessManagement APIs");
            _ode.activatePMAPIs();


            _suManager = new OdeSUManager(_ode);
            _initSuccess = true;
            __log.info(__msgs.msgOdeInitialized());
        } finally {
            if (!_initSuccess) {
                // TODO ..then what? at least shutdown the scheduler
            }
        }
    }

    private void initMappers() throws JBIException {
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
        _db = new Database(_ode._config);
        _db.setTransactionManager(_ode.getTransactionManager());
        _db.setWorkRoot(new File(_ode.getContext().getInstallRoot()));

        try {
            _db.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbConfigError();
            __log.error(errmsg, ex);
            throw new JBIException(errmsg, ex);
        }

        _ode._dataSource = _db.getDataSource();
    }

    /**
     * Load the "ode-jbi.properties" file from the install directory.
     *
     * @throws JBIException
     */
    private void initProperties() throws JBIException {
        OdeConfigProperties config = new OdeConfigProperties(new File(_ode.getContext().getInstallRoot(),
                OdeConfigProperties.CONFIG_FILE_NAME));

        try {
            config.load();
        } catch (FileNotFoundException fnf) {
            __log.warn(__msgs.msgOdeInstallErrorCfgNotFound(config.getFile()));
        } catch (Exception ex) {
           String errmsg = __msgs.msgOdeInstallErrorCfgReadError(config.getFile());
           throw new JBIException(errmsg,ex);
        }
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
        if (_ode._config.getThreadPoolMaxSize() == 0)
            _ode._executorService = Executors.newCachedThreadPool();
        else
            _ode._executorService = Executors.newFixedThreadPool(_ode._config.getThreadPoolMaxSize());
        SimpleScheduler sched =new SimpleScheduler(new GUID().toString(), new JdbcDelegate(_ode._dataSource),  _ode._config.getProperties());
        sched.setJobProcessor(_ode._server);
        sched.setTransactionManager((TransactionManager) _ode.getContext().getTransactionManager());
        _ode._scheduler = sched;

        _ode._store = new ProcessStoreImpl(_ode._eprContext, _ode._dataSource,
                _ode._config.getDAOConnectionFactory(), _ode._config, false);
        _ode._store.loadAll();

        _ode._server.setDaoConnectionFactory(_ode._daocf);
        _ode._server.setEndpointReferenceContext(_ode._eprContext);
        _ode._server.setMessageExchangeContext(_ode._mexContext);
        _ode._server.setBindingContext(new BindingContextImpl(_ode));
        _ode._server.setScheduler(_ode._scheduler);
        _ode._server.setTransactionManager(_ode.getTransactionManager());
        _ode._server.setConfigProperties(_ode._config);
        _ode._server.registerBpelEventListener(new DebugBpelEventListener());

        _ode._server.init();

    }

    /**
     * Initialize the data store.
     *
     * @throws JBIException
     */
    private void initDao() throws JBIException {
        BpelDAOConnectionFactoryJDBC cf;
        try {
            cf = _db.createDaoCF();
        } catch (DatabaseConfigException e) {
            String errmsg = __msgs.msgDAOInstantiationFailed(_ode._config.getDAOConnectionFactory());
            throw new JBIException(errmsg,e);
        }
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
                __log.debug("Failed to initialize JCA connector (check security manager configuration)", e);

            }
        }
    }

    private void registerMBean() throws JBIException {
        ProcessAndInstanceManagementMBean pmapi = new ProcessAndInstanceManagementMBean(_ode._server,_ode._store);
        MBeanServer server = _ode.getContext().getMBeanServer();
        try {
            if (server != null) {
                _mbeanName = _ode.getContext().getMBeanNames().createCustomComponentMBeanName("Management");
                if (server.isRegistered(_mbeanName)) {
                    server.unregisterMBean(_mbeanName);
                }
                server.registerMBean(pmapi, _mbeanName);
            }
        } catch (Exception e) {
            throw new JBIException(e);
        }
    }
    
    private void unregisterMBean() throws JBIException {
        try {
            if (_mbeanName != null) {
                MBeanServer server = _ode.getContext().getMBeanServer();
                assert server != null;
                if (server.isRegistered(_mbeanName)) {
                    server.unregisterMBean(_mbeanName);
                }
            }
        } catch (Exception e) {
            throw new JBIException(e);
        }
    }
    
    private void registerEventListeners() {
        String listenersStr = _ode._config.getEventListeners();
        if (listenersStr != null) {
            for (String listenerCN : listenersStr.split("\\s*(,|;)\\s*")) {
                try {
                    _ode._server.registerBpelEventListener((BpelEventListener) Class.forName(listenerCN).newInstance());
                    __log.info(__msgs.msgBpelEventListenerRegistered(listenerCN));
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + listenerCN + ", the class couldn't be "
                            + "loaded properly.");
                }
            }
        }
    }

    private void registerMexInterceptors() {
        String listenersStr = _ode._config.getMessageExchangeInterceptors();
        if (listenersStr != null) {
            for (String interceptorCN : listenersStr.split("\\s*(,|;)\\s*")) {
                try {
                    _ode._server.registerMessageExchangeInterceptor((MessageExchangeInterceptor) Class.forName(interceptorCN).newInstance());
                    __log.info(__msgs.msgMessageExchangeInterceptorRegistered(interceptorCN));
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + interceptorCN + ", the class couldn't be "
                            + "loaded properly: " + e);
                }
            }
        }
    }

    private void registerExtensionActivityBundles() {
        String extensionsRTStr = _ode._config.getExtensionActivityBundlesRT();
        String extensionsValStr = _ode._config.getExtensionActivityBundlesValidation();
        if (extensionsRTStr != null) {
            // TODO replace StringTokenizer by regex
            for (StringTokenizer tokenizer = new StringTokenizer(extensionsRTStr, ",;"); tokenizer.hasMoreTokens();) {
                String bundleCN = tokenizer.nextToken();
                try {
                    // instantiate bundle
                    ExtensionBundleRuntime bundleRT = (ExtensionBundleRuntime) Class.forName(bundleCN).newInstance();
                    // register extension bundle (BPEL server)
                    _ode._server.registerExtensionBundle(bundleRT);
                } catch (Exception e) {
                    __log.warn("Couldn't register the extension bundle runtime " + bundleCN + ", the class couldn't be " +
                            "loaded properly.");
                }
            }
        }
        if (extensionsValStr != null) {
            Map<QName, ExtensionValidator> validators = new HashMap<QName, ExtensionValidator>();
            for (StringTokenizer tokenizer = new StringTokenizer(extensionsValStr, ",;"); tokenizer.hasMoreTokens();) {
                String bundleCN = tokenizer.nextToken();
                try {
                    // instantiate bundle
                    ExtensionBundleValidation bundleVal = (ExtensionBundleValidation) Class.forName(bundleCN).newInstance();
                    //add validators
                    validators.putAll(bundleVal.getExtensionValidators());
                } catch (Exception e) {
                    __log.warn("Couldn't register the extension bundle validator " + bundleCN + ", the class couldn't be " +
                            "loaded properly.");
                }
            }
            // register extension bundle (BPEL store)
            _ode._store.setExtensionValidators(validators);
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
                throw new JBIException(errmsg, ex);
            }

            if (_ode.getChannel() == null) {
                throw (new JBIException("No channel!", new NullPointerException()));
            }

            try {
                _ode._server.start();
            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeBpelServerStartFailure();
                __log.error(errmsg, ex);
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
     * Shutdown the service engine. This performs cleanup before the BPE is terminated. Once this method has been called, init()
     * must be called before the transformation engine can be started again with a call to start().
     *
     * @throws javax.jbi.JBIException
     *             if the transformation engine is unable to shut down.
     */
    public void shutDown() throws JBIException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        
        unregisterMBean();

        _ode.deactivatePMAPIs();

        if (_connector != null) {
            try {
                _connector.shutdown();
            } catch (Exception e) {
                __log.error("Error shutting down JCA server.", e);
            }
            _connector = null;
        }

        try {

            try {
                __log.debug("shutting down quartz scheduler.");
                _ode._scheduler.shutdown();
            } catch (Exception ex) {

            }

            try {
                _db.shutdown();
            } catch (Exception ex) {
                __log.debug("error shutting down db.", ex);
            } finally {
                _db = null;
            }

            __log.debug("cleaning up temporary files.");
            TempFileManager.cleanup();

            _suManager = null;
            _ode = null;

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

