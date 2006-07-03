/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjmxremote;

import com.fs.pxe.kernel.PxeKernelModMBean;

public interface ModJmxRemoteMBean extends PxeKernelModMBean {

  String getJmxURL();

  void setJmxURL(String jmxURL);

}
