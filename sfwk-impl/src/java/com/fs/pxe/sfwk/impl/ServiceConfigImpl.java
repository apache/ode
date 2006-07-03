/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.deployment.ExpandedSAR;
import com.fs.pxe.sfwk.deployment.som.Property;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.spi.PxeException;
import com.fs.pxe.sfwk.spi.ServiceConfig;
import com.fs.pxe.sfwk.spi.ServicePort;

import java.io.File;
import java.util.Properties;

import javax.wsdl.Definition;
import javax.wsdl.PortType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the {@link ServiceConfig} interface.
 */
class ServiceConfigImpl implements ServiceConfig {
  private static final Log __log = LogFactory.getLog(ServiceConfigImpl.class);

  protected boolean _valid = true;

  /** Exports, aka out-ports. */
  ServicePort[] _outputPorts;

  /** Inports, aka in-ports. */
  ServicePort[] _inputPorts;

  /** System deployment descriptor (service part). */
  com.fs.pxe.sfwk.deployment.som.Service _sdd;

  Properties _deploymentProperties;
  private String _spURI;
  private ExpandedSAR _sar;
  private SystemUUID _sysUUID;

  ServiceConfigImpl(com.fs.pxe.sfwk.deployment.som.Service dd, SystemUUID sysUUID, ExpandedSAR sar) throws LoadException {
    _sdd = dd;
    _sysUUID = sysUUID;
    _spURI = dd.getProviderUri().toASCIIString();
    _sar = sar;

    createPorts(dd.getExportedPorts(), true);
    createPorts(dd.getImportedPorts(), false);
    createDeploymentProperties();


  }

  public File getDeployDir() throws PxeException {
    return _sar.getBaseDir();
  }

  public ResourceRepository getSystemResourceRepository() {
    return _sar.getSystemResourceRepository();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getDeploymentProperty(String)
   */
  public String getDeploymentProperty(String propertyName) {
    return _deploymentProperties.getProperty(propertyName);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getExports()
   */
  public ServicePort[] getExports() {
    checkValid();

    return _inputPorts;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getImports()
   */
  public ServicePort[] getImports() {
    checkValid();

    return _outputPorts;
  }

  public ServicePort getExport(String portName) {
    for (int i = 0; i < _inputPorts.length; ++i) {
      if (_inputPorts[i].getPortName().equals(portName))
        return _inputPorts[i];
    }

    throw new IllegalArgumentException("No such port: " + portName);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getImport(String)
   */
  public ServicePort getImport(String portName) {
    for (int i = 0; i < _outputPorts.length; ++i) {
      if (_outputPorts[i].getPortName().equals(portName))
        return _outputPorts[i];
    }

    throw new IllegalArgumentException("No such port: " + portName);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getServiceName()
   */
  public String getServiceName() {
    checkValid();

    return _sdd.getName();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getServiceUUID()
   */
  public String getServiceUUID() {
    return _sysUUID.toString() + "-" + getServiceName();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getSystemWSDL()
   */
  public Definition getSystemWSDL() {
    return _sar.getDefinition();
  }


  private void checkValid() {
    if (!_valid) {
      String msg = "Service object was used past its"
                   + " contracted life; check the ServiceProvider implementation! ";
      throw new RuntimeException(msg);
    }
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceContext#getSystemName()
   */
  public String getSystemName() {
    return _sar.getDescriptor().getName();
  }

  /**
   * Extract deployment properties from the deployment descriptor.
   */
  private void createDeploymentProperties() {
    _deploymentProperties = new Properties();

    Property[] sp = _sdd.getProperties();
    for (int i=0; i< sp.length; ++i) {
      _deploymentProperties.put(sp[i].getName(),sp[i].getValue());

      if (__log.isDebugEnabled()) {
        __log.debug("service \"" + _sdd.getName()
                  + "\" deployment property \"" + sp[i].getName() + "\" = \""
                  + sp[i].getValue() + "\".");
      }
    }
    if (sp.length == 0 && __log.isDebugEnabled()) {
      __log.debug("service \"" + _sdd.getName()
                + "\" had no deployment properties set. ");
    }
  }

  /**
   * Create the port objects based on the descriptors.
   *
   * @param portDesc
   * @param export DOCUMENTME
   *
   * @throws LoadException
   */
  private void createPorts(com.fs.pxe.sfwk.deployment.som.Port[] portDesc, boolean export)
                    throws LoadException {
    ServicePortImpl[] ports = new ServicePortImpl[portDesc.length];

    if (export) {
      _inputPorts = ports;
    } else {
      _outputPorts = ports;
    }

    for (int i = 0; i < ports.length; ++i) {
      PortType portType = _sar.getDefinition().getPortType(portDesc[i].getType());

      if (portType == null) {
        String msg = "Could not resolve port type \"" + portDesc[i].getType()
                     + "\" for port \"" + _sdd.getName() + "."
                     + portDesc[i].getName() + "\".";
        __log.error(msg);
        throw new LoadException(msg);
      }


      Properties portProps = new Properties();
      Property[] pp = portDesc[i].getProperties();
      for (int j=0; j < pp.length; ++j) {
        portProps.setProperty(pp[j].getName(), pp[j].getValue());
      }

      ports[i] = new ServicePortImpl(portDesc[i].getName(), portType, portProps);

      // the following assertions enforce semantics (should be detected in the semantics checker)
      assert ports[i].getPortName()
                     .equals(portDesc[i].getName());
      assert ports[i].getPortType()
                     .getQName()
                     .equals(portDesc[i].getType());

    }
  }

  public String getSpURI() {
    return _spURI;
  }
}
