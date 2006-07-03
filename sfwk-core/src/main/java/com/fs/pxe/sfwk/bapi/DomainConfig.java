/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi;

import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;

/**
 * Domain configuration.
 */
public interface DomainConfig {

  /**
   * Gets the unique domain identifier.
   *
   * @return domain identifier
   */
  String getDomainId();

  /**
   * Get the {@link javax.transaction.TransactionManager} implementation
   * appropriate for this domain.
   *
   * @return{@link TransactionManager} implementation
   */
  TransactionManager getTransactionManager();

  /**
   * Get the domain state store connection factory.
   *
   * @return domain state store connection factory
   */
  DAOConnectionFactory getDomainStateConnectionFactory();

//  /*
//   * Get all service provider configurations.
//   *
//   * @return all service provider configurations
//   */
//  ServiceProviderConfig[] getProviders();

  /**
   * Get the MBean server for this domain.
   * @return {@link MBeanServer} or <code>null</code> if none is available. 
   */
  MBeanServer getMBeanServer();

  /**
   * Get the requested thread-pool size.
   * @return requested size of domain thread pool
   */
  int getThreadPoolSize();

}
