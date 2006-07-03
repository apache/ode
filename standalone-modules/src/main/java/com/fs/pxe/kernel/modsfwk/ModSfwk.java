/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modsfwk;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.pxe.sfwk.bapi.DomainConfigImpl;
import com.fs.pxe.sfwk.bapi.DomainNode;
import com.fs.pxe.sfwk.bapi.ServiceProviderInstallationException;
import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;
import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.spi.ServiceProvider;
import com.fs.pxe.sfwk.transports.rmi.RmiTransportServerImpl;
import com.fs.utils.jmx.JMXConstants;

import java.util.ArrayList;
import java.util.Map;

import javax.management.*;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

/**
 * Base class for J2EE-based PXE server.
 */
public class ModSfwk extends ModAbstractSfwkMBean implements ModSfwkMBean {

  private int _rmiPort = -1;
	private String _transactionManager = null;
  private String _stateStoreConnectionFactory = null;
  private String _domainId = "StandaloneDomain";
  private int _threadPoolSize = 20;
  boolean _disableAll = false;

  /** The PXE transaction manager. */
	protected TransactionManager _txManager;

  /** The DOMAIN */
	private DomainNode _domainInstance;

  private RmiTransportServerImpl _rmiTransport;

  public ModSfwk() throws NotCompliantMBeanException {
  	super(ModSfwkMBean.class);
  }

  /**
   * Shutdown PXE.
   */
	public void stop() throws PxeKernelModException {
    try {
      _domainInstance.shutdown();
    } catch (PxeSystemException e) {
      throw new PxeKernelModException(e);
    }
	}

  public String getTransactionManager() {
    return _transactionManager;
  }

  public void setTransactionManager(String transactionManager) {
    _transactionManager = transactionManager;
  }

  /**
   * @see com.fs.pxe.kernel.modsfwk.ModSfwkMBean#getDomainAdminMBean()
   */
  public ObjectName getDomainAdminMBean() {
    try {
			return JMXConstants.createDomainObjectName(_domainId);
		} catch (MalformedObjectNameException e) {
      // this shouldn't happend
			 throw new RuntimeException(e);
		}
  }

  public boolean getDisableAll() {
    return _disableAll;
  }

  public void setDisableAll(boolean disableAll) {
    _disableAll = disableAll;
  }

  public String getDomainId() {
    return _domainId;
  }

  public void setDomainId(String domainId) {
    _domainId = domainId;
  }

  public String getDAOConnectionFactory() {
    return _stateStoreConnectionFactory;
  }

  public int getRegistryPort() {
    return _rmiPort;
  }

  public void setRegistryPort(int port) {
    _rmiPort  = port;
  }

  public void setDAOConnectionFactory(String cfName) {
    _stateStoreConnectionFactory = cfName;
  }

	public void start() throws PxeKernelModException {
    
    _txManager = resolveInJNDI(_transactionManager, TransactionManager.class);
    if (_txManager == null) {
      final String errmsg = __msgs.msgTransactionManagerNotFound(_transactionManager);
      _log.error(errmsg);
      throw new PxeKernelModException(errmsg);
    }


    DAOConnectionFactory dscf = configureDomainStateStore();
    if (dscf == null) {
      final String errmsg = __msgs.msgUnableToConfigureDomainStore();
      _log.error(errmsg);
      throw new PxeKernelModException(errmsg);
    }

    if (!initializeDomainStore(dscf)) {
      final String errmsg = __msgs.msgUnableToInitializeDomainStore();
      _log.error(errmsg);
      throw new PxeKernelModException(errmsg);
    }

    if (_mbeanServer == null) {
      _log.warn(__msgs.msgMBeanServerNotFound());
    }

    DomainConfigImpl domainConfig = new DomainConfigImpl();
    domainConfig.setTransactionManager(_txManager);
    domainConfig.setDomainStateConnectionFactory(dscf);
    domainConfig.setMBeanServer(_mbeanServer);
    domainConfig.setDomainId(_domainId);
    domainConfig.setThreadPoolSize(_threadPoolSize);

    _domainInstance = DomainNode.createDomainNode(domainConfig);

    try {
      _domainInstance.initialize(_disableAll);
    } catch (Exception ex) {
      String msg = __msgs.msgPxeStartError(ex.getMessage());
      _log.error(msg, ex);
      throw new PxeKernelModException(msg, ex);
    }

    registerServiceProviders();


    try {
      _domainInstance.start();
    } catch (Exception ex) {
      String msg = __msgs.msgPxeStartError(ex.getMessage());
      _log.error(msg, ex);
      throw new PxeKernelModException(msg, ex);
    }


    if (_rmiPort > 0) {
      _log.info("Starting RMI transport (server-side).");
      _rmiTransport = new RmiTransportServerImpl(_domainInstance);
      _rmiTransport.setPort(_rmiPort);
      try {
        _rmiTransport.start();
      } catch (Exception ex) {
        _log.error("Unable to start RMI transport (server-side).",ex);
        throw new PxeKernelModException("");
      }
    } else {
      String msg = __msgs.msgRmiTransportNotInitialized();
      _log.warn(msg);
    }
    
    _log.info("Domain '" + _domainId + "' ready.");
  }

  private boolean initializeDomainStore(DAOConnectionFactory dscf) {
    try {
      _txManager.begin();
    } catch (Exception se) {
      _log.error(__msgs.msgTransactionError(se.getMessage()), se);
      return false;
    }

    boolean success = false;
    try {
      // creates a new domain store; will ignore if one already exists
      dscf.createDomainStateStore(_domainId);
      _txManager.commit();
      success = true;
    } catch (Exception e) {
      _log.error(__msgs.msgDomainStateStoreError(), e);
      return false;
    } finally {
      if (!success)
        try {
          _txManager.rollback();
        } catch (Exception ex) {
          _log.error(__msgs.msgTransactionError(ex.getMessage()),ex);
        }
    }
    return success;
  }

  private DAOConnectionFactory configureDomainStateStore() {

    try {
      InitialContext ctx = new InitialContext();
      DAOConnectionFactory cf = (DAOConnectionFactory)ctx.lookup(_stateStoreConnectionFactory);
      ctx.close();
      return cf;
    } catch (Exception dce) {
      _log.error(__msgs.msgDomainStateStoreError(), dce);
      return null;
    }
  }

  protected MBeanServer findMBeanServer() {
    try {
      ArrayList servers = MBeanServerFactory.findMBeanServer(null);
      if (servers.size() != 0) {
        return (MBeanServer) servers.get(0);
      }else{
      	  return MBeanServerFactory.createMBeanServer("PxeMbeanServer");
      }
    } catch (Exception ex) {
      _log.error(__msgs.msgJmxException(), ex);
    }
    return null;
  }

  private void registerServiceProviders() throws PxeKernelModException{
  	if(ModSvcProvider.__svcProviders.isEmpty())
  		_log.warn(__msgs.msgNoServiceProviderBindingsConfigured());

  	for(Map.Entry<String,ServiceProvider> e : ModSvcProvider.__svcProviders.entrySet())
  		registerServiceProvider(e.getKey(), e.getValue());
  }

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSfwkMBean#registerServiceProvider(java.lang.String)
	 */
	public void registerServiceProvider(String uri,ServiceProvider sp) throws PxeKernelModException {
		try {
			_domainInstance.registerServiceProvider(uri,sp);
		} catch (ServiceProviderInstallationException e) {
			String msg = __msgs.msgErrorRegisteringServiceProviderWithDomain();
			_log.error(msg, e);
			throw new PxeKernelModException(msg, e);
		}

	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSfwkMBean#setThreadPoolSize(int)
	 */
	public void setThreadPoolSize(int size) {
		if(size > 0)
			_threadPoolSize = size;
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSfwkMBean#getThreadPoolSize()
	 */
	public int getThreadPoolSize() {
		return _threadPoolSize;
	}

    protected ObjectName createObjectName() {
        return createObjectName(JMXConstants.JMX_DOMAIN, new String[]{"name", getClass().getName()});
    }

}

