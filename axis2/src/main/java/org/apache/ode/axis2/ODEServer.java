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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.transaction.*;
import javax.transaction.xa.XAResource;
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
import org.apache.ode.axis2.httpbinding.HttpExternalService;
import org.apache.ode.axis2.soapbinding.SoapExternalService;
import org.apache.ode.bpel.extension.ExtensionValidator;
import org.apache.ode.bpel.extension.ExtensionBundleRuntime;
import org.apache.ode.bpel.extension.ExtensionBundleValidation;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
import org.apache.ode.bpel.evtproc.DebugBpelEventListener;
import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.scheduler.simple.JdbcDelegate;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.apache.ode.utils.fs.TempFileManager;
import org.apache.ode.bpel.pmapi.InstanceManagement;
import org.apache.ode.bpel.pmapi.ProcessManagement;

/**
 * Server class called by our Axis hooks to handle all ODE lifecycle management.
 *
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ODEServer {

    protected final Log __log = LogFactory.getLog(getClass());
    protected final Log __logTx = LogFactory.getLog("org.apache.ode.tx");

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
    private ManagementService _mgtService;
    
    public void init(ServletConfig config, AxisConfiguration axisConf) throws ServletException {
        init(config.getServletContext().getRealPath("/WEB-INF"), axisConf);
    }

    public void init(String contextPath, AxisConfiguration axisConf) throws ServletException {
        boolean success = false;
        try {
            _axisConfig = axisConf;
            String rootDir = System.getProperty("org.apache.ode.rootDir");
            if (rootDir != null) _appRoot = new File(rootDir);
            else _appRoot = new File(contextPath);
            TempFileManager.setWorkingDirectory(_appRoot);

            __log.debug("Loading properties");
            String confDir = System.getProperty("org.apache.ode.configDir");
            if (confDir == null) _odeConfig = new ODEConfigProperties(new File(_appRoot, "conf"));
            else _odeConfig = new ODEConfigProperties(new File(confDir));

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
            if (wdir == null) _workRoot = _appRoot;
            else _workRoot = new File(wdir.trim());

            __log.debug("Initializing transaction manager");
            initTxMgr();
            __log.debug("Creating data source.");
            initDataSource();
            __log.debug("Starting DAO.");
            initDAO();
            EndpointReferenceContextImpl eprContext = new EndpointReferenceContextImpl(this);            
            __log.debug("Initializing BPEL process store.");
            initProcessStore(eprContext);
            __log.debug("Initializing BPEL server.");
            initBpelServer(eprContext);

            // Register BPEL event listeners configured in axis2.properties file.
            registerEventListeners();
            registerMexInterceptors();

            registerExtensionActivityBundles();

            registerExternalVariableModules();

            try {
                _server.start();
            } catch (Exception ex) {
                String errmsg = __msgs.msgOdeBpelServerStartFailure();
                __log.error(errmsg, ex);
                throw new ServletException(errmsg, ex);
            }

            File deploymentDir = new File(_workRoot, "processes");
            _poller = new DeploymentPoller(deploymentDir, this);

            _mgtService = new ManagementService();
            _mgtService.enableService(_axisConfig, _server, _store, _appRoot.getAbsolutePath());

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
     * @throws AxisFault if the engine is unable to shut down.
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
                _txMgr = null;
            }

            if (_connector != null) {
                try {
                    __log.debug("shutdown BpelConnector");
                    _connector.shutdown();
                } catch (Throwable t) {
                    __log.error("Unable to cleanup temp files.", t);
                }
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
        ODEService odeService = new ODEService(axisService, pconf, serviceName, portName, _server);

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

    public ExternalService createExternalService(ProcessConf pconf, QName serviceName, String portName) throws ContextException {
        ExternalService extService = (ExternalService) _externalServices.get(serviceName);
        if (extService != null)
            return extService;

	Definition def = pconf.getDefinitionForService(serviceName);
        try {
             if (WsdlUtils.useHTTPBinding(def, serviceName, portName)) {
                 if(__log.isDebugEnabled())__log.debug("Creating HTTP-bound external service " + serviceName);
                 extService = new HttpExternalService(pconf, serviceName, portName, _server);
             } else if (WsdlUtils.useSOAPBinding(def, serviceName, portName)) {
                 if(__log.isDebugEnabled())__log.debug("Creating SOAP-bound external service " + serviceName);
                 extService = new SoapExternalService(def, serviceName, portName, _axisConfig, pconf);
             }
        } catch (Exception ex) {
            __log.error("Could not create external service.", ex);
            throw new ContextException("Error creating external service! name:"+serviceName+", port:"+portName, ex);
        }

         // if not SOAP nor HTTP binding
         if (extService == null) throw new ContextException("Only SOAP and HTTP binding supported!");

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
            if (__logTx.isDebugEnabled() && System.getProperty("ode.debug.tx") != null)
                _txMgr = new DebugTxMgr(_txMgr);
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
                __log.error("Failed to initialize JCA connector.", e);
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

    protected void initProcessStore(EndpointReferenceContext eprContext) {
        _store = createProcessStore(eprContext, _db.getDataSource());
        _store.registerListener(new ProcessStoreListenerImpl());
        _store.setDeployDir(new File(_workRoot, "processes"));
    }

    protected ProcessStoreImpl createProcessStore(EndpointReferenceContext eprContext, DataSource ds) {
        return new ProcessStoreImpl(eprContext, ds, _odeConfig.getDAOConnectionFactory(), _odeConfig, false);
    }

    protected Scheduler createScheduler() {
        SimpleScheduler scheduler = new SimpleScheduler(new GUID().toString(),new JdbcDelegate(_db.getDataSource()));
        scheduler.setTransactionManager(_txMgr);

        return scheduler;
    }

    protected void initBpelServer(EndpointReferenceContextImpl eprContext) {
        if (__log.isDebugEnabled()) {
            __log.debug("ODE initializing");
        }

        _server = new BpelServerImpl();
        _scheduler = createScheduler();
        _scheduler.setJobProcessor(_server);

        _server.setDaoConnectionFactory(_daoCF);
        _server.setEndpointReferenceContext(eprContext);
        _server.setMessageExchangeContext(new MessageExchangeContextImpl(this));
        _server.setBindingContext(new BindingContextImpl(this, _store));
        _server.setScheduler(_scheduler);
        _server.setTransactionManager(_txMgr);
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

    public InstanceManagement getInstanceManagement() {
        return _mgtService.getInstanceMgmt();
    }

    public ProcessManagement getProcessManagement() {
        return _mgtService.getProcessMgmt();
    }


    /**
     * Register event listeners configured in the configuration.
     *
     */
    private void registerEventListeners() {

        // let's always register the debugging listener....
        _server.registerBpelEventListener(new DebugBpelEventListener());

        // then, whatever else they want.
        String listenersStr = _odeConfig.getEventListeners();
        if (listenersStr != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
                String listenerCN = tokenizer.nextToken();
                try {
                    _server.registerBpelEventListener((BpelEventListener) Class.forName(listenerCN).newInstance());
                    __log.info(__msgs.msgBpelEventListenerRegistered(listenerCN));
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + listenerCN + ", the class couldn't be "
                            + "loaded properly: " + e);
                }
            }

        }
    }

    private void registerMexInterceptors() {
        String listenersStr = _odeConfig.getMessageExchangeInterceptors();
        if (listenersStr != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
                String interceptorCN = tokenizer.nextToken();
                try {
                    _server.registerMessageExchangeInterceptor((MessageExchangeInterceptor) Class.forName(interceptorCN).newInstance());
                    __log.info(__msgs.msgMessageExchangeInterceptorRegistered(interceptorCN));
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + interceptorCN + ", the class couldn't be "
                            + "loaded properly: " + e);
                }
            }
        }
    }

    private void registerExtensionActivityBundles() {
        String extensionsRTStr = _odeConfig.getExtensionActivityBundlesRT();
        String extensionsValStr = _odeConfig.getExtensionActivityBundlesValidation();
        if (extensionsRTStr != null) {
            // TODO replace StringTokenizer by regex
            for (StringTokenizer tokenizer = new StringTokenizer(extensionsRTStr, ",;"); tokenizer.hasMoreTokens();) {
                String bundleCN = tokenizer.nextToken();
                try {
                    // instantiate bundle
                    ExtensionBundleRuntime bundleRT = (ExtensionBundleRuntime) Class.forName(bundleCN).newInstance();
                    // register extension bundle (BPEL server)
                    _server.registerExtensionBundle(bundleRT);
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
            _store.setExtensionValidators(validators);
        }
    }

    private void registerExternalVariableModules() {
        JdbcExternalVariableModule jdbcext;
        jdbcext = new JdbcExternalVariableModule();
        jdbcext.registerDataSource("ode", _db.getDataSource());
        _server.registerExternalVariableEngine(jdbcext);

    }

    private class ProcessStoreListenerImpl implements ProcessStoreListener {

        public void onProcessStoreEvent(ProcessStoreEvent event) {
            handleEvent(event);
        }

    }

    private void handleEvent(ProcessStoreEvent pse) {
        __log.debug("Process store event: " + pse);
        switch (pse.type) {
            case ACTIVATED:
            case RETIRED:
                // bounce the process
                _server.unregister(pse.pid);
                ProcessConf pconf = _store.getProcessConfiguration(pse.pid);
                if (pconf != null) _server.register(pconf);
                else __log.debug("slighly odd: recevied event " + pse + " for process not in store!");
                break;
            case DISABLED:
            case UNDEPLOYED:
                _server.unregister(pse.pid);
                break;
            default:
                __log.debug("Ignoring store event: " + pse);
        }
    }

    // Transactional debugging stuff, to track down all these little annoying bugs.
    private class DebugTxMgr implements TransactionManager {
        private TransactionManager _tm;

        public DebugTxMgr(TransactionManager tm) {
            _tm = tm;
        }

        public void begin() throws NotSupportedException, SystemException {
            __logTx.debug("Txm begin");
            _tm.begin();
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            __logTx.debug("Txm commit");
            for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) {
                __logTx.debug(traceElement.toString());
            }
            _tm.commit();
        }

        public int getStatus() throws SystemException {
            __logTx.debug("Txm status");
            return _tm.getStatus();
        }

        public Transaction getTransaction() throws SystemException {
            Transaction tx = _tm.getTransaction();
            __logTx.debug("Txm get tx " + tx);
            return tx == null ? null : new DebugTx(tx);
        }

        public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
            __logTx.debug("Txm resume");
            _tm.resume(transaction);
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            __logTx.debug("Txm rollback");
            _tm.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            __logTx.debug("Txm set rollback");
            _tm.setRollbackOnly();
        }

        public void setTransactionTimeout(int i) throws SystemException {
            __logTx.debug("Txm set tiemout " + i);
            _tm.setTransactionTimeout(i);
        }

        public Transaction suspend() throws SystemException {
            __logTx.debug("Txm suspend");
            return _tm.suspend();
        }
    }

    private class DebugTx implements Transaction {
        private Transaction _tx;

        public DebugTx(Transaction tx) {
            _tx = tx;
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
            __logTx.debug("Tx commit");
            _tx.commit();
        }

        public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
            return _tx.delistResource(xaResource, i);
        }

        public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
            return _tx.enlistResource(xaResource);
        }

        public int getStatus() throws SystemException {
            return _tx.getStatus();
        }

        public void registerSynchronization(Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
            __logTx.debug("Synchronization registration on " + synchronization.getClass().getName());
            _tx.registerSynchronization(synchronization);
        }

        public void rollback() throws IllegalStateException, SystemException {
            __logTx.debug("Tx rollback");
            _tx.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            __logTx.debug("Tx set rollback");
            _tx.setRollbackOnly();
        }
    }

}
