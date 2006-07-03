/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjdbc;

import com.fs.pxe.kernel.PxeKernelModMBean;

/**
 * Management interface for JDBC DataSource PXE Kernel Mod.
 */
public interface ModJdbcDSMBean extends PxeKernelModMBean {
  
  String getType();
  
  void setType(String type);
  
  String getDataSourceName();

  void setDataSourceName(String dataSourceName);

  String getDriver();

  void setDriver(String driver);

  String getPassword();

  void setPassword(String password);

  int getPoolMax();

  void setPoolMax(int poolMax);

  int getPoolMin();

  void setPoolMin(int poolMin);

  String getTransactionManagerName();

  void setTransactionManagerName(String transactionManagerName);

  String getUrl();

  void setUrl(String url);

  String getUsername();

  void setUsername(String username);
}
