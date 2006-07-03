/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjotm;

import com.fs.pxe.kernel.PxeKernelModMBean;

/**
 * Management interface for the JOTM MBean.
 */
public interface ModJOTMMBean extends PxeKernelModMBean {

  String getTransactionManagerName();

  void setTransactionManagerName(String txManagerName);

  String getUserTransactionManagerName();

  void setUserTransactionManagerName(String utxManagerName);

  void setTxTimeout(int txTimeout);

  int getTxTimeout();

}
