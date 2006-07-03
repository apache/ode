package com.fs.pxe.jbi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;
import org.opentools.minerva.MinervaPool;
import org.opentools.minerva.MinervaPool.PoolType;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.management.*;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import com.fs.pxe.bpel.engine.BpelServerImpl;
import com.fs.pxe.bpel.scheduler.quartz.QuartzSchedulerImpl;
import com.fs.pxe.daohib.DataSourceConnectionProvider;
import com.fs.pxe.daohib.HibernateTransactionManagerLookup;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.BpelDAOConnectionFactoryImpl;
import com.fs.pxe.jbi.msgmap.JbiWsdl11WrapperMapper;
import com.fs.pxe.jbi.msgmap.Mapper;

import com.fs.utils.fs.TempFileManager;

/**
 * This class implements ComponentLifeCycle. The JBI framework will start this
 * engine class automatically when JBI framework starts up.
 */
public class PxeLifeCycle implements ComponentLifeCycle {

  private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";

  private static final Messages __msgs = Messages.getMessages(Messages.class);

  private static final Log __log = LogFactory.getLog(PxeLifeCycle.class);

  private PxeSUManager _suManager = null;

  private boolean _initSuccess = false;

  private PxeContext _pxe;

  private Receiver _receiver;

  private boolean _started;

  private boolean _needDerbyShutdown;

  private String _derbyUrl;

  ServiceUnitManager getSUManager() {
    return _suManager;
  }

  PxeContext getPxeContext() {
    return _pxe;
  }

  public ObjectName getExtensionMBeanName() {
    return null;
  }

  public void init(ComponentContext context) throws JBIException {
    try {
      _pxe = PxeContext.getInstance();
      _pxe.setContext(context);
      _pxe._consumer = new PxeConsumer(_pxe);

      
      TempFileManager.setWorkingDirectory(new File(_pxe.getContext()
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

      _suManager = new PxeSUManager(_pxe);
      _initSuccess = true;
      __log.info(__msgs.msgPxeInitialized());
    } finally {
      if (!_initSuccess) {
        // TODO ..then what?
      }
    }
  }

  private void initMappers() throws JBIException  {
    Class mapperClass;
    try {
      mapperClass = Class.forName(_pxe._config.getMessageMapper());
    } catch (ClassNotFoundException e) {
      String errmsg = __msgs.msgPxeInitMapperClassNotFound(_pxe._config.getMessageMapper());
      __log.error(errmsg);
      throw new JBIException(errmsg, e);
    } catch (Throwable t) {
      String errmsg = __msgs.msgPxeInitMapperClassLoadFailed(_pxe._config.getMessageMapper());
      __log.error(errmsg);
      throw new JBIException(errmsg, t);
    }
    try {
      _pxe.registerMapper((Mapper) mapperClass.newInstance());
    } catch (Throwable t) {
      String errmsg = __msgs.msgPxeInitMapperInstantiationFailed(_pxe._config.getMessageMapper());
      __log.error(errmsg);
      throw new JBIException(errmsg, t);
    }
  }

  private void initDataSource() throws JBIException {
    switch (_pxe._config.getDbMode()) {
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
      _pxe._dataSource = lookupInJndi(_pxe._config.getDbDataSource());
      __log.info(__msgs.msgPxeUsingExternalDb(_pxe._config.getDbDataSource()));
    } catch (Exception ex) {
      String msg = __msgs.msgPxeInitExternalDbFailed(_pxe._config.getDbDataSource());
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
      "jdbc:derby:" + _pxe.getContext().getInstallRoot() + "/"
      + _pxe._config.getDbEmbeddedName();
    
    __log.debug("creating Minerva pool for " + url);
    
    MinervaPool minervaPool = new MinervaPool();
    minervaPool.setTransactionManager(_pxe.getTransactionManager());
    minervaPool.getConnectionFactory().setConnectionURL(url);
    minervaPool.getConnectionFactory().setUserName("sa");
    minervaPool.getConnectionFactory().setDriver(
        org.apache.derby.jdbc.EmbeddedDriver.class.getName());

    minervaPool.getPoolParams().maxSize = _pxe._config.getPoolMaxSize();
    minervaPool.getPoolParams().minSize = _pxe._config.getPoolMinSize();
    minervaPool.getPoolParams().blocking = false;
    minervaPool.setType(PoolType.MANAGED);
    
    try {
      minervaPool.start();
    } catch (Exception ex) {
      String errmsg = __msgs.msgPxeDbPoolStartupFailed(url);
      __log.error(errmsg,ex);
      throw new JBIException(errmsg,ex);
    }

    _pxe._dataSource = minervaPool.createDataSource();
    _needDerbyShutdown = true;
    _derbyUrl = url;
  }

  /**
   * Load the "pxe-jbi.properties" file from the install directory.
   * 
   * @throws JBIException
   */
  private void initProperties() throws JBIException {
    PxeConfigProperties config = new PxeConfigProperties(_pxe.getContext()
        .getInstallRoot());
    config.load();
    _pxe._config = config;
  }

  private void initBpelServer() throws JBIException {
    if (__log.isDebugEnabled()) {
      __log.debug("PXE initializing");
    }

    _pxe._server = new BpelServerImpl();
    // We don't want the server to automatically activate deployed processes,
    // we'll do that explcitly
    _pxe._server.setAutoActivate(false);
    _pxe._eprContext = new EndpointReferenceContextImpl(_pxe);
    _pxe._mexContext = new MessageExchangeContextImpl(_pxe);
    _pxe._executorService = Executors.newCachedThreadPool();
    _pxe._scheduler = new QuartzSchedulerImpl();
    _pxe._scheduler.setBpelServer(_pxe._server);
    _pxe._scheduler.setExecutorService(_pxe._executorService, 20);
    _pxe._scheduler.setTransactionManager((TransactionManager) _pxe
        .getContext().getTransactionManager());
    _pxe._scheduler.setDataSource(_pxe._dataSource);
    _pxe._scheduler.init();

    _pxe._server.setDaoConnectionFactory(_pxe._daocf);
    _pxe._server.setEndpointReferenceContext(_pxe._eprContext);
    _pxe._server.setMessageExchangeContext(_pxe._mexContext);
    _pxe._server.setScheduler(_pxe._scheduler);
    _pxe._server.init();

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
    
    try {
      properties.put(Environment.DIALECT, guessDialect(_pxe._dataSource));
    } catch (Exception ex) {
      String errmsg = __msgs.msgPxeInitHibernateDialectDetectFailed();
      __log.error(errmsg,ex);
      throw new JBIException(errmsg,ex);
    }

    File hibernatePropFile = new File(_pxe.getContext().getInstallRoot()
        + File.separatorChar + "hibernate.properties");

    if (hibernatePropFile.exists()) {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(hibernatePropFile);
        properties.load(new BufferedInputStream(fis));
      } catch (IOException e) {
        String errmsg = __msgs
            .msgPxeInitHibernateErrorReadingHibernateProperties(hibernatePropFile);
        __log.error(errmsg, e);
        throw new JBIException(errmsg, e);
      }
    } else {
      __log.warn(__msgs
          .msgPxeInitHibernatePropertiesNotFound(hibernatePropFile));
    }

    SessionManager sm = new SessionManager(properties, _pxe._dataSource, _pxe
        .getTransactionManager());
    _pxe._daocf = new BpelDAOConnectionFactoryImpl(sm);
  }

  public synchronized void start() throws JBIException {
    if (_started)
      return;
    
    try {
      __log.info(__msgs.msgPxeStarting());

      if (!_initSuccess) {
        String errmsg = "attempt to call start() after init() failure.";
        IllegalStateException ex = new IllegalStateException(errmsg); 
        __log.fatal(errmsg, ex);
        throw new JBIException(errmsg,ex);
      }

      if (_pxe.getChannel() == null) {
        throw (new JBIException("No channel!", new NullPointerException()));
      }

      try {
        _pxe._server.start();
      } catch (Exception ex) {
        String errmsg = __msgs.msgPxeBpelServerStartFailure();
        __log.error(errmsg,ex);
        throw new JBIException(errmsg, ex);
      }

      _receiver = new Receiver(_pxe);
      _receiver.start();
      _started = true;
      __log.info(__msgs.msgPxeStarted());
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

    __log.info("Stopping PXE.");

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
        _pxe._server.stop();

      } catch (Throwable ex) {
        __log.fatal("Error stopping services.", ex);
      }

      __log.info("PXE stopped.");
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
    try {

      try {
        __log.debug("shutting down quartz scheduler.");
        _pxe._scheduler.shutdown();
      } catch (Exception ex) {

      }

      __log.debug("cleaning up temporary files.");
      TempFileManager.cleanup();

      _suManager = null;
      _pxe = null;

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
          Dialect hbDialect = hbDialect = DialectFactory.determineDialect(
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
