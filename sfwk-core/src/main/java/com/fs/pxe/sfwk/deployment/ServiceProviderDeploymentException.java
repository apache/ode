/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

import com.fs.pxe.sfwk.spi.ServiceProviderException;


/**
 * Thrown when a service provider cannot be deployed due to a previous 
 * <code>ServiceProviderException</code>.
 */
public class ServiceProviderDeploymentException extends DeploymentException {

  /**
   * Constructor.
   * @param spe <code>ServiceProviderException</code> that caused this exception
   */
  public ServiceProviderDeploymentException(ServiceProviderException spe) {
    super();
    initCause(spe);
  }
}
