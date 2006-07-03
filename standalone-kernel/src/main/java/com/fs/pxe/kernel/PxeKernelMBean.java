/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel;

/**
 * Management interface for the PXE kernel.
 */
public interface PxeKernelMBean {

  String getConfigURL();
  
  //void setConfigURL(String configURL);

  void start() throws PxeKernelModException;

  void stop();

  void shutdown();
  
}
