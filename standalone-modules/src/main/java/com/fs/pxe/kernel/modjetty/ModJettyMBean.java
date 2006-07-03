/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjetty;

import com.fs.pxe.kernel.PxeKernelModMBean;

public interface ModJettyMBean extends PxeKernelModMBean {

  void setConfig(String configURL);

  String getConfig();
}
