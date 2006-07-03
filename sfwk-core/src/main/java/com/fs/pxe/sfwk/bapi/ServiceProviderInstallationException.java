/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi;

import com.fs.pxe.sfwk.core.PxeSystemException;
import com.fs.pxe.sfwk.spi.ServiceProviderException;

/**
 * Thrown when a service provider cannot be registered.
 */
public class ServiceProviderInstallationException extends PxeSystemException {
  public ServiceProviderInstallationException(String msg) {
    super(msg);
  }

  public ServiceProviderInstallationException(String msg,
                                              ServiceProviderException spe) {
    super(msg);
    initCause(spe);
  }
}
