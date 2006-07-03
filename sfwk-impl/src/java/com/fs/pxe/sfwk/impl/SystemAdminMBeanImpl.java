/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.utils.jmx.JMXConstants;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
class SystemAdminMBeanImpl extends AdminMBeanImpl implements SystemAdminMBean {
  private static final Log __log = LogFactory.getLog(SystemAdminMBean.class);

  /** Locally-accessible domain node. */
  private DomainNodeImpl _localNode;
  private SystemUUID _systemUUID;
  private SystemDescriptor _systemDescriptor;
  private ObjectName[] _serviceAdminMBeans;

  SystemAdminMBeanImpl(DomainNodeImpl domainNode, SystemUUID systemUUID, SystemDescriptor desc) throws NotCompliantMBeanException {
    super(SystemAdminMBean.class, domainNode.getDomainId());
    _localNode = domainNode;
    _systemUUID = systemUUID;
    _systemDescriptor = desc;
  }

	/**
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		for (int i = 0 ; i < _serviceAdminMBeans.length ; ++i) {
      if (_serviceAdminMBeans[i] != null)
        try {
          _mbeanServer.unregisterMBean(_serviceAdminMBeans[i]);
        } catch (Exception ex) {
          __log.warn("Failed to unregister MBean", ex);

        }
    }
    super.preDeregister();
	}
  
  public void postRegister(Boolean aBoolean) {
    super.postRegister(aBoolean);
    if (aBoolean.booleanValue()) {
      _serviceAdminMBeans = new ObjectName[_systemDescriptor.getServices().length];
      for (int i = 0; i < _serviceAdminMBeans.length; ++i) {
        Service sdd = _systemDescriptor.getServices()[i];
        try {
          ServiceAdminMBeanImpl s =new ServiceAdminMBeanImpl(_localNode, _systemDescriptor, sdd);
          _serviceAdminMBeans[i] = s.register(_mbeanServer);
        } catch (Exception ex) {
          __log.warn("Failed to create MBean" ,ex);
        }
      }
    }
  }
  public void disable() {
    try {
      _localNode.disable(_systemUUID);
    } catch (PxeSystemException pse) {
      throw new RuntimeException(pse);
    }
  }

  public void enable() {
    try {
      _localNode.enable(_systemUUID);
    } catch (PxeSystemException pse) {
      throw new RuntimeException(pse);
    }
  }

  public void undeploy() {
    try {
      _localNode.undeploy(_systemUUID);
    } catch (PxeSystemException pse) {
      throw new RuntimeException(pse);
    }
  }
  public String getDeploymentDescriptor() {
    // TODO: does toString work?
    return _systemDescriptor.toString();
  }

  public String getName() {
    return _systemDescriptor.getName();
  }

  public boolean isEnabled() {
    return _localNode.isSystemEnabled(_systemUUID);
  }

  public ObjectName[] getServices() {
    return _serviceAdminMBeans;
  }

  protected ObjectName createObjectName() {
    return createObjectName(JMXConstants.JMX_DOMAIN, new String [] {
        "domain", _localNode.getDomainId(),
        "node", _localNode.getNodeId(),
        "system", _systemDescriptor.getName(),
        "type", "SystemAdmin"});
  }
}
