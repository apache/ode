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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

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
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.scheduler.simple.JdbcDelegate;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.fs.TempFileManager;

/**
 * Server class called by our Axis hooks to handle all ODE lifecycle management.
 * 
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ODEServer {

    protected final Log __log = LogFactory.getLog(getClass());

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    protected File _appRoot;

    protected File _workRoot;

    protected BpelServerImpl _server;

    protected ProcessStoreImpl _store;

    protected ODEConfigProperties _odeConfig;

    protected AxisConfiguration _axisConfig;


    protected TransactionManager _txMgr;

    protected BpelDAOConnectionFactory _daoCF;

    protected Scheduler _scheduler;

    protected Database _db;

    private DeploymentPoller _poller;

    private MultiKeyMap _services = new MultiKeyMap();

    private MultiKeyMap _externalServices = new MultiKeyMap();

    private BpelServerConnector _connector;


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

            try {
                _odeConfig.load();
            } catch (FileNotFoundException fnf) {
                String errmsg = __msgs.msgOdeInstallErrorCfgNotFound(_odeConfig.getFile());
                __log.warn(errmsg);

            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeInstallErrorCfgReadError(_odeConfig.getFile());
                __log.error(errmsg, ex);
                throw new ServletException(errmsg, ex);
            }

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

            File deploymentDir = new File(_workRoot, "processes");
            _poller = new DeploymentPoller(deploymentDir, this);

            new ManagementService().enableService(_axisConfig, _server, _store, _appRoot.getAbsolutePath());
            new DeploymentWebService().enableService(_axisConfig, _server, _store, _poller, _appRoot.getAbsolutePath(), _workRoot
                    .getAbsolutePath());

            _store.loadAll();

            __log.debug("Initializing JCA adapter.");
            initConnector();

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

    private void initDataSource() throws ServletException {
        _db = new Database(_odeConfig);
        _db.setTransactionManager(_txMgr);
        _db.setWorkRoot(_workRoot);

        try {
            _db.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbConfigError();
            __log.error(errmsg, ex);
            throw new ServletException(errmsg, ex);
        }

    }

    /**
     * Shutdown the service engine. This performs cleanup before the BPE is terminated. Once this method has been called, init()
     * must be called before the transformation engine can be started again with a call to start().
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
                    __log.debug("Store could not be shutdown.", t);
                }

            if (_daoCF != null)
                try {
                    _daoCF.shutdown();
                } catch (Throwable ex) {
                    __log.debug("DOA shutdown failed.", ex);
                } finally {
                    _daoCF = null;
                }

            if (_db != null)
                try {
                    _db.shutdown();
                    
                } catch (Throwable ex) {
                    __log.debug("DB shutdown failed.", ex);
                } finally {
                    _db = null;
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

    public ODEService createService(ProcessConf pconf, QName serviceName, String portName) throws AxisFault {
        destroyService(serviceName, portName);
        AxisService axisService = ODEAxisService.createService(_axisConfig, pconf, serviceName, portName);
        ODEService odeService = new ODEService(axisService, pconf.getDefinitionForService(serviceName), serviceName, portName, _server);
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

        // We're public!
        _axisConfig.addService(axisService);
        __log.debug("Created Axis2 service " + serviceName);
        return odeService;
    }

    public ExternalService createExternalService(Definition def, QName serviceName, String portName) throws ContextException {
        ExternalService extService = (ExternalService) _externalServices.get(serviceName);
        if (extService != null)
            return extService;

        try {
            extService = new ExternalService(def, serviceName, portName, _axisConfig, _scheduler, _server);
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

    public void destroyService(QName serviceName, String portName) {
        __log.debug("Destroying service " + serviceName + " port " + portName);
        ODEService service = (ODEService) _services.remove(serviceName, portName);
        if (service != null) {
            try {
                _axisConfig.removeService(service.getAxisService().getName());
            } catch (AxisFault axisFault) {
                __log.error("Couldn't destroy service " + serviceName);
            }
        } else {
            __log.debug("Couldn't find service " + serviceName + " port " + portName + " to destroy.");
        }
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
            __log.fatal("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
            throw new ServletException("Couldn't initialize a transaction manager with factory: " + txFactoryName, e);
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

    /**
     * Initialize the DAO.
     * 
     * @throws ServletException
     */
    protected void initDAO() throws ServletException {
        __log.info(__msgs.msgOdeUsingDAOImpl(_odeConfig.getDAOConnectionFactory()));
        try {
            _daoCF = _db.createDaoCF();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(_odeConfig.getDAOConnectionFactory());
            __log.error(errmsg, ex);
            throw new ServletException(errmsg, ex);
            
        }
    }

    protected void initProcessStore() {
        _store = createProcessStore(_db.getDataSource());
        _store.registerListener(new ProcessStoreListenerImpl());
        _store.setDeployDir(new File(_workRoot, "processes"));
    }

    protected ProcessStoreImpl createProcessStore(DataSource ds) {
        return new ProcessStoreImpl(ds, _odeConfig.getDAOConnectionFactory(),false);
    }

    protected Scheduler createScheduler() {
        SimpleScheduler scheduler = new SimpleScheduler(new GUID().toString(),new JdbcDelegate(_db.getDataSource()));
        scheduler.setTransactionManager(_txMgr);

        return scheduler;
    }

    private void initBpelServer() {
        if (__log.isDebugEnabled()) {
            __log.debug("ODE initializing");
        }

        _server = new BpelServerImpl();
        _scheduler = createScheduler();
        _scheduler.setJobProcessor(_server);

        _server.setDaoConnectionFactory(_daoCF);
        _server.setEndpointReferenceContext(new EndpointReferenceContextImpl(this));
        _server.setMessageExchangeContext(new MessageExchangeContextImpl(this));
        _server.setBindingContext(new BindingContextImpl(this, _store));
        _server.setScheduler(_scheduler);
        if (_odeConfig.isDehydrationEnabled()) {
            CountLRUDehydrationPolicy dehy = new CountLRUDehydrationPolicy();
            // dehy.setProcessMaxAge(10000);
            _server.setDehydrationPolicy(dehy);
        }
        _server.setConfigProperties(_odeConfig.getProperties());
        _server.init();
    }

    public ProcessStoreImpl getProcessStore() {
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
            ProcessConf pconf = _store.getProcessConfiguration(pse.pid);
            if (pconf != null)
                _server.register(pconf);
            else {
                __log.debug("slighly odd: recevied event " + pse + " for process not in store!");
            }
            break;
        case DISABLED:
        case UNDEPLOYED:
            _server.unregister(pse.pid);
            break;
        default:
            __log.debug("Ignoring store event: " + pse);
        }
    }
}
