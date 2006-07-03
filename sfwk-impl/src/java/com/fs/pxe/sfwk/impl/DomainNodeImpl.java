/*
 * File:      $Id: DomainNodeImpl.java 1505 2006-06-21 07:06:10Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.bapi.DomainConfig;
import com.fs.pxe.sfwk.bapi.DomainNode;
import com.fs.pxe.sfwk.bapi.ServiceProviderInstallationException;
import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;
import com.fs.pxe.sfwk.bapi.dao.SystemDAO;
import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.deployment.DeploymentException;
import com.fs.pxe.sfwk.deployment.ExpandedSAR;
import com.fs.pxe.sfwk.deployment.SarFormatException;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.mngmt.DomainNotification;
import com.fs.pxe.sfwk.spi.PxeException;
import com.fs.pxe.sfwk.spi.ServiceProvider;
import com.fs.pxe.sfwk.spi.ServiceProviderException;
import com.fs.utils.StreamUtils;
import com.fs.utils.fs.FileUtils;
import com.fs.utils.fs.TempFileManager;
import com.fs.utils.msg.MessageBundle;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.transaction.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The implementation of a PXE container node run-time.
 */
public class DomainNodeImpl extends DomainNode {
  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  private static final Log __log = LogFactory.getLog(DomainNodeImpl.class);

  private static final ThreadLocal<DomainNodeImpl> __ActiveTx = new ThreadLocal<DomainNodeImpl>();

  private static long __InstanceCounter;

  private long _nodeId = ++__InstanceCounter;

  private ManagementLock _lock = new ManagementLock();

  /** Transaction to DataStore Connection map. */
  private Map<Transaction, DomainStateConnection> _dconns = Collections
      .synchronizedMap(new HashMap<Transaction, DomainStateConnection>());

  /**
   * <b>serviceProviders</b>: <b>{@link String}</b>--><b>{@link
   * ServiceProviderBackend}</b> s.t. <i>(uri, spb)</i> in <b>serviceProviders</b>
   * iff <i>uri</i> is the URI of the service provider backended by <i>spb</i>
   */
  final Map<String, ServiceProviderBackend> _serviceProviders = new HashMap<String, ServiceProviderBackend>();

  /**
   * <b>systems</b>: <b>{@link SystemUUID}</b>--><b>{@link SystemBackend}</b>
   * s.t. <i>(uuid, sb)</i> in <b>system</b> iff <i>name</i> is the unique id
   * of the of the system backended by <i>sb</i>
   */
  final Map<SystemUUID, SystemBackend> _systems = Collections
      .synchronizedMap(new HashMap<SystemUUID, SystemBackend>());

  /**
   * <b>systemsByName</b>: <b>{@link String}</b>--><b>{@link SystemBackend}</b>
   * s.t. <i>(name, sb)</i> in <b>systemByName</b> iff <i>name</i> is the
   * name of the of the system backended by <i>sb</i>
   */
  final HashMap<String, SystemBackend> _systemsByName = new HashMap<String, SystemBackend>();

  /** Domain configuration object. */
  private DomainConfig _config;

  private ExecutorService _threadPool;

  private DomainAdminMBeanImpl _adminMBean;

  private File _domainDir;

  private File _systemsDir;

  /** Has the domain been initialized. */
  private boolean _initialized = false;

  /** Indicates that shutdown has been initiated. */
  private boolean _shutdown = false;

  public DomainNodeImpl(DomainConfig config) {
    _config = config;

    try {
      _adminMBean = new DomainAdminMBeanImpl(this);
    } catch (NotCompliantMBeanException ex) {
      throw new AssertionError(ex);
    }

    _domainDir = new File(System.getProperty("pxe.home"), _config.getDomainId());
    _systemsDir = new File(_domainDir, "systems");
  }

  // TODO create these in a 'domains' subdirectory
  public Object resolve(ObjectName objectName, Class cls) throws PxeException {
    if (objectName == null) {
      return null;
    }

    return MBeanServerInvocationHandler.newProxyInstance(_config
        .getMBeanServer(), objectName, cls, true);
  }

  /**
   * @throws ServiceProviderInstallationException
   * @see com.fs.pxe.sfwk.bapi.DomainNode#registerServiceProvider(com.fs.pxe.sfwk.spi.ServiceProvider)
   */
  public void registerServiceProvider(String uri,ServiceProvider provider)
      throws ServiceProviderInstallationException {
    if (__log.isTraceEnabled())
      __log.debug("DomainNodeImpl.registerServiceProvider("
          + uri + ")");

    _lock.acquireManagementLock();
    try {

      if (_serviceProviders.containsKey(uri)) {
        String msg = __msgs.msgSysProviderRegCfgDuplicatedId(uri);
        __log.error(msg);
        throw new ServiceProviderInstallationException(msg);
      }

      ServiceProviderBackend backend = new ServiceProviderBackend(uri,_threadPool,
          _config.getMBeanServer(),
          getTransactionManager(), provider);

      backend.init();
      _serviceProviders.put(uri, backend);
      backend.start();
      
      __log.info(__msgs.msgDomainServiceProviderInstalled(uri));
    } catch (ServiceProviderException spe) {
      throw new ServiceProviderInstallationException(spe.getMessage(),spe);
    } finally {
      _lock.releaseManagementLock();
    } 
  }

  public void initialize(boolean disableAll) throws PxeSystemException {

    _lock.acquireManagementLock();
    try {
      if (_initialized) {
        return;
      }

      _threadPool = Executors.newCachedThreadPool(new DomainThreadFactoryImpl());

      createDomainDirs();
      if (disableAll) {
        disableAllSystems();
      }

      registerManagementObjects();

      _initialized = true;

    } finally {
      _lock.releaseManagementLock();
    }

  }

  public void start() throws PxeSystemException {
    loadSystems();
  }

  public void shutdown() throws PxeSystemException {

    _lock.acquireManagementLock();
    try {
      if (!_initialized || _shutdown) {
        return;
      }
      _shutdown = true;
    } finally {
      _lock.releaseManagementLock();
    }

    // We'd like to do the following blocking operations outside
    // of our management lock, they'll block until all the outstanding
    // activities have expired.
    _threadPool.shutdown();

    // At this point we should have no contention from the workers, we'll
    // reacquire management lock, and complete the shutdown...
    _lock.acquireManagementLock();
    try {
      unregisterManagementObjects();
      HashSet<SystemBackend> systems = new HashSet<SystemBackend>(_systems
          .values());
      for (Iterator<SystemBackend> i = systems.iterator(); i.hasNext();) {
        SystemBackend sb = i.next();
        try {
          _adminMBean.unregisterSystemAdminMBean(sb.getSystemUUID(), sb
              .getName());
        } catch (Exception ex) {
          __log.error(__msgs.msgMBeanRegistrationError("SystemAdminMBean"));
        }
        sb.stop();
        this.unregisterSystemBackend(sb);
      }

      _threadPool.shutdown();
      _threadPool = null;
      _initialized = false;
    } finally {
      _lock.releaseManagementLock();
    }

  }

  private void disableAllSystems() {
    this.runTransaction(new Tx() {
      protected Object run() {
        DomainStateConnection dsc = getDomainStoreConnection();
        Collection<SystemDAO> systems = dsc.findAllSystems();
        for (Iterator<SystemDAO> i = systems.iterator(); i.hasNext();) {
          i.next().setEnabled(false);
        }
        return null;
      }
    });
  }

  /**
   * Get the domain node instance associated with the current thread.
   * 
   * @return {@link DomainNodeImpl} associated with current thread.
   * @throws IllegalStateException
   *           if there is no node associated with the current thread
   */
  public static DomainNodeImpl getActiveDomain() {
    DomainNodeImpl dNode = __ActiveTx.get();

    if (dNode == null) {
      String msg = "getActiveDomain(): No domain associated with current thread "
          + Thread.currentThread();
      __log.debug(msg);
      throw new PxeExceptionImpl(msg, null);
    }

    return dNode;
  }

  /**
   * Get domain id.
   * 
   * @return
   */
  public String getDomainId() {
    return _config.getDomainId();
  }

  /**
   * Get the connection associated with the current thread.
   * 
   * @return connection associated with current thread.
   */
  DomainStateConnection getDomainStoreConnection() {

    final Transaction trans;
    try {
      trans = _config.getTransactionManager().getTransaction();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    DomainStateConnection dconn = _dconns.get(trans);
    if (dconn == null)
      try {
        final DomainStateConnection newconn = dconn = _config
            .getDomainStateConnectionFactory().open(_config.getDomainId());
        _dconns.put(trans, dconn);
        trans.registerSynchronization(new Synchronization() {
          public void afterCompletion(int status) {
            _dconns.remove(trans);
          }

          public void beforeCompletion() {
            try {
              newconn.close();
            } catch (Exception ex) {
              __log.error("Error closing DataStore connection.", ex);
            }
          }
        });

      } catch (IllegalStateException e) {
        throw new RuntimeException(e);
      } catch (RollbackException e) {
        throw new RuntimeException(e);
      } catch (SystemException e) {
        throw new RuntimeException(e);
      }
    return dconn;
  }

  /**
   * Do a scheduled task.
   * 
   * @param task
   *          task to do
   * @throws DomainTaskProcessingException
   */
  void onDomainTask(final DomainTask task) {
    if (__log.isTraceEnabled()) {
      __log.trace("onDomainTask(" + task + ")");
    }

    assert getActiveDomain() == DomainNodeImpl.this;
    assert (task.getDomainId() != null)
        && task.getDomainId().equals(_config.getDomainId());

    _lock.acquireWorkLock();
    try {
      assert _initialized;

      if (task instanceof SystemTask) {
        SystemTask sysEvent = (SystemTask) task;
        SystemUUID systemUUID = sysEvent.getSystemUUID();
        assert systemUUID != null;

        SystemBackend system = _systems.get(systemUUID);

        if (system == null) {
          // System not found, could be that it was undeployed.
          String msg = __msgs.msgSystemMsgUnroutable(task.toString(),
              systemUUID);
          __log.warn(msg);
          return;
        }

        system.onTask(task);
      } else {
        // For now, there are no message of this sort.
        __log.fatal("Received unexpected domain message: " + task);
      }

    } catch (DomainTaskProcessingException e) {
      switch (e.getAction()) {
      case DomainTaskProcessingException.ACTION_COMMIT_AND_CONSUME:
        break;
      case DomainTaskProcessingException.ACTION_ROLLBACK_AND_CONSUME_EVENT:
        throw new RuntimeException(e);
      case DomainTaskProcessingException.ACTION_ROLLBACK_AND_RETRY_LATER:
      case DomainTaskProcessingException.ACTION_ROLLBACK_AND_RETRY_NOW:
        schedule(task);
        break;
      default:
        throw new RuntimeException(e);
      }
    } finally {
      _lock.releaseWorkLock();
    }

  }

  public void runTransientThread(final boolean transacted, final Runnable run) {
    if (__log.isTraceEnabled()) {
      __log.trace("runTransientThread(transacted=" + transacted + ", run="
          + run);
    }

    _lock.acquireWorkLock();
    try {
      if (_initialized && !_shutdown) {

        _threadPool.submit(new Runnable() {
          public void run() {
            assert getActiveDomain() == DomainNodeImpl.this;
            boolean commit = false;
            try {
              if (transacted) {
                getTransactionManager().begin();
              }
            } catch (Exception te) {
              __log.error("Transaction Error", te);
              return;
            }

            try {
              run.run();
              commit = true;
            } finally {
              if (transacted) {
                try {
                  if (commit) {
                    getTransactionManager().commit();
                  } else {
                    getTransactionManager().rollback();
                  }
                } catch (Exception te) {
                  __log.error("Transaction Error", te);
                }
              }
            }

          }
        });
      } else {
        __log.debug("Transient Thread will not be scheduled: " + run);
      }
    } finally {
      _lock.releaseWorkLock();
    }
  }

  public void closeServiceProviderSession(String serviceProviderUri,
      Object sessionId) {
    _lock.acquireWorkLock();
    try {
      ServiceProviderBackend spBackend = _serviceProviders
          .get(serviceProviderUri);

      if (spBackend == null) {
        return;
      }

      spBackend.closeServiceProviderSession((String) sessionId);
    } finally {
      _lock.releaseWorkLock();
    }
  }

  public Object createServiceProviderSession(String serviceProviderUri,
      Class interactionClass) {
    _lock.acquireWorkLock();
    try {
      if (!_initialized || _shutdown) {
        throw new IllegalStateException("DomainNotRunning");
      }

      ServiceProviderBackend spBackend = _serviceProviders
          .get(serviceProviderUri);
      if (spBackend == null) {
        throw new IllegalArgumentException("No service provider with uri '"
            + serviceProviderUri + "'");
      }
      return spBackend.createServiceProviderSession(interactionClass);
    } finally {
      _lock.releaseWorkLock();
    }
  }

  public Object onServiceProviderInvoke(String serviceProviderUri,
      Object sessionId, String name, Object[] args) throws PxeException,
      InvocationTargetException {
    _lock.acquireWorkLock();
    try {
      if (!_initialized || _shutdown) {
        throw new IllegalStateException("DomainNotRunning");
      }

      ServiceProviderBackend spBackend = _serviceProviders
          .get(serviceProviderUri);
      if (spBackend == null) {
        throw new PxeExceptionImpl("Unknown service provider: "
            + serviceProviderUri, null);
      }

      this.associateWithThread();

      try {
        return spBackend.onSessionMethod(sessionId, name, args);
      } finally {
        disassociateFromThread();
      }
    } finally {
      _lock.releaseWorkLock();
    }
  }

  final TransactionManager getTransactionManager() {
    return _config.getTransactionManager();
  }

  /**
   * Schedule a task for future execution.
   * 
   * @param task
   *          task to schedule
   */
  void schedule(final DomainTask task) {
    // TODO: This is no longer reliable, fix it.
    if (__log.isTraceEnabled())
      __log.trace("schedule(" + task + ")");

    _lock.acquireWorkLock();
    try {
      if (!_initialized || _shutdown) {
        throw new IllegalStateException("DomainNodeShutdown");
      }

      task.setDomainId(_config.getDomainId());
      getTransactionManager().getTransaction().registerSynchronization(
          new Synchronization() {

            public void beforeCompletion() {
            }

            public void afterCompletion(int arg0) {
              runTransientThread(true, new Runnable() {
                public void run() {
                  onDomainTask(task);
                }
              });
            }

          });
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      _lock.releaseWorkLock();
    }
  }

  void enable(SystemUUID systemUUID) throws PxeSystemException {
    updateSystemEnabledState(systemUUID, true);
    refresh(systemUUID);
  }

  void disable(SystemUUID systemUUID) throws PxeSystemException {
    updateSystemEnabledState(systemUUID, false);
    refresh(systemUUID);
  }

  public void undeploy(SystemUUID systemUUID) throws PxeSystemException {
    markUndeployed(systemUUID);
    refresh(systemUUID);
  }

  ChannelBackend findChannelBackend(SystemUUID systemUUID, String channelName) {
    SystemBackend system = _systems.get(systemUUID);

    if (system == null) {
      return null;
    }

    ChannelBackend channelBackend = system.findChannel(channelName);

    return channelBackend;
  }

  ServiceProviderBackend findServiceProviderBackend(String serviceProviderId) {
    return _serviceProviders.get(serviceProviderId);
  }

  DomainConfig getConfig() {
    return _config;
  }

  /**
   * Associate the present thread with the domain.
   */
  void associateWithThread() {

    assert __ActiveTx.get() == null;

    __ActiveTx.set(this);

    if (__log.isDebugEnabled()) {
      __log.debug("Associated domain node " + _nodeId + " for domain "
          + _config.getDomainId() + " with thread "
          + Thread.currentThread().getName());
    }
  }

  /**
   * Disassociate the present thread from the current domain.
   */
  void disassociateFromThread() {
    DomainNodeImpl dnode = __ActiveTx.get();
    assert dnode != null;

    __ActiveTx.set(null);

    if (__log.isDebugEnabled()) {
      __log.debug("Disassociated domain node " + _nodeId + " for domain "
          + _config.getDomainId() + " from thread "
          + Thread.currentThread().getName());
    }
  }

  void loadSystems() {
    final List<SystemUUID> systems = new ArrayList<SystemUUID>();

    this.runTransaction(new Tx() {
      @SuppressWarnings("unchecked")
      public Object run() {
        DomainStateConnection conn = getDomainStoreConnection();
        Collection<SystemDAO> allSystems = conn.findAllSystems();
        Set<String> knownNames = new HashSet<String>();
        for (Iterator<SystemDAO> i = allSystems.iterator(); i.hasNext();) {
          SystemDAO sysDao = i.next();
          if (!knownNames.contains(sysDao.getSystemName())) {
            systems.add(SystemUUID.fromIdString(sysDao.getSystemUUID()));
            knownNames.add(sysDao.getSystemName());
          } else {
            sysDao.setDeployed(false);
            String errmsg = "Duplicate system name detected! Correcting.";
            __log.warn(errmsg);
          }
        }
        return null;
      }
    });

    for (Iterator<SystemUUID> i = systems.iterator(); i.hasNext();) {
      try {
        this.refresh(i.next());
      } catch (PxeSystemException e) {
        __log.error("PxeSystemError", e);
      }
    }

  }

  Object runTransaction(Tx tx) {
    Object retval;
    try {
      getTransactionManager().begin();
    } catch (SystemException e) {
      String msg = "!!! TRANSACTION MANAGER FAILURE !!!!";
      __log.fatal(msg);
      throw new PxeExceptionImpl(msg, e);
    } catch (NotSupportedException e) {
      String msg = "!!! TRANSACTION MANAGER FAILURE !!!!";
      __log.fatal(msg);
      throw new PxeExceptionImpl(msg, e);
    }
    boolean success = false;
    try {
      try {
        retval = tx.run();
        success = true;
      } finally {
        if (tx._rollbackOnly || !success)
          try {
            Transaction tx1 = getTransactionManager().getTransaction();
            if (tx1 != null) {
              tx1.rollback();
            }
          } catch (SystemException e) {
            String msg = "!!! TRANSACTION MANAGER ERROR : ROLLBACK FAILED !!!!";
            __log.error(msg, e);
            throw new PxeExceptionImpl(msg, e);
          }
        else {
          try {
            getTransactionManager().commit();
          } catch (HeuristicMixedException e) {
            e.printStackTrace();
          } catch (SystemException e) {
            String msg = "!!! TRANSACTION MANAGER ERROR: COMMIT FAILED!!!!";
            __log.error(msg, e);
            throw new PxeExceptionImpl(msg, e);
          } catch (HeuristicRollbackException e) {
            e.printStackTrace();
          } catch (RollbackException e) {
            e.printStackTrace();
          } catch (Exception e) {
            String msg = "!!! TRANSACTION MANAGER ERROR: COMMIT FAILED!!!!";
            __log.error(msg, e);
            throw new PxeExceptionImpl(msg, e);
          }
        }
      }
    } finally {
      try {
        if (getTransactionManager().getTransaction() != null) {
          getTransactionManager().rollback();
        }
      } catch (Throwable t) {
        __log.fatal("!! TRANSACTION MANAGER FAILURE !!", t);
      }
    }
    return retval;
  }

  /**
   * Synchronize the state of the given system on this node.
   * 
   * @param systemUUID
   *          identifier for the system
   */
  void refresh(final SystemUUID systemUUID) throws PxeSystemException {
    class Holder {
      SystemDescriptor descriptor = null;

      boolean active = false;

      boolean deployed = false;

      String name = null;
    }

    final Holder h = new Holder();
    this.runTransaction(new Tx() {
      public Object run() {
        DomainStateConnection dconn = getDomainStoreConnection();
        SystemDAO systemDao = dconn.findSystem(systemUUID.toString());
        if (systemDao != null) {
          h.descriptor = systemDao.getSystemDescriptor();
          h.active = systemDao.isEnabled();
          h.deployed = systemDao.isDeployed();
          h.name = systemDao.getSystemName();
        }
        return h;
      }
    });

    // This is a bit of a long operation to block on, but it shouldn't
    // happen frequently.
    _lock.acquireManagementLock();
    try {
      if (h.deployed) {
        deployOnNode(systemUUID); // --> DomainNotification
        loadOnNode(systemUUID); // --> DomainNotification
        _adminMBean.registerSystemAdminMBean(systemUUID, h.descriptor); // -->
        // DomainNotification
        if (h.active) {
          activateOnNode(systemUUID); // --> DomainNotification
        }
      } else {
        deactivateOnNode(systemUUID); // --> DomainNotification
        _adminMBean.unregisterSystemAdminMBean(systemUUID, h.name); // -->
        // DomainNotification
        unloadOnNode(systemUUID); // --> DomainNotification
        undeployOnNode(systemUUID); // --> DomainNotification
      }

      // TODO: Make this multi-node aware...
      if (!h.deployed) {
        purgeSystem(systemUUID, h.name); // --> DomainNotification
      }

    } finally {
      _lock.releaseManagementLock();
    }
  }

  /**
   * Purge a system from the database. This should only be used after the system
   * has been succesfully undeployed from <em>all</em> nodes.
   * 
   * @param systemUUID
   */
  private void purgeSystem(final SystemUUID systemUUID, String name) {
    runTransaction(new Tx() {
      public Object run() {
        DomainStateConnection dconn = getDomainStoreConnection();
        SystemDAO systemDao = dconn.findSystem(systemUUID.toString());
        if (systemDao != null) {
          systemDao.delete();
        }
        return null;
      }
    });
    _adminMBean.send(new DomainNotification(DomainNotification.SYSTEM_PURGE,
        _adminMBean.getObjectName(), _adminMBean.nextNotificationSequence(),
        name));
  }

  private void unloadOnNode(SystemUUID systemUUID) {
    _lock.acquireManagementLock();
    try {
      SystemBackend backend = _systems.get(systemUUID);
      String name = systemUUID.toString();
      if (backend != null) {
        name = backend.getName();
        backend.stop();
        unregisterSystemBackend(backend);
      }
      _adminMBean.send(new DomainNotification(DomainNotification.SYSTEM_UNLOAD,
          _adminMBean.getObjectName(), _adminMBean.nextNotificationSequence(),
          name));
    } finally {
      _lock.releaseManagementLock();
    }
  }

  /**
   * Load a system onto the current node.
   * 
   * @param systemUUID
   */
  private void loadOnNode(SystemUUID systemUUID) throws LoadException {
    final File deployDir = getDeployDir(systemUUID);

    _lock.acquireManagementLock();
    try {
      SystemBackend backend = _systems.get(systemUUID);
      if (backend != null) {
        return;
      }

      ExpandedSAR sar;
      try {
        sar = new ExpandedSAR(deployDir);
      } catch (SarFormatException e) {
        String errmsg = __msgs.msgSarFormatException(deployDir.toString(), e
            .getMessage());
        __log.error(errmsg, e);
        return;
      } catch (Exception ex) {
        String errmsg = __msgs.msgSarIoError(deployDir.toString());
        __log.error(errmsg, ex);
        return;
      }

      backend = new SystemBackend(this, systemUUID, sar);
      registerSystemBackend(backend);

      _adminMBean.send(new DomainNotification(DomainNotification.SYSTEM_LOAD,
          _adminMBean.getObjectName(), _adminMBean.nextNotificationSequence(),
          backend.getName()));

    } finally {
      _lock.releaseManagementLock();
    }
  }

  /**
   * Undeploy the system from the current node. This method will try to do this
   * nicely, but if there are problems (such as missing service providers) brute
   * force will be used.
   * 
   * @param systemUUID
   */
  private void undeployOnNode(SystemUUID systemUUID) {
    final File deployDir = getDeployDir(systemUUID);
    final File marker = new File(deployDir, ".deployed");

    _lock.acquireManagementLock();
    try {
      SystemDescriptor descriptor = null;
      String descriptorName = null;
      ExpandedSAR sar = null;

      if (marker.exists()) {
        try {
          sar = new ExpandedSAR(deployDir);
          descriptor = sar.getDescriptor();
        } catch (SarFormatException e) {
          String errmsg = __msgs.msgSarFormatException(deployDir.toString(), e
              .getMessage());
          __log.error(errmsg, e);
        } catch (Exception e) {
          String errmsg = __msgs.msgSarIoError(deployDir.toString());
          __log.error(errmsg, e);
        } finally {
          if (!FileUtils.deepDelete(marker)) {
            __log.warn("Could not delete deployment marker, deleting later: "
                + marker);
            TempFileManager.registerTemporaryFile(marker);
          }
        }
      }

      if (descriptor != null) {
        descriptorName = descriptor.getName();
        Service[] services = descriptor.getServices();
        for (int i = 0; i < services.length; ++i) {
          try {
            ServiceConfigImpl cfg = new ServiceConfigImpl(services[i],
                systemUUID, sar);
            ServiceProviderBackend serviceProvider = findServiceProviderBackend(cfg
                .getSpURI());
            if (serviceProvider == null) {
              __log
                  .error(__msgs.msgServiceProviderNotAvailable(cfg.getSpURI()));
              continue;
            }
            serviceProvider.undeploy(cfg);
          } catch (Exception ex) {
            String errmsg = __msgs.msgServiceConfigurationError(descriptorName,
                services[i].getName());
            __log.error(errmsg, ex);
            continue;
          }
        }

        try {
          sar.close();
          sar = null;
        } catch (IOException ioex) {
          __log.warn("Could not close the SAR at: " + sar.getBaseDir());
          sar = null;
          System.gc(); // Yes, yes, I know..but we can at least try.
        }

        // finally clean up the deployment directory
        if (!FileUtils.deepDelete(deployDir)) {
          __log.warn("Could not delete deployment directory, deleting later: "
              + deployDir);
          TempFileManager.registerTemporaryFile(deployDir);
        }

        _adminMBean.send(new DomainNotification(
            DomainNotification.SYSTEM_UNDEPLOYMENT,
            _adminMBean.getObjectName(),
            _adminMBean.nextNotificationSequence(), descriptorName));

        __log.info(__msgs.msgSystemUndeployed(descriptorName));
      }
    } finally {
      _lock.releaseManagementLock();
    }
  }

  private File deployOnNode(SystemUUID systemUUID) throws DeploymentException {
    final File deployDir = getDeployDir(systemUUID);
    final File marker = new File(deployDir, ".deployed");

    _lock.acquireManagementLock();
    try {
      // If we have the system in memory we are certainly deployed.
      if (_systems.containsKey(systemUUID)) {
        return deployDir;
      }

      // If this system is deployed and we do not have a local
      // deploy directory, we are going to have to make one.
      if (!marker.exists()) {
        if (!extractSARfromDB(deployDir, systemUUID)) {
          FileUtils.deepDelete(deployDir);
          String msg = __msgs.msgSarIoError(deployDir.toString());
          throw new DeploymentException(msg);
        }
      } else {
        // Nothing to do, marker file indicates we've been deployed.
        return deployDir;
      }

      ExpandedSAR sar = null;
      try {
        sar = new ExpandedSAR(deployDir);
      } catch (SarFormatException e) {
        String errmsg = __msgs.msgSarFormatException(deployDir.toString(), e
            .getMessage());
        __log.error(errmsg, e);
        throw new DeploymentException(errmsg, e);
      } catch (Exception e) {
        String errmsg = __msgs.msgSarIoError(deployDir.toString());
        __log.error(errmsg, e);
        throw new DeploymentException(errmsg, e);
      }

      SystemDescriptor descriptor = sar.getDescriptor();

      boolean success = true;
      ServiceConfigImpl svcConfigs[] = new ServiceConfigImpl[descriptor
          .getServices().length];
      int i = 0;
      try {
        for (i = 0; i < descriptor.getServices().length; ++i)
          svcConfigs[i] = new ServiceConfigImpl(descriptor.getServices()[i],
              systemUUID, sar);
      } catch (Exception ex) {
        throw new DeploymentException("", ex);
      }

      Exception caught = null;

      try {
        for (i = 0; i < descriptor.getServices().length; ++i) {
          ServiceProviderBackend serviceProvider = findServiceProviderBackend(svcConfigs[i]
              .getSpURI());

          if (serviceProvider == null) {
            String errmsg = __msgs.msgServiceProviderNotAvailable(svcConfigs[i]
                .getSpURI());
            __log.error(errmsg);
            throw new DeploymentException(errmsg);
          }

          __log.info(__msgs.msgServiceDeploying(svcConfigs[i].getServiceName(),
              serviceProvider.getSpURI()));

          if (!serviceProvider.deploy(svcConfigs[i])) {
            success = false;
            break;
          }
        }

        if (success) {
          marker.createNewFile();
        }
      } catch (Exception ex) {
        success = false;
        caught = ex;
      }

      if (!success) {
        String errmsg = __msgs.msgSystemDeployFailure(svcConfigs[i]
            .getServiceName());
        __log.error(errmsg);
        for (int j = 0; j < i; ++j) {
          ServiceProviderBackend serviceProvider = findServiceProviderBackend(svcConfigs[j]
              .getSpURI());
          if (__log.isInfoEnabled()) {
            __log.info(__msgs.msgServiceUndeploying(svcConfigs[j]
                .getServiceName(), serviceProvider.getSpURI()));
          }
          try {
            serviceProvider.undeploy(svcConfigs[j]);
          } catch (Exception ex1) {
            // Report, but do not abort.
            String errmsg1 = "todo: Error undeploying system (recovery)";
            __log.error(errmsg1, ex1);
          }
        }
        if (caught != null) {
          throw new DeploymentException(errmsg, caught);
        } else {
          throw new DeploymentException(errmsg);
        }
      }
      _adminMBean.send(new DomainNotification(
          DomainNotification.SYSTEM_DEPLOYMENT, _adminMBean.getObjectName(),
          _adminMBean.nextNotificationSequence(), descriptor.getName()));
      __log.info(__msgs.msgSystemDeployed(sar.getDescriptor().getName()));

      return deployDir;
    } finally {
      _lock.releaseManagementLock();
    }
  }

  private File getDeployDir(SystemUUID systemUUID) {
    return new File(_systemsDir, systemUUID.toString());
  }

  private void deactivateOnNode(SystemUUID systemUUID) {
    _lock.acquireManagementLock();
    try {
      SystemBackend backend = _systems.get(systemUUID);
      // If no backend, we are done.
      if (backend == null) {
        return;
      }

      backend.stop();

      _adminMBean.send(new DomainNotification(
          DomainNotification.SYSTEM_DEACTIVATION, _adminMBean.getObjectName(),
          _adminMBean.nextNotificationSequence(), backend.getName()));

    } finally {
      _lock.releaseManagementLock();
    }
  }

  private void activateOnNode(SystemUUID systemUUID) throws LoadException,
      PxeSystemException {

    _lock.acquireManagementLock();

    try {
      // Prerequisite is that we are loaded.
      loadOnNode(systemUUID);
      SystemBackend backend = _systems.get(systemUUID);
      backend.start();

      _adminMBean.send(new DomainNotification(
          DomainNotification.SYSTEM_ACTIVATION, _adminMBean.getObjectName(),
          _adminMBean.nextNotificationSequence(), backend.getName()));

    } finally {
      _lock.releaseManagementLock();
    }
  }

  private boolean extractSARfromDB(final File deployDir,
      final SystemUUID systemUUID) {
    deployDir.mkdirs();
    Object result = runTransaction(new Tx() {
      public Object run() {
        DomainStateConnection dconn = getDomainStoreConnection();
        SystemDAO systemDao = dconn.findSystem(systemUUID.toString());
        try {
          StreamUtils.extractJar(deployDir, systemDao.getSystemArchive());
        } catch (IOException ioex) {
          __log.error(__msgs.msgSarIoError(deployDir.toString()), ioex);
          return ioex;
        }
        return null;
      }
    });
    return result == null;
  }

  SystemUUID deploy(URL url, boolean redeploy) throws PxeSystemException {
    if (url == null) {
      throw new DeploymentException("Deployment URL must not be null!");
    }

    // create local temp copy of archive
    final File sarFile = this.saveSAR(url);

    // unpack archive
    final File sarDir = this.expandSAR(sarFile);

    // required later
    final SystemUUID systemUUID = new SystemUUID();

    try {
      final SystemDescriptor descriptor;
      try {
        ExpandedSAR sar = new ExpandedSAR(sarDir);
        descriptor = sar.getDescriptor();
      } catch (SarFormatException sffe) {
        String msg = __msgs.msgSarFormatException(url.toExternalForm(), sffe
            .getMessage());
        __log.error(msg, sffe);
        throw new DeploymentException(msg, sffe);
      } catch (Exception ex) {
        String msg = __msgs.msgSarIoError(url.toExternalForm());
        __log.error(msg, ex);
        throw new DeploymentException(msg, ex);
      }

      __log.info(__msgs.msgDeployingSystem(descriptor.getName()));

      SystemUUID existing = null;
      _lock.acquireManagementLock();
      try {
        if (_systemsByName.containsKey(descriptor.getName())) {
          existing = _systemsByName.get(descriptor.getName()).getSystemUUID();
        }
      } finally {
        _lock.releaseManagementLock();
      }

      if (existing != null) {
        if (redeploy) {
          undeploy(_systemsByName.get(descriptor.getName()).getSystemUUID());
        } else {
          String msg = __msgs.msgSystemAlreadyDeployedError(descriptor
              .getName());
          __log.error(msg);
          throw new DeploymentException(msg);
        }
      }

      Exception tex;
      final InputStream is;
      try {
        is = new BufferedInputStream(new FileInputStream(sarFile));
      } catch (FileNotFoundException e) {
        String msg = __msgs.msgSarIoError(sarFile.toString());
        __log.error(msg);
        throw new DeploymentException(msg);
      }

      try {
        tex = (Exception) runTransaction(new Tx() {
          public Object run() {
            try {
              DomainStateConnection dsc = getDomainStoreConnection();
              SystemDAO systemDao = dsc.createSystemDeployment(systemUUID
                  .toString(), descriptor.getName());
              systemDao.setSystemArchive(is);
              systemDao.setSystemDescriptor(descriptor);
              systemDao.setDeployed(true);
              return null;
            } catch (Exception ex) {
              setRollbackOnly();
              return ex;
            }
          }
        });
      } finally {
        try {
          is.close();
        } catch (IOException ioex) {
          __log.debug("error closing file tmpsarfile", ioex);
        }
      }

      if (tex != null) {
        String msg = __msgs.msgDataStoreError();
        __log.error(msg);
        throw new DeploymentException(msg, tex);
      }

      boolean localdeploy = false;
      try {
        deployOnNode(systemUUID);
        localdeploy = true;
      } finally {
        // Remove from database if local deployment failed.
        if (!localdeploy) {
          tex = (Exception) runTransaction(new Tx() {
            public Object run() {
              try {
                DomainStateConnection dsc = getDomainStoreConnection();
                SystemDAO systemDao = dsc.findSystem(systemUUID.toString());
                if (systemDao != null) {
                  systemDao.delete();
                }
                return null;
              } catch (Exception ex) {
                setRollbackOnly();
                return ex;
              }
            }
          });
        }
      }
    } finally {
      // try to cleanup temporary SAR file & deployment directory;
      // schedule for later cleanup if that does not work.
      if (!FileUtils.deepDelete(sarFile)) {
        __log.warn("scheduling for later deletion: " + sarFile);
        TempFileManager.registerTemporaryFile(sarFile);
      }
      if (!FileUtils.deepDelete(sarDir)) {
        __log.warn("scheduling for later deletion: " + sarDir);
        TempFileManager.registerTemporaryFile(sarDir);
      }
    }

    refresh(systemUUID);
    return systemUUID;
  }

  boolean isSystemEnabled(final SystemUUID systemUUID) {
    return ((Boolean) runTransaction(new Tx() {
      public Object run() {
        DomainStateConnection dconn = getDomainStoreConnection();
        SystemDAO systemDao = dconn.findSystem(systemUUID.toString());
        return (systemDao != null) && systemDao.isEnabled() ? Boolean.TRUE
            : Boolean.FALSE;
      }
    })).booleanValue();
  }

  /**
   * Set the deployed state of the system in the database.
   */
  private void markUndeployed(final SystemUUID systemUUID)
      throws PxeSystemException {
    Object retVal = runTransaction(new Tx() {
      public Object run() {
        try {
          SystemDAO systemDao = getDomainStoreConnection().findSystem(
              systemUUID.toString());
          if (systemDao == null || !systemDao.isDeployed()) {
            return null;
          }
          systemDao.setDeployed(false);
          return null;
        } catch (Exception ex) {
          setRollbackOnly();
          String msg = __msgs.msgDataStoreError();
          __log.error(msg);
          return new PxeSystemException(msg, ex);
        }
      }
    });

    if (retVal != null && retVal instanceof PxeSystemException) {
      throw (PxeSystemException) retVal;
    } else if (retVal != null) {
      throw new AssertionError(retVal);
    }
  }

  public DomainAdminMBean getDomainAdminMBean() {
    return _adminMBean;
  }

  public String getNodeId() {
    return "node0";
  }

  private void registerManagementObjects() {
    MBeanServer mbeanServer = _config.getMBeanServer();
    if (mbeanServer == null)
      return;

    try {
      _adminMBean.register(mbeanServer);
    } catch (Exception ex) {
      __log.warn(__msgs.msgMBeanRegistrationError("DomainAdmin"), ex);
    }

  }

  private void unregisterManagementObjects() {
    MBeanServer mbeanServer = _config.getMBeanServer();
    if (mbeanServer == null) {
      return;
    }

    try {
      _adminMBean.unregister(mbeanServer);
    } catch (Exception ex) {
      __log.warn(__msgs.msgMBeanRegistrationError("DomainAdmin"), ex);
    }
  }

  /**
   * Create doman directories.
   */
  private void createDomainDirs() throws PxeSystemException {
    _domainDir.mkdirs();
    _systemsDir.mkdirs();
    if (!_systemsDir.exists() || !_systemsDir.isDirectory()) {
      throw new PxeSystemException(
          "Unable to configure domain systems directory: " + _systemsDir);
    }
  }

  /**
   * Set the enabled state of a system in the database.
   * 
   * @param systemUUID
   *          system in question
   * @param enabled
   *          new enabled state
   */
  private void updateSystemEnabledState(final SystemUUID systemUUID,
      final boolean enabled) {
    runTransaction(new Tx() {
      protected Object run() {
        DomainStateConnection dconn = getDomainStoreConnection();
        SystemDAO systemDao = dconn.findSystem(systemUUID.toString());
        if (systemDao != null) {
          systemDao.setEnabled(enabled);
        }
        return null;
      }
    });
  }

  /**
   * Register a system back-end with this domain node.
   * 
   * @param systemBackend
   *          {@link SystemBackend} to register
   */
  private void registerSystemBackend(SystemBackend systemBackend)
      throws LoadException {
    _lock.acquireManagementLock();
    try {
      // Make sure we haven't already deployed a system with the same /name/.
      if (_systemsByName.containsKey(systemBackend.getName())) {
        String msg = __msgs.msgSystemAlreadyDeployedError(systemBackend
            .getName());
        __log.error(msg);
        throw new LoadException(msg);
      }
      _systems.put(systemBackend.getSystemUUID(), systemBackend);
      _systemsByName.put(systemBackend.getName(), systemBackend);
    } finally {
      _lock.releaseManagementLock();
    }
    __log.info(__msgs.msgDomainSystemLoaded(systemBackend.getName(),
        systemBackend.getSystemUUID()));
  }

  /**
   * Create a temporary deployment directory.
   * 
   * @return temporary deployment directory
   * @throws PxeSystemException
   */
  private File expandSAR(File sarfile) throws PxeSystemException {
    File tmpdir = TempFileManager.getTemporaryDirectory("sard");
    InputStream is = null;

    try {
      is = new BufferedInputStream(new FileInputStream(sarfile));
      StreamUtils.extractJar(tmpdir, is);
    } catch (IOException ex) {
      String msg = __msgs.msgSarIoError(sarfile.toString());
      __log.error(msg, ex);
      throw new PxeSystemException(msg, ex);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ioex) {
          // give up
        }
      }
    }

    return tmpdir;
  }

  /**
   * Save a SAR file found at a given URL to a temporary file on the file
   * system.
   * 
   * @param url
   *          SAR URL
   * @return temporary file containing SAR
   * @throws PxeSystemException
   */
  private File saveSAR(URL url) throws PxeSystemException {
    OutputStream os = null;

    try {
      File tmpsarfile = File.createTempFile("sarfile-", ".sar");
      os = new BufferedOutputStream(new FileOutputStream(tmpsarfile));
      StreamUtils.copy(os, url);
      return tmpsarfile;
    } catch (IOException ioex) {
      String msg = __msgs.msgFileSystemError(url.toExternalForm());
      __log.error(msg, ioex);
      throw new PxeSystemException(msg, ioex);
    } finally {
      if (os != null) {
        try {
          os.close();
        } catch (IOException ioex) {
          // give up
        }
      }
    }
  }

  ExecutorService getExecutorService() {
    return _threadPool;
  }
  
  /**
   * Unegister a system back-end with this domain node.
   * 
   * @param systemBackend
   *          {@link SystemBackend} to unregister
   */
  private void unregisterSystemBackend(SystemBackend systemBackend) {
    _lock.acquireManagementLock();
    try {
      _systems.remove(systemBackend.getSystemUUID());
      _systemsByName.remove(systemBackend.getName());
    } finally {
      _lock.releaseManagementLock();
    }
  }

  private class DomainThreadFactoryImpl implements ThreadFactory {

    public Thread newThread(final Runnable r) {
      return new Thread() {
        public void run() {

          associateWithThread();
          try {
            _lock.acquireWorkLock();
            try {
              r.run();
            } finally {
              _lock.releaseWorkLock();
            }
          } finally {
            disassociateFromThread();
          }
        }
      };
    }

  }

}
