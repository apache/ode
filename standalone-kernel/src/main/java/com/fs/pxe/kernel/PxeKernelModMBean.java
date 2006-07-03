/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel;

/**
 * Management interface implemented by PXE kernel modules.
 */
public interface PxeKernelModMBean {

  /** Start the kernel mod. */
  public void start() throws PxeKernelModException;

  /** Stop the kernel mod. */
  public void stop() throws PxeKernelModException;

}
