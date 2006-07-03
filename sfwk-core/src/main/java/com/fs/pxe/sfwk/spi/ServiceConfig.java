/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import com.fs.pxe.sfwk.rr.ResourceRepository;

import java.io.File;

import javax.wsdl.Definition;

/**
 * Interface exposing information regarding the configuration of a PXE
 * service.
 */
public interface ServiceConfig {
  /**
   * Get the name of a deployment bproperty as configured in the service
   * deployment descriptor
   *
   * @param propertyName name of the property
   *
   * @return value of the property
   */
  String getDeploymentProperty(String propertyName)
          throws PxeException;

  /**
   * Get deployment directory: the directory containing the (exploded) system archive.
   *
   * @return {@link File} pointing to the directory in which the system archive was exploded;
   *         this is a temporary directory, and should not be written to!
   */
  File getDeployDir()
          throws PxeException;

  /**
   * Get the system-level resource repository.
   * @return system-level resource repository
   * @throws PxeException
   */
  ResourceRepository getSystemResourceRepository()
          throws PxeException;

  /**
   * Get the ports exported by this service; these ports will be accessible to
   * other services in the system. The exports are obtained from the
   * deployment descriptor.
   *
   * @return array of exported ports
   */
  ServicePort[] getExports()
          throws PxeException;

  /**
   * Get a specific port exported by this service.
   * @param portName name of the port
   * @return the named port
   * @throws IllegalArgumentException
   */
  ServicePort getExport(String portName)
          throws PxeException;

  /**
   * Get the ports imported by this service; these ports are foreign ports on
   * other services in the system that will be accessible to this service.
   * The imports, like the exports, are obtained from the deployment
   * descriptor.
   *
   * @return array of imported ports
   */
  ServicePort[] getImports()
          throws PxeException;

  /**
   * Get a specific port imported by this service.
   * @param portName name of the port
   * @return the named port
   * @throws IllegalArgumentException
   */
  ServicePort getImport(String portName)
          throws PxeException;

  /**
   * Get the name of the deployed service instance.
   *
   * @return the name of the deployed service instance
   */
  String getServiceName()
          throws PxeException;

  /**
   * Get the name of the system.
   * @return the name of this system
   */
  String getSystemName()
          throws PxeException;

  /**
   * Get the service's unique id.
   *
   * @return the service's unique id
   */
  String getServiceUUID()
          throws PxeException;

  /**
   * Returns the WSDL for the system.
   *
   * @return the WSDL for the system
   */
  Definition getSystemWSDL()
          throws PxeException;

}
