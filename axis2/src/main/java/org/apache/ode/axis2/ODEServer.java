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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.deploy.DeploymentPoller;
import org.apache.ode.axis2.service.DeploymentWebService;
import org.apache.ode.axis2.service.ManagementService;
import org.apache.ode.axis2.util.ClusterUrlTransformer;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.engine.CountLRUDehydrationPolicy;
import org.apache.ode.bpel.engine.cron.CronScheduler;
import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.bpel.pmapi.InstanceManagement;
import org.apache.ode.bpel.pmapi.ProcessManagement;
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
    protected final Log __logTx = LogFactory.getLog("org.apache.ode.tx");

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    protected File _appRoot;

    protected File _workRoot;

    protected File _configRoot;

    protected BpelServerImpl _bpelServer;

    protected ProcessStoreImpl _store;

    protected ODEConfigProperties _odeConfig;

    protected AxisConfiguration _axisConfig;

    protected TransactionManager _txMgr;

    protected BpelDAOConnectionFactory _daoCF;

    protected ExecutorService _executorService;

    protected Scheduler _scheduler;

    protected CronScheduler _cronScheduler;

    protected Database _db;

    private DeploymentPoller _poller;

    private BpelServerConnector _connector;

    private ManagementService _mgtService;

    protected ClusterUrlTransformer _clusterUrlTransformer;

    protected MultiThreadedHttpConnectionManager httpConnectionManager;
    protected IdleConnectionTimeoutThread idleConnectionTimeoutThread;
    
    public Runnable txMgrCreatedCallback;

    public void init(ServletConfig config, AxisConfiguration axisConf) throws ServletException {
        init(config.getServletContext().getRealPath("/WEB-INF"), axisConf);
    }

    public void init(String contextPath, AxisConfiguration axisConf) throws ServletException {
        init(contextPath, axisConf, null);
    }
    
    public void init(String contextPath, AxisConfiguration axisConf, ODEConfigProperties config) throws ServletException {
        _axisConfig = axisConf;
        String rootDir = System.getProperty("org.apache.ode.rootDir");
        if (rootDir != null) _appRoot = new File(rootDir);
        else _appRoot = new File(contextPath);

        if (!_appRoot.isDirectory())
            throw new IllegalArgumentException(_appRoot + " does not exist or is not a directory");
        TempFileManager.setWorkingDirectory(_appRoot);

        __log.debug("Loading properties");
        String confDir = System.getProperty("org.apache.ode.configDir");
        _configRoot = confDir == null ? new File(_appRoot, "conf") : new File(confDir);
        if (!_configRoot.isDirectory())
            throw new IllegalArgumentException(_configRoot + " does not exist or is not a directory");

        try {
            if (config == null) {
                _odeConfig = new ODEConfigProperties(_configRoot);
                _odeConfig.load();
            } else {
                _odeConfig = config;
            }
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
        if (!_workRoot.isDirectory())
            throw new IllegalArgumentException(_workRoot + " does not exist or is not a directory");

        __log.debug("Initializing transaction manager");
        initTxMgr();
        if (txMgrCreatedCallback != null) {
            txMgrCreatedCallback.run();
        }
        __log.debug("Creating data source.");
        initDataSource();
        __log.debug("Starting DAO.");
        initDAO();
        EndpointReferenceContextImpl eprContext = new EndpointReferenceContextImpl(this);
        __log.debug("Initializing BPEL process store.");
        initProcessStore(eprContext);
        __log.debug("Initializing BPEL server.");
        initBpelServer(eprContext);
        __log.debug("Initializing HTTP connection manager");
        initHttpConnectionManager();

        // Register BPEL event listeners configured in axis2.properties file.
        registerEventListeners();
        registerMexInterceptors();
        registerExternalVariableModules();

        _store.loadAll();

        try {
            _bpelServer.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeBpelServerStartFailure();
            __log.error(errmsg, ex);
            throw new ServletException(errmsg, ex);
        }

        _poller = getDeploymentPollerExt();
        if( _poller == null ) {
            _poller = new DeploymentPoller(_store.getDeployDir(), this);
        }

        _mgtService = new ManagementService();
        _mgtService.enableService(_axisConfig, _bpelServer, _store, _appRoot.getAbsolutePath());

        try {
            __log.debug("Initializing Deployment Web Service");
            new DeploymentWebService().enableService(_axisConfig, _store, _poller, _appRoot.getAbsolutePath(), _workRoot.getAbsolutePath());
        } catch (Exception e) {
            throw new ServletException(e);
        }

        __log.debug("Starting scheduler");
        _scheduler.start();

        __log.debug("Initializing JCA adapter.");
        initConnector();

        _poller.start();
        __log.info(__msgs.msgPollingStarted(_store.getDeployDir().getAbsolutePath()));
        __log.info(__msgs.msgOdeStarted());
    }

    @SuppressWarnings("unchecked")
    private DeploymentPoller getDeploymentPollerExt() {
        DeploymentPoller poller = null;

        InputStream is = null;
        try {
            is = ODEServer.class.getResourceAsStream("/deploy-ext.properties");
            if( is != null ) {
                __log.info("A deploy-ext.properties found; will use the provided class if applicable.");
                try {
                    Properties props = new Properties();
                    props.load(is);
                    String deploymentPollerClass = props.getProperty("deploymentPoller.class");
                    if( deploymentPollerClass == null ) {
                        __log.warn("deploy-ext.properties found in the class path; however, the file does not have 'deploymentPoller.class' as one of the properties!!");
                    } else {
                        Class pollerClass = Class.forName(deploymentPollerClass);
                        poller = (DeploymentPoller)pollerClass.getConstructor(File.class, ODEServer.class).newInstance(_store.getDeployDir(), this);
                        __log.info("A custom deployment poller: " + deploymentPollerClass + " has been plugged in.");
                    }
                } catch( Exception e ) {
                    __log.warn("Deployment poller extension class is not loadable, falling back to the default DeploymentPoller.", e);
                }
            } else if( __log.isDebugEnabled() ) __log.debug("No deploy-ext.properties found.");
        } finally {
            try {
                if(is != null) is.close();
            } catch( IOException ie ) {
                // ignore
            }
        }

        return poller;
    }

    private void initDataSource() throws ServletException {
        _db = Database.create(_odeConfig);
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

    public TransactionManager getTransactionManager() {
        return _txMgr;
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

            if (_bpelServer != null)
                try {
                    __log.debug("shutting down ODE server.");
                    _bpelServer.shutdown();
                    _bpelServer = null;
                } catch (Throwable ex) {
                    __log.debug("Error stopping services.", ex);
                }

            if( _cronScheduler != null ) {
                try {
                    __log.debug("shutting down cron scheduler.");
                    _cronScheduler.shutdown();
                    _cronScheduler = null;
                } catch (Exception ex) {
                    __log.debug("Cron scheduler couldn't be shutdown.", ex);
                }
            }

            if (_scheduler != null)
                try {
                    __log.debug("shutting down scheduler.");
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
                    _connector = null;
                } catch (Throwable t) {
                    __log.error("Unable to cleanup temp files.", t);
                }
            }
            if (httpConnectionManager != null) {
                __log.debug("shutting down HTTP connection manager.");
                try {
                    httpConnectionManager.shutdown();
                    httpConnectionManager = null;
                } catch(Throwable t) {
                    __log.error("Unable to shut down HTTP connection manager.", t);
                }
            }
            if (idleConnectionTimeoutThread != null) {
                __log.debug("shutting down Idle Connection Timeout Thread.");
                try {
                    idleConnectionTimeoutThread.shutdown();
                    idleConnectionTimeoutThread = null;
                } catch(Throwable t) {
                    __log.error("Unable to shut down Idle Connection Timeout Thread.", t);
                }
            }
            try {
                __log.debug("cleaning up temporary files.");
                TempFileManager.cleanup();
            } catch (Throwable t) {
                __log.error("Unable to cleanup temp files.", t);
            }

            if (_executorService != null) {
                _executorService.shutdownNow();
                _executorService = null;
            }

            __log.info(__msgs.msgOdeShutdownCompleted());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @SuppressWarnings("unchecked")
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
            _connector.setBpelServer(_bpelServer);
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
        _store.setDeployDir(
                _odeConfig.getDeployDir() != null ?
                    new File(_odeConfig.getDeployDir()) :
                    new File(_workRoot, "processes"));
        _store.setConfigDir(_configRoot);
    }

    protected ProcessStoreImpl createProcessStore(EndpointReferenceContext eprContext, DataSource ds) {
        return new ProcessStoreImpl(eprContext, ds, _odeConfig.getDAOConnectionFactory(), _odeConfig, false);
    }

    protected Scheduler createScheduler() {
        SimpleScheduler scheduler = new SimpleScheduler(new GUID().toString(),
                new JdbcDelegate(_db.getDataSource()), _odeConfig.getProperties());
        scheduler.setExecutorService(_executorService);
        scheduler.setTransactionManager(_txMgr);
        return scheduler;
    }

    private void initBpelServer(EndpointReferenceContextImpl eprContext) {
        if (__log.isDebugEnabled()) {
            __log.debug("ODE initializing");
        }
        ThreadFactory threadFactory = new ThreadFactory() {
            int threadNumber = 0;
            public Thread newThread(Runnable r) {
                threadNumber += 1;
                Thread t = new Thread(r, "ODEServer-"+threadNumber);
                t.setDaemon(true);
                return t;
            }
        };

        if (_odeConfig.getThreadPoolMaxSize() == 0)
            _executorService = Executors.newCachedThreadPool(threadFactory);
        else
            _executorService = Executors.newFixedThreadPool(_odeConfig.getThreadPoolMaxSize(), threadFactory);

        {
            List<String> targets = new ArrayList<String>();
            Collections.addAll(targets, _odeConfig.getProperty("cluster.localRoute.targets", "").split(","));
            _clusterUrlTransformer = new ClusterUrlTransformer(targets, _odeConfig.getProperty("cluster.localRoute.base", "http://localhost:8080/ode/processes/"));
        }
        _bpelServer = new BpelServerImpl();
        _scheduler = createScheduler();
        _scheduler.setJobProcessor(_bpelServer);

        BpelServerImpl.PolledRunnableProcessor polledRunnableProcessor = new BpelServerImpl.PolledRunnableProcessor();
        polledRunnableProcessor.setPolledRunnableExecutorService(_executorService);
        polledRunnableProcessor.setContexts(_bpelServer.getContexts());
        _scheduler.setPolledRunnableProcesser(polledRunnableProcessor);

        _cronScheduler = new CronScheduler();
        _cronScheduler.setScheduledTaskExec(_executorService);
        _cronScheduler.setContexts(_bpelServer.getContexts());
        _bpelServer.setCronScheduler(_cronScheduler);

        _bpelServer.setDaoConnectionFactory(_daoCF);
        _bpelServer.setInMemDaoConnectionFactory(new BpelDAOConnectionFactoryImpl(_scheduler, _odeConfig.getInMemMexTtl()));
        _bpelServer.setEndpointReferenceContext(eprContext);
        _bpelServer.setMessageExchangeContext(new MessageExchangeContextImpl(this));
        _bpelServer.setBindingContext(new BindingContextImpl(this));
        _bpelServer.setScheduler(_scheduler);
        if (_odeConfig.isDehydrationEnabled()) {
            CountLRUDehydrationPolicy dehy = new CountLRUDehydrationPolicy();
            dehy.setProcessMaxAge(_odeConfig.getDehydrationMaximumAge());
            dehy.setProcessMaxCount(_odeConfig.getDehydrationMaximumCount());
            _bpelServer.setDehydrationPolicy(dehy);
        }
        _bpelServer.setMigrationTransactionTimeout(_odeConfig.getMigrationTransactionTimeout());
        _bpelServer.setConfigProperties(_odeConfig.getProperties());
        _bpelServer.init();
        _bpelServer.setInstanceThrottledMaximumCount(_odeConfig.getInstanceThrottledMaximumCount());
        _bpelServer.setProcessThrottledMaximumCount(_odeConfig.getProcessThrottledMaximumCount());
        _bpelServer.setProcessThrottledMaximumSize(_odeConfig.getProcessThrottledMaximumSize());
        _bpelServer.setHydrationLazy(_odeConfig.isHydrationLazy());
        _bpelServer.setHydrationLazyMinimumSize(_odeConfig.getHydrationLazyMinimumSize());
    }

    private void initHttpConnectionManager() throws ServletException {
        httpConnectionManager = new MultiThreadedHttpConnectionManager();
        // settings may be overridden from ode-axis2.properties using the same properties as HttpClient
        // /!\ If the size of the conn pool is smaller than the size of the thread pool, the thread pool might get starved.
        int max_per_host = Integer.parseInt(_odeConfig.getProperty(HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, ""+_odeConfig.getPoolMaxSize()));
        int max_total = Integer.parseInt(_odeConfig.getProperty(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, ""+_odeConfig.getPoolMaxSize()));
        if(__log.isDebugEnabled()) {
            __log.debug(HttpConnectionManagerParams.MAX_HOST_CONNECTIONS+"="+max_per_host);
            __log.debug(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS+"="+max_total);
        }
        if(max_per_host<1 || max_total <1){
            String errmsg = HttpConnectionManagerParams.MAX_HOST_CONNECTIONS+" and "+ HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS+" must be positive integers!";
            __log.error(errmsg);
            throw new ServletException(errmsg);
        }
        httpConnectionManager.getParams().setDefaultMaxConnectionsPerHost(max_per_host);
        httpConnectionManager.getParams().setMaxTotalConnections(max_total);

        // Register the connection manager to a idle check thread
        idleConnectionTimeoutThread = new IdleConnectionTimeoutThread();
        idleConnectionTimeoutThread.setName("Http_Idle_Connection_Timeout_Thread");
        long idleConnectionTimeout = Long.parseLong(_odeConfig.getProperty("http.idle.connection.timeout", "30000"));
        long idleConnectionCheckInterval = Long.parseLong(_odeConfig.getProperty("http.idle.connection.check.interval", "30000"));

        if(__log.isDebugEnabled()){
            __log.debug("http.idle.connection.timeout="+idleConnectionTimeout);
            __log.debug("http.idle.connection.check.interval="+idleConnectionCheckInterval);
        }
        idleConnectionTimeoutThread.setConnectionTimeout(idleConnectionTimeout);
        idleConnectionTimeoutThread.setTimeoutInterval(idleConnectionCheckInterval);

        idleConnectionTimeoutThread.addConnectionManager(httpConnectionManager);
        idleConnectionTimeoutThread.start();
    }

    public ProcessStoreImpl getProcessStore() {
        return _store;
    }

    public BpelServerImpl getBpelServer() {
        return _bpelServer;
    }

    public InstanceManagement getInstanceManagement() {
        return _mgtService.getInstanceMgmt();
    }

    public ProcessManagement getProcessManagement() {
        return _mgtService.getProcessMgmt();
    }

    public File getAppRoot() {
        return _appRoot;
    }

    public File getConfigRoot() {
        return _configRoot;
    }

    private void registerEventListeners() {
        String listenersStr = _odeConfig.getEventListeners();
        if (listenersStr != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(listenersStr, ",;"); tokenizer.hasMoreTokens();) {
                String listenerCN = tokenizer.nextToken();
                try {
                    _bpelServer.registerBpelEventListener((BpelEventListener) Class.forName(listenerCN).newInstance());
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
                    _bpelServer.registerMessageExchangeInterceptor((MessageExchangeInterceptor) Class.forName(interceptorCN).newInstance());
                    __log.info(__msgs.msgMessageExchangeInterceptorRegistered(interceptorCN));
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + interceptorCN + ", the class couldn't be "
                            + "loaded properly: " + e);
                }
            }
        }
    }

    private void registerExternalVariableModules() {
        JdbcExternalVariableModule jdbcext;
        jdbcext = new JdbcExternalVariableModule();
        jdbcext.registerDataSource("ode", _db.getDataSource());
        _bpelServer.registerExternalVariableEngine(jdbcext);

    }

    private class ProcessStoreListenerImpl implements ProcessStoreListener {

        public void onProcessStoreEvent(ProcessStoreEvent event) {
            handleEvent(event);
        }

    }

    private void handleEvent(ProcessStoreEvent pse) {
        if (__log.isDebugEnabled()) {
            __log.debug("Process store event: " + pse);
        }
        ProcessConf pconf = _store.getProcessConfiguration(pse.pid);
        switch (pse.type) {
            case DEPLOYED:
                if (pconf != null) {
                    /*
                     * If and only if an old process exists with the same pid, the old process is cleaned up.
                     * The following line is IMPORTANT and used for the case when the deployment and store
                     * do not have the process while the process itself exists in the BPEL_PROCESS table.
                     * Notice that the new process is actually created on the 'ACTIVATED' event.
                     */
                    _bpelServer.cleanupProcess(pconf);
                }
                break;
            case ACTVIATED:
                // bounce the process
                _bpelServer.unregister(pse.pid);
                if (pconf != null) {
                    _bpelServer.register(pconf);
                } else {
                    __log.debug("slighly odd: recevied event " +
                            pse + " for process not in store!");
                }
                break;
            case RETIRED:
                // are there are instances of this process running?
                boolean instantiated = _bpelServer.hasActiveInstances(pse.pid);
                // remove the process
                _bpelServer.unregister(pse.pid);
                // bounce the process if necessary
                if (instantiated) {
                    if (pconf != null) {
                        _bpelServer.register(pconf);
                    } else {
                        __log.debug("slighly odd: recevied event " +
                                pse + " for process not in store!");
                    }
                } else {
                    // we may have potentially created a lot of garbage, so,
                    // let's hope the garbage collector is configured properly.
                    if (pconf != null) {
                        _bpelServer.cleanupProcess(pconf);
                    }
                }
                break;
            case DISABLED:
            case UNDEPLOYED:
                _bpelServer.unregister(pse.pid);
                if (pconf != null) {
                    _bpelServer.cleanupProcess(pconf);
                }
                break;
            default:
                __log.debug("Ignoring store event: " + pse);
        }

        if( pconf != null ) {
            if( pse.type == ProcessStoreEvent.Type.UNDEPLOYED) {
                __log.debug("Cancelling all cron scheduled jobs on store event: " + pse);
                _bpelServer.getContexts().cronScheduler.cancelProcessCronJobs(pse.pid, true);
            }

            // Except for undeploy event, we need to re-schedule process dependent jobs
            __log.debug("(Re)scheduling cron scheduled jobs on store event: " + pse);
            if( pse.type != ProcessStoreEvent.Type.UNDEPLOYED) {
                _bpelServer.getContexts().cronScheduler.scheduleProcessCronJobs(pse.pid, pconf);
            }
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
            if (__log.isDebugEnabled()) {
                __logTx.debug("Txm commit");
                for (StackTraceElement traceElement : Thread.currentThread().getStackTrace()) {
                    __logTx.debug(traceElement.toString());
                }
            }
            _tm.commit();
        }

        public int getStatus() throws SystemException {
            __logTx.debug("Txm status");
            return _tm.getStatus();
        }

        public Transaction getTransaction() throws SystemException {
            Transaction tx = _tm.getTransaction();
            if (__log.isDebugEnabled()) {
                __logTx.debug("Txm get tx " + tx);
            }
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
            if (__log.isDebugEnabled()) {
                __logTx.debug("Txm set tiemout " + i);
            }
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
            if (__log.isDebugEnabled()) {
                __logTx.debug("Synchronization registration on " + synchronization.getClass().getName());
            }
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
