/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modrmi;

import com.fs.pxe.kernel.PxeKernelModMBean;

/**
 * Management interface for the FiveSight Naming PXE Kernel Module.
 */
public interface ModRmiRegistryMBean extends PxeKernelModMBean {

  void setPort(int port);

  int getPort();

}
