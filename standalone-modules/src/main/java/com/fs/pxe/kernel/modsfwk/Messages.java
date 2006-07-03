/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.pxe.kernel.modsfwk;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgConfigurationError(String configFileURI) {
    return this.format("PXE configuration error in \"{0}\".", configFileURI);
  }

  public String msgDomainStateStoreError() {
    return this.format("Domain State Store error.");
  }

  public String msgErrorRegisteringServiceProviderWithDomain() {
    return this.format("Error registering service provider with domain.");
  }

  public String msgFailedToBindServiceProvider(String jndiName) {
    return this.format("Error binding service provider to jndi: \"{0}\".", jndiName);
  }

  public String msgInitialContextError() {
    return this.format("Unable to obtain the initial JNDI context.");
  }

  public String msgJmxException() {
    return this.format("JMX error.");
  }

  public String msgJndiLookupError(String objname) {
    return this.format("Unable to resolve name \"{0}\" in JNDI.", objname);
  }

  public String msgMBeanServerNotFound() {
    return this.format("JMX MBeanServer not found!");
  }

  public String msgNoServiceProviderBindingsConfigured() {
    return this.format("No service provider bindings are configured for the domain.");
  }

  public String msgPxeEventListenerClassNotFound(String elClassName) {
    return this.format("The PXE Event Listener class \"{0}\" was not found!", elClassName);
  }

  public String msgPxeEventListenerInaccessible(String elClassName) {
    return this.format("The PXE Event Listener class \"{0}\" is not accessible;"
        + "check class-loading environment!", elClassName);
  }

  public String msgPxeEventListenerInstantiationErr(String elClassName) {
    return this.format("The PXE Event Listener class \"{0}\" could not be instantiated!",
        elClassName);
  }

  public String msgPxePropertiesLoadError(String resourcename) {
    return this.format("The PXE configuration resource \"{0}\" could not be loaded.",
        resourcename);
  }

  public String msgPxePropertiesNotFound(String resourcename) {
    return this.format("The PXE configuration resource \"{0}\" was not found!", resourcename);
  }

  public String msgPxeServiceProviderClassNotFound(String spClassName) {
    return this.format("The PXE Service Provider class \"{0}\" was not found!", spClassName);
  }

  public String msgPxeStartError(String cause) {
    return this.format("Error starting PXE: {0}", cause);
  }

  public String msgPxeWarAlreadyDeployed() {
    return this.format("The PXE WAR file is already deployed!");
  }

  public String msgRmiTransportNotInitialized() {
    return this.format("RMI Transport was not registered; the \'registryPort\'"
        + " property was not set.");
  }

  public String msgSchedulerLoadError() {
    return this.format("Unable to load scheduler.");
  }

  public String msgServiceProviderInitializationException(String providerClass, String message) {
    return this.format("Error initializing service provider \"{0}\"; reason: \"{1}\"",
        providerClass, message);
  }

  public String msgServiceProviderInstantiationException(String providerClass) {
    return this.format("Error instantiating service provider \"{0}\".", providerClass);
  }

  public String msgServiceProviderConfigError(String providerClass, String message) {
    return this.format("Error configuring service provider \"{0}\"; reason: \"{1}\"",
        providerClass, message);
  }

  public String msgStartedServiceProvider(String providerClass, String jndiName) {
    return this.format("Started service provider \"{0}\"; bound to \"{1}\"", providerClass,
        jndiName);
  }

  public String msgTransactionError(String cause) {
    return this.format("A JTA transaction error was encountered: {0}", cause);
  }

  public String msgTransactionManagerNotFound(String txJndiName) {
    return this.format(
        "Unable to find JTA TransactionManager object at JNDI location \"{0}\".", txJndiName);
  }

  public String msgUnableToConfigureDomainStore() {
    return this.format("Error configuring Domain State Store.");
  }

  public String msgUnableToInitializeDomainStore() {
    return this.format("Unable to initialize Domain State Store.");
  }

  public String msgWrongObjectInJndi(String objName, String expectedClassName) {
    return this.format("The JNDI object \"{0}\" is not of the expected type \"{1}\".",
        objName, expectedClassName);
  }

}
