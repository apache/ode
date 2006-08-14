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

import com.fs.naming.mem.InMemoryContextFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.hooks.ManagementService;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.hooks.ODEMessageReceiver;
import org.apache.ode.bpel.connector.BpelServerConnector;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.daohib.DataSourceConnectionProvider;
import org.apache.ode.daohib.HibernateTransactionManagerLookup;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl;
import org.apache.ode.utils.fs.TempFileManager;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;
import org.objectweb.jotm.Jotm;
import org.opentools.minerva.MinervaPool;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class called by our Axis hooks to handle all ODE lifecycle
 * management.
 */
public class ODEServer {

  private static final Log __log = LogFactory.getLog(ODEServer.class);
  private static final Messages __msgs = Messages.getMessages(Messages.class);

  private File _appRoot;

  private BpelServerImpl _server;
  private ODEConfigProperties _odeConfig;
  private AxisConfiguration _axisConfig;
  private DataSource _datasource;
  private Jotm _jotm;
  private BpelDAOConnectionFactory _daoCF;
  private ExecutorService _executorService;
  private QuartzSchedulerImpl _scheduler;
  private DeploymentPoller _poller;

  private MultiKeyMap _services = new MultiKeyMap();
  private MultiKeyMap _externalServices = new MultiKeyMap();
  private BpelServerConnector _connector;

//  private HashMap<QName,ODEService> _services = new HashMap<QName,ODEService>();
//  private HashMap<QName,ExternalService> _externalServices = new HashMap<QName,ExternalService>();

  public void init(ServletConfig config, AxisConfiguration axisConf)  throws ServletException {
    _axisConfig = axisConf;
    _appRoot = new File(config.getServletContext().getRealPath("/WEB-INF"));
    TempFileManager.setWorkingDirectory(_appRoot);

    __log.debug("Loading properties");
    _odeConfig = new ODEConfigProperties(_appRoot);
    _odeConfig.load();

    __log.debug("Initializing transaction manager");
    initTxMgr();

    __log.debug("Creating data source.");
    initDataSource();

    __log.debug("Starting Hibernate.");
    initHibernate();
    __log.debug("Hibernate started.");

    __log.debug("Initializing BPEL server.");
    initBpelServer();

    try {
      _server.start();
    } catch (Exception ex) {
      String errmsg = __msgs.msgOdeBpelServerStartFailure();
      __log.error(errmsg,ex);
      throw new ServletException(errmsg, ex);
    }

    __log.debug("Initializing JCA adapter.");
    initConnector();
    
    File deploymentDir = new File(_appRoot, "processes");
    _poller = new DeploymentPoller(deploymentDir, this);
    _poller.start();
    __log.info(__msgs.msgPollingStarted(deploymentDir.getAbsolutePath()));

    new ManagementService().enableService(_axisConfig, _server, _appRoot.getAbsolutePath());

    __log.info(__msgs.msgOdeStarted());
  }

  /**
   * Shutdown the service engine. This performs cleanup before the BPE is
   * terminated. Once this method has been called, init() must be called before
   * the transformation engine can be started again with a call to start().
   *
   * @throws AxisFault if the engine is unable to shut down.
   */
  public void shutDown() throws AxisFault {
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

    _poller.stop();
    _poller = null;
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
      _jotm.stop();
      _jotm = null;

      __log.info(__msgs.msgOdeShutdownCompleted());
    } finally {
      Thread.currentThread().setContextClassLoader(old);
    }
  }

  public ODEService createService(Definition def, QName serviceName, String portName) throws AxisFault {
    if (_services.get(serviceName, portName) != null){
      AxisService service = ((ODEService)_services.get(serviceName, portName)).getAxisService();
      _axisConfig.removeService(service.getName());
    }
    AxisService axisService = ODEAxisService.createService(_axisConfig,
            def, serviceName, portName);
    ODEService odeService = new ODEService(axisService, def, serviceName, portName,
            _server, _jotm.getTransactionManager());
    _services.put(serviceName, portName, odeService);

    // Setting our new service on the receiver, the same receiver handles all
    // operations so the first one should fit them all
    AxisOperation firstOp = (AxisOperation)axisService.getOperations().next();
    ((ODEMessageReceiver)firstOp.getMessageReceiver()).setService(odeService);
    ((ODEMessageReceiver)firstOp.getMessageReceiver()).setExecutorService(_executorService);

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
    _externalServices.put(serviceName, portName, extService);
    __log.debug("Created external service " + serviceName);
    return extService;
  }

  public void destroyService(QName serviceName) {
    try {
      _axisConfig.removeService(serviceName.getLocalPart());
    } catch (AxisFault axisFault) {
      // TODO do something!
      axisFault.printStackTrace();
    }
    _services.remove(serviceName);
  }

  public ODEService getService(QName serviceName, String portName) {
    return (ODEService) _services.get(serviceName, portName);
  }

  public ODEService getService(QName serviceName, QName portTypeName) {
    // TODO Normally this lookup should't exist as there could be more one than port
    // TODO for a portType. See MessageExchnageContextImpl.
    for (Iterator iterator = _services.values().iterator(); iterator.hasNext();) {
      ODEService service = (ODEService) iterator.next();
      if (service.respondsTo(serviceName, portTypeName)) return service;
    }
    return null;
  }

  public ExternalService getExternalService(QName serviceName, String portName) {
    return (ExternalService) _externalServices.get(serviceName, portName);
  }

  public AxisInvoker createInvoker() {
    AxisInvoker invoker = new AxisInvoker(_executorService);
    return invoker;
  }

  private void initTxMgr() throws ServletException {
    try {
      _jotm = new Jotm(true, false);
      _jotm.getTransactionManager().setTransactionTimeout(30);

      Reference txm = new Reference("javax.transaction.TransactionManager",
              JotmTransactionManagerFactory.class.getName(), null);

      System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
              InMemoryContextFactory.class.getName());
      System.setProperty(Context.PROVIDER_URL, "ode");
      InitialContext ctx = new InitialContext();
      ctx.rebind("TransactionManager", txm);
      ctx.close();
    } catch (Exception ex) {
      __log.error("Error creating initial JNDI context.",ex);
      throw new ServletException("Failed to start JNDI!",ex);
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
        _connector.setPort(_odeConfig.getConnectorPort());
        _connector.setId("jcaServer");
        try {
          _connector.start();
        } catch (Exception e) {
        __log.error("Failed to initialize JCA connector.",e);
        }
      }
    }
  
  private void initExternalDb() throws ServletException {
    try {
      _datasource = lookupInJndi(_odeConfig.getDbDataSource());
      __log.info(__msgs.msgOdeUsingExternalDb(_odeConfig.getDbDataSource()));
    } catch (Exception ex) {
      String msg = __msgs.msgOdeInitExternalDbFailed(_odeConfig.getDbDataSource());
      __log.error(msg,ex);
      throw new ServletException(msg,ex);
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

    String url =
            "jdbc:derby:" + _appRoot + "/" + _odeConfig.getDbEmbeddedName();

    __log.debug("creating Minerva pool for " + url);

    MinervaPool minervaPool = new MinervaPool();
    minervaPool.setTransactionManager(_jotm.getTransactionManager());
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
      __log.error(errmsg,ex);
      throw new ServletException(errmsg,ex);
    }

    _datasource = minervaPool.createDataSource();
  }


  /**
   * Initialize the Hibernate data store.
   *
   * @throws ServletException
   */
  private void initHibernate() throws ServletException {
    Properties properties = new Properties();
    properties.put(Environment.CONNECTION_PROVIDER,
            DataSourceConnectionProvider.class.getName());
    properties.put(Environment.TRANSACTION_MANAGER_STRATEGY,
            HibernateTransactionManagerLookup.class.getName());
    properties.put(Environment.SESSION_FACTORY_NAME, "jta");

    try {
      properties.put(Environment.DIALECT, guessDialect(_datasource));
    } catch (Exception ex) {
      String errmsg = __msgs.msgOdeInitHibernateDialectDetectFailed();
      __log.error(errmsg,ex);
      throw new ServletException(errmsg,ex);
    }

    File hibernatePropFile = new File(_appRoot, "conf" + File.separatorChar + "hibernate.properties");

    if (hibernatePropFile.exists()) {
      FileInputStream fis;
      try {
        fis = new FileInputStream(hibernatePropFile);
        properties.load(new BufferedInputStream(fis));
      } catch (IOException e) {
        String errmsg = __msgs
                .msgOdeInitHibernateErrorReadingHibernateProperties(hibernatePropFile);
        __log.error(errmsg, e);
        throw new ServletException(errmsg, e);
      }
    } else {
      __log.warn(__msgs
              .msgOdeInitHibernatePropertiesNotFound(hibernatePropFile));
    }

    SessionManager sm = new SessionManager(properties, _datasource, _jotm.getTransactionManager());
    _daoCF = new BpelDAOConnectionFactoryImpl(sm);

    Reference bpelSscfRef = new Reference(BpelDAOConnectionFactory.class.getName(),
            HibernateDaoObjectFactory.class.getName(), null);
    try {
      InitialContext ctx = new InitialContext();
      try {
        if (_daoCF  != null)
          ctx.rebind("bpelSSCF", bpelSscfRef);
      } finally {
        ctx.close();
      }
    } catch (Exception ex) {
      throw new ServletException("Couldn't bind connection factory!", ex);
    }
  }

  private void initBpelServer() {
    if (__log.isDebugEnabled()) {
      __log.debug("ODE initializing");
    }

    _server = new BpelServerImpl();
    // We don't want the server to automatically activate deployed processes,
    // we'll do that explcitly
    _server.setAutoActivate(false);

    _executorService = Executors.newCachedThreadPool();
    _scheduler = new QuartzSchedulerImpl();
    _scheduler.setBpelServer(_server);
    _scheduler.setExecutorService(_executorService, 20);
    _scheduler.setTransactionManager(_jotm.getTransactionManager());
    _scheduler.setDataSource(_datasource);
    _scheduler.init();

    _server.setDaoConnectionFactory(_daoCF);
    _server.setEndpointReferenceContext(new EndpointReferenceContextImpl(this));
    _server.setMessageExchangeContext(new MessageExchangeContextImpl(this));
    _server.setBindingContext(new BindingContextImpl(this));
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
        DialectFactory.DatabaseDialectMapper mapper = HIBERNATE_DIALECTS.get(dbProductName);
        if (mapper != null) {
          dialect = mapper.getDialectClass(dbMajorVer);
        } else {
          Dialect hbDialect = DialectFactory.determineDialect(dbProductName, dbMajorVer);
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

    return dialect;

  }

  public BpelServerImpl getBpelServer() {
    return _server;
  }

  private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";
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

  /**
   * An {@link javax.naming.spi.ObjectFactory} implementation that can be used to bind the
   * JOTM {@link javax.transaction.TransactionManager} implementation in JNDI.
   */
  private class JotmTransactionManagerFactory implements ObjectFactory {

    public Object getObjectInstance(Object objref, Name name, Context ctx, Hashtable env) throws Exception {
      Reference ref = (Reference) objref;
      if (ref.getClassName().equals(TransactionManager.class.getName())) {
        return _jotm.getTransactionManager();
      }
      throw new RuntimeException("The reference class name \"" + ref.getClassName() + "\" is unknown.");
    }
  }

  /**
   * JNDI {@link ObjectFactory} implementation for Hibernate-based
   * connection factory objects.
   */
  private class HibernateDaoObjectFactory implements ObjectFactory {

    public Object getObjectInstance(Object objref, Name name, Context ctx, Hashtable env) throws Exception {
      Reference ref = (Reference) objref;
      if (ref.getClassName().equals(BpelDAOConnectionFactory.class.getName())) {
        return _daoCF;
      }
      throw new RuntimeException("The reference class name \"" + ref.getClassName() + "\" is unknown.");
    }
  }
}
