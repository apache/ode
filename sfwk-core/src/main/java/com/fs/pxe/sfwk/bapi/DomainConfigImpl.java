/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi;

import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;

import java.util.Properties;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;

/**
 * Default implementation of the {@link DomainConfig} interface. Allows
 * for explicit setting of each configuration item.
 */
public class DomainConfigImpl implements DomainConfig {
  private String _domainId;
  private DAOConnectionFactory _domainStateConnectionFactory;
  private final Properties _properties = new Properties();
  private TransactionManager _transactionManager;
  private MBeanServer _mbeanServer;
  private int _threadPoolSize = 20;

  public String getDomainId() {
    return _domainId;
  }

  public DAOConnectionFactory getDomainStateConnectionFactory() {
    return _domainStateConnectionFactory;
  }

  public Properties getProperties() {
    return _properties;
  }

  public String getProperty(String prop) {
    return _properties.getProperty(prop);
  }

  public TransactionManager getTransactionManager() {
    return _transactionManager;
  }

  public void setDomainId(String domainId) {
    _domainId = domainId;
  }

  public void setDomainStateConnectionFactory(DAOConnectionFactory domainStateConnectionFactory) {
    _domainStateConnectionFactory = domainStateConnectionFactory;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    _transactionManager = transactionManager;
  }

  public MBeanServer getMBeanServer() {
    return _mbeanServer;
  }

  public void setMBeanServer(MBeanServer mbeanServer) {
    _mbeanServer = mbeanServer;
  }

  public int getThreadPoolSize() {
    return _threadPoolSize;
  }

  public void setThreadPoolSize(int threadPoolSize) {
    _threadPoolSize = threadPoolSize;
  }
}
