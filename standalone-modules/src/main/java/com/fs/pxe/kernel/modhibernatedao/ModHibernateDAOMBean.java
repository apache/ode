/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modhibernatedao;

import com.fs.pxe.kernel.PxeKernelModMBean;

public interface ModHibernateDAOMBean extends PxeKernelModMBean {

  void setTransactionManager(String transactionManager);

  String getTransactionManager();

  void setStateStoreConnectionFactory(String sscf);

  String getStateStoreConnectionFactory();

  void setBpelStateStoreConnectionFactory(String bpelSscf);

  String getBpelStateStoreConnectionFactory();

  String getHibernateProperties();

  void setHibernateProperties(String hibernateProperties);

  void setDataSource(String dsname);

  String getDataSource();

  void setDialect(String dialect);

  String getDialect();
}
