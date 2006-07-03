/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.msg.MessageBundle;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backend representation of a Service Provider. Ensures that the {@link ServiceProvider}
 * contract is enforced, particularly with respect to threading issues. This is the class
 * that provides the {@link ServiceProviderContext} as well as the Service Provider's view
 * of transaction management facilities.
 */
class ServiceProviderBackend {
  private static final Log __log = LogFactory.getLog(ServiceProviderBackend.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /** Service provider (class implemented by the actual provider) */
  private final ServiceProvider _serviceProvider;
  
  /** Bound services. */
  private final HashSet<String> _boundServices = new HashSet<String>();

  /** Active client sessions. */
  private final HashMap<Object,SpSessionBackend>  _sessions = new HashMap<Object,SpSessionBackend>();

  private ManagementLock _lock = new ManagementLock();

  private Log _spLog = LogFactory.getLog(ServiceProviderBackend.class);

  private TransactionManager _txm;
  
  private ServiceProviderContext _context;
  
  private String _uri;
  
  private ExecutorService _es;
  
  private MBeanServer _mbs;
   
  ServiceProviderBackend(String uri,
      ExecutorService es,
      MBeanServer mbs,
      TransactionManager txm, ServiceProvider serviceProvider) {
    _uri = uri;
    _es = es;
    _mbs = mbs;
  	_txm = txm;
  	_serviceProvider = serviceProvider;
    _spLog = LogFactory.getLog(_serviceProvider.getClass());
    _context = new ServiceProviderContextImpl();
  }

  boolean start(ServiceBackend svcBackend) {
    __log.info(__msgs.msgServiceProviderActivateService(svcBackend.getServiceName()));
    boolean success = false;
    _lock.acquireManagementLock();
    try {
      _serviceProvider.activateService(svcBackend.getService());
      _boundServices.add(svcBackend.getService().getServiceUUID());
      success = true;
    } catch (ServiceProviderException spe) {
      String msg = __msgs.msgServiceProviderActivateServiceError(svcBackend.getServiceName());
      __log.error(msg, spe);
    } catch (Throwable t) {
      String msg = __msgs.msgServiceProviderContractViolation(_serviceProvider.getClass().getName(), svcBackend.getServiceProviderId(), "activateService");
      __log.error(msg, t);
    } finally {
      _lock.releaseManagementLock();
    }

    return success;
  }

  boolean stop(ServiceBackend svcBackend) {
    __log.info(__msgs.msgServiceProviderDeactivateService(svcBackend.getServiceName()));
    boolean success = false;

    _lock.acquireManagementLock();
    try {
      closeAllSessions();
      _serviceProvider.deactivateService(svcBackend.getService());
      _boundServices.remove(svcBackend.getService().getServiceUUID());
      success = true;
    } catch (ServiceProviderException spe) {
      String msg = __msgs.msgServiceProviderDeactivateServiceError(svcBackend.getServiceName());
      __log.error(msg, spe);
    } catch (Throwable t) {
      String msg = __msgs.msgServiceProviderContractViolation(_serviceProvider.getClass().getName(), svcBackend.getServiceProviderId(), "deactivateService");
      __log.error(msg, t);
    } finally {
      _lock.releaseManagementLock();
    }
    return success;
  }


  boolean deploy(ServiceConfigImpl serviceConfig) {
    __log.info(__msgs.msgServiceProviderDeployService(serviceConfig.getServiceName()));
    boolean success = false;

    _lock.acquireManagementLock();
    try {
      _serviceProvider.deployService(serviceConfig);
      success = true;
    } catch (ServiceProviderException spe) {
      String msg = __msgs.msgServiceProviderDeployServiceError(serviceConfig.getServiceName());
      __log.error(msg, spe);
    } catch (Throwable t) {
      String msg = __msgs.msgServiceProviderContractViolation(_serviceProvider.getClass().getName(), getSpURI(), "deployService");
      __log.error(msg, t);
    } finally {
      _lock.releaseManagementLock();
    }
      return success;
  }

  boolean undeploy(ServiceConfigImpl svcConfig) {
    boolean success = false;

    _lock.acquireManagementLock();
    try {
      closeAllSessions();
      _serviceProvider.undeployService(svcConfig);
      success = true;
    } catch (ServiceProviderException spe) {
      String msg = __msgs.msgServiceProviderUndeployServiceError(svcConfig.getServiceName());
      __log.error(msg, spe);
    } catch (Throwable t) {
      String msg = __msgs.msgServiceProviderContractViolation(_serviceProvider.getClass().getName(), getSpURI(), "deployService");
      __log.error(msg, t);
    } finally {
      _lock.releaseManagementLock();
    }

    return success;
  }

  void onServiceEvent(ServiceEventImpl svcEvent)
          throws ServiceProviderException, MessageExchangeException
  {
    _lock.acquireWorkLock();
    try {
      _serviceProvider.onServiceEvent(svcEvent);
    } finally {
      _lock.releaseWorkLock();
    }
  }

  Object onSessionMethod(Object sessionId, String name, Object[] args)
          throws PxeException, InvocationTargetException {
    // close() needs to be handled through closeServiceProviderSession
    assert !name.equals("close");

    _lock.acquireWorkLock();
    try {
      SpSessionBackend session = _sessions.get(sessionId);
      if (session == null) {
        throw new PxeExceptionImpl(__msgs.msgServiceProviderSessionClosed(sessionId),null);
      }
      return session.invoke(name, args);
    } finally {
      _lock.releaseWorkLock();
    }
  }


  String createServiceProviderSession(Class interactionClass) {
    _lock.acquireWorkLock();
    Object listener;
    try {
      listener = _serviceProvider.createInteractionHandler(interactionClass);
      SpSessionBackend session = new SpSessionBackend(this, _txm, listener, interactionClass);
      _sessions.put(session.getId(), session);
      return session.getId();
    } catch (ServiceProviderException spe) {
      throw new IllegalArgumentException(spe.getMessage());
    } finally {
      _lock.releaseWorkLock();
    }
  }

  void closeServiceProviderSession(String sessionId) {
    _lock.acquireWorkLock();
    try {
      SpSessionBackend session = _sessions.remove(sessionId);
      if (session != null) {
        session.close();
      }
    } finally {
      _lock.releaseWorkLock();
    }
  }


  private void closeAllSessions() {
    for (Iterator<SpSessionBackend> i = _sessions.values().iterator(); i.hasNext(); ) {
      i.next().close();
    }
    _sessions.clear();
  }

  String getSpName() {
    return ObjectPrinter.getShortClassName(_serviceProvider);
  }

  String getSpURI() {
    return _serviceProvider.getProviderURI();
  }

  Log getSpLog() {
    return _spLog;
  }

  public Class getSpClass() {
    return _serviceProvider.getClass();
  }

  public void init() throws ServiceProviderException {
    _serviceProvider.initialize(_context);
  }

  public void start() throws ServiceProviderException {
    _serviceProvider.start();
  }

  private class ServiceProviderContextImpl implements ServiceProviderContext {

    
    public String getProviderURI() {
      return _uri;
    }

    public TransactionManager getTransactionManager() {
      return _txm;
    }

    public ExecutorService getExeuctorService() {
      return _es;
    }

    public MBeanServer getMBeanServer() {
      return _mbs;
    }
  }


}
