/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.sfwk.impl;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgChannelReferenceNotFound(String channelRef, String portName) {
    return this.format("The channel \"{0}\" referenced from port \"{1}\" is not declared.",
        channelRef, portName);
  }

  public String msgDataStoreError() {
    return this.format("Data-store error.");
  }

  public String msgDeployingSystem(String systemname) {
    return this.format("Deploying system \"{0}\".", systemname);
  }

  public String msgDeployingSystemArchiveFailed(String url) {
    return this.format("System archive at {0} could not be deployed.", url);
  }

  public String msgDomainServiceProviderInstalled(String providerURI) {
    return this.format("Service provider \"{0}\" installed.", providerURI);
  }

  public String msgDomainSystemLoaded(String systemName, SystemUUID uuid) {
    return this.format("System \"{0}\" loaded.", systemName);
  }

  public String msgFileSystemError(String msg) {
    return this.format("File system error: {0}", msg);
  }

  public String msgInternalJMXErrorChannelOName(String dom, String serv, String chan,
      String message) {
    return this.format(
        "Unable to create ObjectName for [domain={0},service={1},channel={2}]: {3}", dom,
        serv, chan, message);
  }

  public String msgJtaSystemException(String jtaErr) {
    return this.format("The JTA transaction manager experienced an error: {0}.", jtaErr);
  }

  public String msgMBeanRegistrationError(String name) {
    return this.format("Unable to register MBean \"{0}\" in JMX.", name);
  }

  public String msgSarFormatException(String path, String message) {
    return this.format("Archive format error reading {0}: {1}", path, message);
  }

  public String msgSarIoError(String path) {
    return this.format("I/O error processing SAR at {0}.", path);
  }

  public String msgServiceConfigurationError(String system, String service) {
    return this.format("Configuration error for service \"{1}\" in system \"{0}\".", system,
        service);
  }

  public String msgServiceDeploying(String serviceName, String spURI) {
    return this.format("Deploying service \"{0}\" on \"{1}\".", serviceName, spURI);
  }

  public String msgServiceProviderActivateService(String serviceName) {
    return this.format("Activating service \"{0}\".", serviceName);
  }

  public String msgServiceProviderActivateServiceError(String serviceName) {
    return this.format("Service \"{0}\" could not be activated!", serviceName);
  }

  public String msgServiceProviderContractViolation(String providerClass, String providerURI,
      String method) {
    return this.format("The service provider class \"{0}\" identified by URI \"{1}\" "
        + "violated the container contract in method \"{2}\".", providerClass, providerURI,
        method);
  }

  public String msgServiceProviderDeactivateService(String serviceName) {
    return this.format("Deactivating service \"{0}\".", serviceName);
  }

  public String msgServiceProviderDeactivateServiceError(String serviceName) {
    return this.format("Service \"{0}\" could not be deactivated!", serviceName);
  }

  public String msgServiceProviderDeployService(String serviceName) {
    return this.format("Deploying service \"{0}\".", serviceName);
  }

  public String msgServiceProviderDeployServiceError(String serviceName) {
    return this.format("Service \"{0}\" could not be deployed!", serviceName);
  }

  public String msgServiceProviderNotAvailable(String spURI) {
    return this.format("System provider \"{0}\" not available.", spURI);
  }

  public String msgServiceProviderSessionClosed(Object sessionId) {
    return this.format(
        "The service provider session (id=\"{0}\") has been closed and cannot be used.",
        sessionId);
  }

  public String msgServiceProviderUndeployServiceError(String name) {
    return this.format("Service \"{0}\" could not be undeployed!", name);
  }

  public String msgServiceUndeploying(String serviceName, String spURI) {
    return this.format("Undeploying service \"{0}\" from \"{1}\".", serviceName, spURI);
  }

  public String msgSessionConnectionClosed() {
    return this.format("The PXE connection is closed.");
  }

  public String msgSessionUnexpectedException(String className, String serviceProviderUri,
      String exClass) {
    return this.format("The Service Provider implementation \"{0}\""
        + "(configuration URI \"{1}\") threw an unexpected exception (\"{2}\")"
        + "from one of it's interaction handlers. "
        + "This indicates a problem in the Service Provider implementation.", className,
        serviceProviderUri, exClass);
  }

  public String msgSysProviderRegCfgDuplicatedId(String providerId) {
    return this.format("System Provider Configuration URI \"{0}\" is not unique.", providerId);
  }

  public String msgSystemActivationFailed(String systemName) {
    return this.format("Error activating system \"{0}\".", systemName);
  }

  public String msgSystemAlreadyDeployedError(String systemName) {
    return this.format("System \"{0}\" already deployed!", systemName);
  }

  public String msgSystemBeginningRecovery(String systemName) {
    return this.format("Recovering system \"{0}\".", systemName);
  }

  public String msgSystemDeployed(String systemName) {
    return this.format("System \"{0}\" deployed.", systemName);
  }

  public String msgSystemDeployFailure(String systemName) {
    return this.format("System \"{0}\" could not be deployed!", systemName);
  }

  // TODO better error message
  public String msgSystemMsgExRecoveryFailure(String systemName, String serviceProviderId,
      String mexId) {
    return this.format("msgSystemMsgExRecoveryFailure {0} {1} {2}");
  }

  public String msgSystemMsgUnroutable(String systemMsgString, SystemUUID systemUUID) {
    return this.format("Received message for unknown system \"{1}\";"
        + " message will be dropped! Message: {0}", systemMsgString, systemUUID);
  }

  public String msgSystemUndeployed(String name) {
    return this.format("System \"{0}\" undeployed.", name);
  }

}
