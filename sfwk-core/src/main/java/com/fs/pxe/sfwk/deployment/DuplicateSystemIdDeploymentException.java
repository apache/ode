/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

/**
 * Thrown when a service provider cannot be deployed due to a duplicate system ID.
 */
public class DuplicateSystemIdDeploymentException extends DeploymentException {
  /**
   * Constructor.
   * @param systemId the duplicate system ID
   */
  public DuplicateSystemIdDeploymentException(String systemId) {
    super("System id: '" + systemId + "'");
  }
}
