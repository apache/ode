/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.mngmt.ServiceAdminMBean;
import com.fs.utils.jmx.JMXConstants;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Implementation of the {@link SystemAdminMBeanImpl} JMX interface.
 */
class ServiceAdminMBeanImpl extends AdminMBeanImpl implements ServiceAdminMBean {

  private SystemDescriptor _sysdd;
  private Service _sdd;
  private DomainNodeImpl _localNode;

  ServiceAdminMBeanImpl(DomainNodeImpl dnode, SystemDescriptor sysdd, Service svcdd) throws NotCompliantMBeanException {
    super(ServiceAdminMBean.class, dnode.getDomainId());
    _localNode = dnode;
    _sdd = svcdd;
    _sysdd = sysdd;
  }

  public String getName() {
    return _sdd.getName();
  }

  public String getServiceProviderURI() {
    return _sdd.getProviderUri().toASCIIString();
  }

  protected ObjectName createObjectName() {
    return createObjectName(JMXConstants.JMX_DOMAIN, new String [] {
        "domain", _localNode.getDomainId(),
        "node", _localNode.getNodeId(),
        "system", _sysdd.getName(),
        "service", _sdd.getName(),
        "type", "ServiceAdmin"});
  }
}
