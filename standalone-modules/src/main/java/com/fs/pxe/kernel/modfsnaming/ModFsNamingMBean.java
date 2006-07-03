/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modfsnaming;

import com.fs.pxe.kernel.PxeKernelModMBean;

/**
 * Management interface for the FiveSight Naming PXE Kernel Module.
 */
public interface ModFsNamingMBean extends PxeKernelModMBean {

  void setProviderURL(String providerURL);

  String getProviderURL();

}
