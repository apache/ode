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

package org.apache.ode.axis2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.deploy.DeploymentPoller;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.hooks.ODEMessageReceiver;
import org.apache.ode.axis2.service.DeploymentWebService;
import org.apache.ode.axis2.service.ManagementService;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.LoggingDataSourceWrapper;
import org.apache.ode.utils.fs.TempFileManager;
import org.opentools.minerva.MinervaPool;

/**
 * Server class called by our Axis hooks to handle all ODE lifecycle management.
 * 
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ODEServer {

    private static final Log __log = LogFactory.getLog(ODEServer.class);
    private static final Log __logSql = LogFactory.getLog("org.apache.ode.sql");

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private File _appRoot;

    private File _workRoot;

    private BpelServerImpl _server;

    private ProcessStoreImpl _store;

    private ODEConfigProperties _odeConfig;

    private AxisConfiguration _axisConfig;

    private DataSource _datasource;

    private TransactionManager _txMgr;

    private BpelDAOConnectionFactory _daoCF;

    private ExecutorService _executorService;

    private QuartzSchedulerImpl _scheduler;

    private DeploymentPoller _poller;

    private MultiKeyMap _services = new MultiKeyMap();

    private MultiKeyMap _externalServices = new MultiKeyMap();

    private BpelServerConnector _connector;

    private MinervaPool _minervaPool;

    public void init(ServletConfig config, AxisConfiguration axisConf) throws ServletException {
        boolean success = false;
        try {
            _axisConfig = axisConf;
            _appRoot = new File(config.getServletContext().getRealPath("/WEB-INF"));
            TempFileManager.setWorkingDirectory(_appRoot);

            __log.debug("Loading properties");
            String confDir = System.getProperty("org.apache.ode.configDir");
            if (confDir == null)
                _odeConfig = new ODEConfigProperties(new File(_appRoot, "conf"));
            else
                _odeConfig = new ODEConfigProperties(new File(confDir));
            _odeConfig.load();

            String wdir = _odeConfig.getWorkingDir();
            if (wdir == null)
                _workRoot = _appRoot;
            else
                _workRoot = new File(wdir.trim());

            __log.debug("Initializing transaction manager");
            initTxMgr();

            __log.debug("Creating data source.");
            initDataSource();

            __log.debug("Starting DAO.");
            initDAO();

            __log.debug("DAO started.");

            __log.debug("Initializing BPEL process store.");
            initProcessStore();

            __log.debug("Initializing BPEL server.");
            initBpelServer();

            // Register BPEL event listeners configured in axis2.properties
            // file.
            registerEventListeners();

            try {
                _server.start();
            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeBpelServerStartFailure();
                __log.error(errmsg, ex);
                throw new ServletException(errmsg, ex);
            }

            _store.loadAll();

            __log.debug("Initializing JCA adapter.");
            initConnector();

            File deploymentDir = new File(_workRoot, "processes");
            _poller = new DeploymentPoller(deploymentDir, this);

            new ManagementService().enableService(_axisConfig, _server, _store, _appRoot.getAbsolutePath());
            new DeploymentWebService().enableService(_axisConfig, _server, _store, _poller, _appRoot.getAbsolutePath(),
                    _workRoot.getAbsolutePath());

            _poller.start();
            __log.info(__msgs.msgPollingStarted(deploymentDir.getAbsolutePath()));
            __log.info(__msgs.msgOdeStarted());
            success = true;
        } finally {
            if (!success)
                try {
                    // shutDown();
                } catch (Exception ex) {
                    // Problem rolling back start(). Not so important
                    __log.debug("Error rolling back incomplete shutdown.", ex);
                }
        }

    }

    /**
     * Shutdown the service engine. This performs cleanup before the BPE is
     * terminated. Once this method has been called, init() must be called
     * before the transformation engine can be started again with a call to
     * start().
     * 
     * @throws AxisFault
     *             if the engine is unable to shut down.
     */
    public void shutDown() throws AxisFault {

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            if (_poller != null)
                try {
                    __log.debug("shutting down poller");
                    _poller.stop();
                    _poller = null;
                } catch (Throwable t) {
                    __log.debug("Error stopping poller.", t);
                }

            if (_server != null)
                try {
                    __log.debug("shutting down ODE server.");
                    _server.shutdown();
                    _server = null;
                } catch (Throwable ex) {
                    __log.debug("Error stopping services.", ex);
                }

            if (_scheduler != null)
                try {
                    __log.debug("shutting down quartz scheduler.");
                    _scheduler.shutdown();
                    _scheduler = null;
                } catch (Exception ex) {
                    __log.debug("Scheduler couldn't be shutdown.", ex);
                }

            if (_store != null) 
                try {
                    _store.shutdown();
                    _store = null;
                } catch (Throwable t) {
                    __log.debug("Store could not be shutdown.",t);
                }
                
            if (_daoCF != null) 
                try {
                    _daoCF.shutdown();
                    _daoCF = null;
                } catch (Throwable ex) {
                    __log.debug("DOA shutdown failed.", ex);                    
                }
                
            if (_minervaPool != null)
                try {
                    __log.debug("shutting down minerva pool.");
                    _minervaPool.stop();
                    _minervaPool = null;
                } catch (Throwable t) {
                    __log.debug("Minerva pool could not be shut down.", t);
                }

            if (_txMgr != null) {
                __log.debug("shutting down transaction manager.");
                // TODO: we need to shutdown jotm if it is running.
                _txMgr = null;
            }

            try {
                __log.debug("cleaning up temporary files.");
                TempFileManager.cleanup();
            } catch (Throwable t) {
                __log.error("Unable to cleanup temp files.", t);
            }

            __log.info(__msgs.msgOdeShutdownCompleted());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public ODEService createService(Definition def, QName serviceName, String portName) throws AxisFault {
        if (_services.get(serviceName, portName) != null) {
            AxisService service = ((ODEService) _services.get(serviceName, portName)).getAxisService();
            _axisConfig.removeService(service.getName());
        }
        AxisService axisService = ODEAxisService.createService(_axisConfig, def, serviceName, portName);
        ODEService odeService = new ODEService(axisService, def, serviceName, portName, _server, _txMgr);
        if (_odeConfig.isReplicateEmptyNS()) {
            __log.debug("Setting service with empty namespace replication");
            odeService.setReplicateEmptyNS(true);
        }

        _services.put(serviceName, portName, odeService);

        // Setting our new service on the receiver, the same receiver handles
        // all
        // operations so the first one should fit them all
        AxisOperation firstOp = (AxisOperation) axisService.getOperations().next();
        ((ODEMessageReceiver) firstOp.getMessageReceiver()).setService(odeService);
        ((ODEMessageReceiver) firstOp.getMessageReceiver()).setExecutorService(_executorService);

        // We're public!
        _axisConfig.addService(axisService);
        __log.debug("Created Axis2 service " + serviceName);
        return odeService;
    }

    public ExternalService createExternalService(Definition def, QName serviceName, String portName) throws ContextException  {
        ExternalService extService = (ExternalService) _externalServices.get(serviceName);
        if (extService != null)
            return extService;

        try {
            extService = new ExternalService(def, serviceName, portName, _executorService, _axisConfig, _scheduler);
        } catch (Exception ex) {
            __log.error("Could not create external service.", ex);
            throw new ContextException("Error creating external service.", ex);
        }
        
        if (_odeConfig.isReplicateEmptyNS()) {
            __log.debug("Setting external service with empty namespace replication");
            extService.setReplicateEmptyNS(true);
        }
        _externalServices.put(serviceName, portName, extService);
        __log.debug("Created external service " + serviceName);
        return extService;
    }

    public void destroyService(QName serviceName) {
        __log.debug("Destroying service " + serviceName);
        try {
            _axisConfig.removeService(serviceName.getLocalPart());
        } catch (AxisFault axisFault) {
            __log.error("Couldn't destroy service " + serviceName);
        }
        _services.remove(serviceName);
    }

    public ODEService getService(QName serviceName, String portName) {
        return (ODEService) _services.get(serviceName, portName);
    }

    public ODEService getService(QName serviceName, QName portTypeName) {
        // TODO Normally this lookup should't exist as there could be more one
        // than port
        // TODO for a portType. See MessageExchangeContextImpl.
        for (Object o : _services.values()) {
            ODEService service = (ODEService) o;
            if (service.respondsTo(serviceName, portTypeName))
                return service;
        }
        return null;
    }

    public ExternalService getExternalService(QName serviceName, String portName) {
        return (ExternalService) _externalServices.get(serviceName, portName);
    }

    private void initTxMgr() throws ServletException {
        String txFactoryName = _odeConfig.getTxFactoryClass();
        __log.debug("Initializing transaction manager using " + txFactoryName);
        try {
            Class txFactClass = this.getClass().getClassLoader().loadClass(txFactoryName);
            Object txFact = txFactClass.newInstance();
            _txMgr = (TransactionManager) txFactClass.getMethod("getTransactionManager", (Class[]) null).invoke(txFact);
        } catch (Exception e) {
            throw new ServletException("Couldn't initialize a transaction manager!", e);
        }
    }

    private void initDataSource() throws ServletException {
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

    private void initConnector() throws ServletException {
        int port = _odeConfig.getConnectorPort();
        if (port == 0) {
            __log.info("Skipping connector initialization.");
        } else {
            _connector = new BpelServerConnector();
            _connector.setBpelServer(_server);
            _connector.setProcessStore(_store);
            _connector.setPort(_odeConfig.getConnectorPort());
            _connector.setId("jcaServer");
            try {
                _connector.start();
            } catch (Exception e) {
                __log.error("Failed to initialize JCA connector.");
            }
        }
    }

    private void initExternalDb() throws ServletException {
        try {
            if (__logSql.isDebugEnabled())
                _datasource = new LoggingDataSourceWrapper((DataSource) lookupInJndi(_odeConfig.getDbDataSource()), __logSql);
            else
                _datasource = (DataSource) lookupInJndi(_odeConfig.getDbDataSource());
            __log.info(__msgs.msgOdeUsingExternalDb(_odeConfig.getDbDataSource()));
        } catch (Exception ex) {
            String msg = __msgs.msgOdeInitExternalDbFailed(_odeConfig.getDbDataSource());
            __log.error(msg, ex);
            throw new ServletException(msg, ex);
        }
    }

    private void initInternalDb() throws ServletException {
        throw new ServletException("internalDb not supported!");
    }

    /**
     * Initialize embedded (DERBY) database.
     */
    private void initEmbeddedDb() throws ServletException {
        __log.info("Using DataSource Derby");

        String db = "jpadb";
        String persistenceType = System.getProperty("ode.persistence");
        if (persistenceType != null) {
            if ("hibernate".equalsIgnoreCase(persistenceType))
                db = "hibdb";
        }

        String url = "jdbc:derby:" + _workRoot + "/" + db + "/" + _odeConfig.getDbEmbeddedName();

        __log.debug("creating Minerva pool for " + url);

        _minervaPool = new MinervaPool();
        _minervaPool.setTransactionManager(_txMgr);
        _minervaPool.getConnectionFactory().setConnectionURL(url);
        _minervaPool.getConnectionFactory().setUserName("sa");
        _minervaPool.getConnectionFactory().setDriver(org.apache.derby.jdbc.EmbeddedDriver.class.getName());

        _minervaPool.getPoolParams().maxSize = _odeConfig.getPoolMaxSize();
        _minervaPool.getPoolParams().minSize = _odeConfig.getPoolMinSize();
        _minervaPool.getPoolParams().blocking = false;
        _minervaPool.setType(MinervaPool.PoolType.MANAGED);

        try {
            _minervaPool.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(url);
            __log.error(errmsg, ex);
            throw new ServletException(errmsg, ex);
        }

        if (__logSql.isDebugEnabled())
            _datasource = new LoggingDataSourceWrapper(_minervaPool.createDataSource(), __logSql);
        else _datasource = _minervaPool.createDataSource();
    }

    /**
     * Initialize the DAO.
     * 
     * @throws ServletException
     */
    protected void initDAO() throws ServletException {
        Properties properties = new Properties();
        File daoPropFile;
        String confDir = System.getProperty("org.apache.ode.configDir");
        daoPropFile = new File((confDir != null) ? new File(confDir) : _appRoot, "bpel-dao.properties");

        if (daoPropFile.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(daoPropFile);
                properties.load(new BufferedInputStream(fis));
            } catch (IOException e) {
                String errmsg = __msgs.msgOdeInitDAOErrorReadingProperties(daoPropFile);
                __log.error(errmsg, e);
                throw new ServletException(errmsg, e);
            }
        } else {
            __log.info(__msgs.msgOdeInitHibernatePropertiesNotFound(daoPropFile));
        }

        BpelDAOConnectionFactoryJDBC cf = createDaoCF();
        cf.setDataSource(_datasource);
        cf.setTransactionManager(_txMgr);
        cf.init(properties);
        _daoCF = cf;
    }

    protected void initProcessStore() {
        _store = createProcessStore(_datasource);
        _store.registerListener(new ProcessStoreListenerImpl());
        _store.setDeployDir(new File(_workRoot, "processes"));
    }
    
    protected ProcessStoreImpl createProcessStore(DataSource ds) {
        return new ProcessStoreImpl(ds);
    }

    private void initBpelServer() {
        if (__log.isDebugEnabled()) {
            __log.debug("ODE initializing");
        }
        _server = new BpelServerImpl();

        _executorService = Executors.newCachedThreadPool();
        _scheduler = new QuartzSchedulerImpl();
        _scheduler.setJobProcessor(_server);
        _scheduler.setExecutorService(_executorService, 20);
        _scheduler.setTransactionManager(_txMgr);
        _scheduler.setDataSource(_datasource);
        _scheduler.init();

        _server.setDaoConnectionFactory(_daoCF);
        _server.setInMemDaoConnectionFactory(new org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl());
        _server.setEndpointReferenceContext(new EndpointReferenceContextImpl(this));
        _server.setMessageExchangeContext(new P2PMexContextImpl(this, new MessageExchangeContextImpl(this),
                _scheduler));
        _server.setBindingContext(new BindingContextImpl(this, _store));
        _server.setScheduler(_scheduler);
        if (_odeConfig.isDehydrationEnabled()){
            CountLRUDehydrationPolicy dehy = new CountLRUDehydrationPolicy();
    //        dehy.setProcessMaxAge(10000);
            _server.setDehydrationPolicy(dehy);
        }
        _server.init();
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

    public ProcessStore getProcessStore() {
        return _store;
    }

    public BpelServerImpl getBpelServer() {
        return _server;
    }

    private void registerEventListeners() {
        String listenersStr = _odeConfig.getEventListeners();
        if (listenersStr != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
                String listenerCN = tokenizer.nextToken();
                try {
                    _server.registerBpelEventListener((BpelEventListener) Class.forName(listenerCN).newInstance());
                    __log.info(__msgs.msgBpelEventListenerRegistered(listenerCN));
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + listenerCN + ", the class couldn't be "
                            + "loaded properly.");
                }
            }

        }
    }

    private class ProcessStoreListenerImpl implements ProcessStoreListener {

        public void onProcessStoreEvent(ProcessStoreEvent event) {
            handleEvent(event);
        }

    }

    private void handleEvent(ProcessStoreEvent pse) {
        __log.debug("Process store event: " + pse);
        switch (pse.type) {
        case ACTVIATED:
        case RETIRED:
            // bounce the process
            _server.unregister(pse.pid);
            _server.register(_store.getProcessConfiguration(pse.pid));
            break;
        case DISABLED:
        case UNDEPLOYED:
            _server.unregister(pse.pid);
            break;
        default:
            __log.debug("Ignoring store event: " + pse);
        }
    }

    private BpelDAOConnectionFactoryJDBC createDaoCF() throws ServletException {
        String pClassName = "org.apache.ode.dao.jpa.ojpa.BPELDAOConnectionFactoryImpl";
        String persistenceType = System.getProperty("ode.persistence");
        if (persistenceType != null) {
            if ("hibernate".equalsIgnoreCase(persistenceType))
                pClassName = "org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl";
            else if ("jpa".equalsIgnoreCase(persistenceType))
                pClassName = "org.apache.ode.dao.jpa.ojpa.BPELDAOConnectionFactoryImpl";
            else
                pClassName = persistenceType;
        }

        __log.info(__msgs.msgOdeUsingDAOImpl(pClassName));

        BpelDAOConnectionFactoryJDBC cf;
        try {
            cf = (BpelDAOConnectionFactoryJDBC) Class.forName(pClassName).newInstance();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(pClassName);
            __log.error(errmsg, ex);
            throw new ServletException(errmsg, ex);
        }

        return cf;
    }

}
