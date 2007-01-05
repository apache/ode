package org.apache.ode.axis2;

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
import org.apache.ode.bpel.iapi.BpelEventListener;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.dao.jpa.ojpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.fs.TempFileManager;
import org.apache.openjpa.ee.ManagedRuntime;
import org.opentools.minerva.MinervaPool;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class ODEServerJPA extends ODEServer {

    private static final Log __log = LogFactory.getLog(ODEServer.class);
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
    private String _dbType;

    public void init(ServletConfig config, AxisConfiguration axisConf) throws ServletException {
        System.out.println("######################################################################");
        System.out.println("###### INITIALIZING WITH JPA                             #############");
        System.out.println("######################################################################");

        _axisConfig = axisConf;
        _appRoot = new File(config.getServletContext().getRealPath("/WEB-INF"));
        TempFileManager.setWorkingDirectory(_appRoot);

        __log.debug("Loading properties");
        String confDir = System.getProperty("org.apache.ode.configDir");
        if (confDir == null) _odeConfig = new ODEConfigProperties(new File(_appRoot, "conf"));
        else _odeConfig = new ODEConfigProperties(new File(confDir));
        _odeConfig.load();

        String wdir = _odeConfig.getWorkingDir();
        if (wdir == null) _workRoot = _appRoot;
        else _workRoot = new File(wdir.trim());

        __log.debug("Initializing transaction manager");
        initTxMgr();

        __log.debug("Creating data source.");
        initDataSource();

        __log.debug("Starting OpenJPA.");
        initJPA();
        __log.debug("OpenJPA started.");

        __log.debug("Initializing BPEL process store.");
        initProcessStore();

        __log.debug("Initializing BPEL server.");
        initBpelServer();


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
        new DeploymentWebService().enableService(_axisConfig, _server, _store, _poller, _appRoot.getAbsolutePath(), _workRoot
                .getAbsolutePath());

        _poller.start();
        __log.info(__msgs.msgPollingStarted(deploymentDir.getAbsolutePath()));

        __log.info(__msgs.msgOdeStarted());
    }

    /**
     * Shutdown the service engine. This performs cleanup before the BPE is
     * terminated. Once this method has been called, init() must be called before
     * the transformation engine can be started again with a call to start().
     *
     * @throws org.apache.axis2.AxisFault if the engine is unable to shut down.
     */
    public void shutDown() throws AxisFault {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        if (_poller != null) {
        _poller.stop();
        _poller = null;
        }

        try {
            _server.stop();
        } catch (Throwable ex) {
            __log.fatal("Error stopping services.", ex);
        }
        __log.info("ODE stopped.");

        try {
            try {
                __log.debug("shutting down quartz scheduler.");
                _scheduler.shutdown();
            } catch (Exception ex) {
                __log.error("Scheduler couldn't be shutdown.", ex);
            }

            __log.debug("cleaning up temporary files.");
            TempFileManager.cleanup();

            __log.debug("shutting down transaction manager.");
            _txMgr = null;

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

        // Setting our new service on the receiver, the same receiver handles all
        // operations so the first one should fit them all
        AxisOperation firstOp = (AxisOperation) axisService.getOperations().next();
        ((ODEMessageReceiver) firstOp.getMessageReceiver()).setService(odeService);
        ((ODEMessageReceiver) firstOp.getMessageReceiver()).setExecutorService(_executorService);

        // We're public!
        _axisConfig.addService(axisService);
        __log.debug("Created Axis2 service " + serviceName);
        return odeService;
    }

    public ExternalService createExternalService(Definition def, QName serviceName, String portName) {
        ExternalService extService = (ExternalService) _externalServices.get(serviceName);
        if (extService != null)
            return extService;

        extService = new ExternalService(def, serviceName, portName, _executorService, _axisConfig);
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
        // TODO Normally this lookup should't exist as there could be more one than port
        // TODO for a portType. See MessageExchangeContextImpl.
        for (Object o : _services.values()) {
            ODEService service = (ODEService) o;
            if (service.respondsTo(serviceName, portTypeName)) return service;
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
            _datasource = lookupInJndi(_odeConfig.getDbDataSource());
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

        String url = "jdbc:derby:" + _workRoot + "/jpadb/" + _odeConfig.getDbEmbeddedName();

        __log.debug("creating Minerva pool for " + url);

        MinervaPool minervaPool = new MinervaPool();
        minervaPool.setTransactionManager(_txMgr);
        minervaPool.getConnectionFactory().setConnectionURL(url);
        minervaPool.getConnectionFactory().setUserName("sa");
        minervaPool.getConnectionFactory().setDriver(
                org.apache.derby.jdbc.EmbeddedDriver.class.getName());

        minervaPool.getPoolParams().maxSize = _odeConfig.getPoolMaxSize();
        minervaPool.getPoolParams().minSize = _odeConfig.getPoolMinSize();
        minervaPool.getPoolParams().blocking = false;
        minervaPool.setType(MinervaPool.PoolType.MANAGED);

        try {
            minervaPool.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(url);
            __log.error(errmsg, ex);
            throw new ServletException(errmsg, ex);
        }

        _datasource = minervaPool.createDataSource();
    }

    private void initJPA() {
        HashMap propMap = new HashMap();
        propMap.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");
        propMap.put("openjpa.ManagedRuntime", new TxMgrProvider());
        propMap.put("openjpa.ConnectionDriverName", org.apache.derby.jdbc.EmbeddedDriver.class.getName());
        propMap.put("javax.persistence.nonJtaDataSource", _datasource);
        propMap.put("openjpa.Log", "DefaultLevel=TRACE");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ode-dao", propMap);
//        propMap.put("openjpa.ConnectionUserName", "sa");
//        propMap.put("openjpa.ConnectionPassword", "");
//        propMap.put("openjpa.ConnectionDriverName", org.apache.derby.jdbc.EmbeddedDriver.class.getName());
//        propMap.put("ConnectionDriverName", org.apache.derby.jdbc.EmbeddedDriver.class.getName());
//        propMap.put("openjpa.ConnectionURL", url);
        EntityManager em = emf.createEntityManager();
//        ((EntityManagerImpl)em).
        _daoCF = new BPELDAOConnectionFactoryImpl(em);
    }

    public class TxMgrProvider implements ManagedRuntime {
        public TxMgrProvider() {
        }
        public TransactionManager getTransactionManager() throws Exception {
            return _txMgr;
        }
    }

    /**
     * Initialize the Hibernate data store.
     */
//    private void initHibernate() throws ServletException {
//        Properties properties = new Properties();
//        properties.put(Environment.CONNECTION_PROVIDER,
//                DataSourceConnectionProvider.class.getName());
//        properties.put(Environment.TRANSACTION_MANAGER_STRATEGY,
//                HibernateTransactionManagerLookup.class.getName());
//        // properties.put(Environment.SESSION_FACTORY_NAME, "jta");
//
//        File hibernatePropFile;
//        String confDir = System.getProperty("org.apache.ode.configDir");
//        if (confDir != null) hibernatePropFile = new File(confDir, "hibernate.properties");
//        else hibernatePropFile = new File(_appRoot, "conf" + File.separatorChar + "hibernate.properties");
//
//        if (hibernatePropFile.exists()) {
//            FileInputStream fis;
//            try {
//                fis = new FileInputStream(hibernatePropFile);
//                properties.load(new BufferedInputStream(fis));
//            } catch (IOException e) {
//                String errmsg = __msgs.msgOdeInitHibernateErrorReadingHibernateProperties(hibernatePropFile);
//                __log.error(errmsg, e);
//                throw new ServletException(errmsg, e);
//            }
//        } else {
//            __log.info(__msgs.msgOdeInitHibernatePropertiesNotFound(hibernatePropFile));
//        }
//
//        // Guess Hibernate dialect if not specified in hibernate.properties
//        if (properties.get(Environment.DIALECT) == null) {
//            try {
//                properties.put(Environment.DIALECT, guessDialect(_datasource));
//            } catch (Exception ex) {
//                String errmsg = __msgs.msgOdeInitHibernateDialectDetectFailed();
//                if (__log.isDebugEnabled()) __log.error(errmsg,ex);
//                else __log.error(errmsg);
//            }
//        }
//        if (properties.get(Environment.DIALECT) != null) {
//            String dialect = (String) properties.get(Environment.DIALECT);
//            if (dialect.equals("org.hibernate.dialect.SQLServerDialect"))
//                _dbType = "sqlserver";
//            else _dbType = "other";
//        }
//
//        SessionManager sm = new SessionManager(properties, _datasource, _txMgr);
//        _daoCF = new BpelDAOConnectionFactoryImpl(sm);
//    }

    private void initProcessStore() {
        _store = new ProcessStoreImpl(_datasource);
        _store.registerListener(new ProcessStoreListenerImpl());
    }

    private void initBpelServer() {
        if (__log.isDebugEnabled()) {
            __log.debug("ODE initializing");
        }
        _server = new BpelServerImpl();

        _executorService = Executors.newCachedThreadPool();
        _scheduler = new QuartzSchedulerImpl();
        _scheduler.setBpelServer(_server);
        _scheduler.setExecutorService(_executorService, 20);
        _scheduler.setTransactionManager(_txMgr);
        _scheduler.setDataSource(_datasource);
        _scheduler.init();

        _server.setDaoConnectionFactory(_daoCF);
        _server.setInMemDaoConnectionFactory(new org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl());
        _server.setEndpointReferenceContext(new EndpointReferenceContextImpl(this));
        _server.setMessageExchangeContext(
                new P2PMexContextImpl(this, new MessageExchangeContextImpl(this), _executorService, _txMgr));
        _server.setBindingContext(new BindingContextImpl(this, _store));
        _server.setScheduler(_scheduler);
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
                } catch (Exception e) {
                    __log.warn("Couldn't register the event listener " + listenerCN + ", the class couldn't be " +
                            "loaded properly.");
                }
            }

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

    private class ProcessStoreListenerImpl implements ProcessStoreListener {

        public void onProcessStoreEvent(ProcessStoreEvent event) {
            handleEvent(event);
        }

    }

}
