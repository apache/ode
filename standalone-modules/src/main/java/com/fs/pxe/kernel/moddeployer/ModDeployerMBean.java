/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.moddeployer;

import com.fs.pxe.kernel.PxeKernelModMBean;

import javax.management.ObjectName;

/**
 * JAR Deployer PXE Kernel Module.
 */
public interface ModDeployerMBean extends PxeKernelModMBean {

  void setDeployDir(String deployDir);

  String getDeployDir();

  void setDomainAdminMBean(ObjectName oname);

  ObjectName getDomainAdminMBean();

}
