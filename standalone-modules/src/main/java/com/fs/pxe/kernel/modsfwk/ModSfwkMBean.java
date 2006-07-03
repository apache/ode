/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modsfwk;

import com.fs.pxe.kernel.PxeKernelModMBean;

import javax.management.ObjectName;

public interface ModSfwkMBean extends PxeKernelModMBean {
	
	// attributes

  void setDomainId(String domainId);

  String getDomainId();

  void setTransactionManager(String transactionManager);

  String getTransactionManager();

  void setDAOConnectionFactory(String cfName);

  String getDAOConnectionFactory();

  /**
   * only called from kernel configuration to set the same value
   * that was set by the kernel runtime context (perhaps even anouther kernel
   * module which established RMI registry.  Setting this value has not operational
   * affect unless of course some FSWK remote client is bootstrapping their
   * RMI URL based on this MBean property
   */
  void setRegistryPort(int port);

  /**
   * no caller of this API and there really is not use case
   * other than someone looking to see what port the SFWK is on
   */
  int getRegistryPort();
  
  ObjectName getDomainAdminMBean();
  
  void setThreadPoolSize(int size);
  
  int getThreadPoolSize();
  
  boolean getDisableAll();

  void setDisableAll(boolean disableAll);
}
