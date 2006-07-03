/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import java.util.concurrent.ExecutorService;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;

/**
 * Context for a {@link ServiceProvider} implementation.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface ServiceProviderContext {

  String getProviderURI();
  
  /**
   * Get the transaction manager.
   * @return transaction manager
   */
  TransactionManager getTransactionManager();
  
  ExecutorService getExeuctorService();

  /**
   * Get the service-provider logger.
   * @return logger for this service provider
   */
  //Log getLog();

  /**
   * Get the {@link MBeanServer} that should be used for management.
   * @return {@link MBeanServer} or <code>null</code> if no server is active 
   */
  MBeanServer getMBeanServer();

}
