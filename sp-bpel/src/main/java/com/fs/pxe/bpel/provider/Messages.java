/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.provider;

import com.fs.utils.msg.MessageBundle;

/**
 * Message bundle used by the BPEL provider implementation.
 */
public class Messages extends MessageBundle {

  /**
   * Unable to load BPEL process with URI {0}
   */
  String msgBarProcessLoadErr(String bpelProcessUri) {
    return format("Unable to load BPEL process with URI {0}",bpelProcessUri);
  }

  /**
   * Unable to load process deployment descriptor with URI {0}
   */
  String msgUnableToLoadDeploymentDescriptor(String bpelProcessUri) {
    return format("Unable to load process deployment descriptor with URI {0}", bpelProcessUri);

  }

  /**
   * Unable to locate class for BPEL event listener {0}
   */
  String msgBpelEventListenerClassNotFound(String className) {
    return format("Unable to locate class for BPEL event listener {0}",className);
  }

  /**
   * Unable to instantiate BPEL event listener {0}: {1}
   */
  String msgBpelEventListenerInstantiationEx(String className, String reason) {
    return format("Unable to instantiate BPEL event listener {0}: {1}", className,reason);
  }

  /**
   * The Bpel event listener class {0} does not appear to have a
   *          default constructor.
   */
  String msgBpelEventListenerMissingPublicCtor(String className) {
    return format("The BPEL event listener class {0} does not appear to have a default " +
        "constructor.",className);
  }

  /**
   * Registered BPEL event listener {0}.
   */
  String msgBpelEventListenerRegistered(String className) {
    return format("Registered BPEL event listener {0}.",className);
  }

  /**
   * Unable to locate state store factory "{0}".
   */
  String msgBpelStateStoreFactoryNotFound(String stateStoreFactory) {
    return format("Unable to locate state store factory \"{0}\".", stateStoreFactory);
  }

  /**
   * Unable to instantiate state story factory class {0}
   */
  String msgBpelStateStoreFactoryInstantiationErr(String stateStoreFactory) {
    return format("Unable to instantiate state story factory class {0}",stateStoreFactory);
  }

  /**
   * The state store configuration property is required.
   */
  String msgBpelStateStorePropertyNotSet() {
    return format("The state store configuration property is required.");
  }

  /**
   * Data access error.
   */
  String msgDataAccessError() {
    return format("Data access error.");
  }

  /**
   * Error parsing BPEL definition.
   */
  String msgErrParsingBpelDefinition() {
    return format("Error parsing BPEL definition.");
  }

  /**
   * Unable to create BPEL processor with service name {0}: {1}
   */
  String msgProcessorInstantiationError(String serviceName, String reason) {
    return format("Unable to create BPEL processor with service name {0}: {1}", serviceName, reason);
  }

  /**
   * The property {0} is required.
   */
  String msgRequiredPropertyNotSet(String propertyName) {
    return format("The property {0} is required.", propertyName);
  }

  /**
   * The service {0} is not active.
   */
  String msgServiceNotActive(String serviceName) {
    return format("The service {0} is not active.", serviceName);
  }

  /**
   * State store created for service [provider={0}, service={1},
   *          uuid={2}]
   */
  String msgStateStoreCreated(String providerURI, String serviceName,
      String serviceUUID) {
    return format("State store created for service [provider={0}, service={1}, uuid={2}]",
        providerURI,serviceName,serviceUUID);
  }

  /**
   * Using in-memory state store.
   */
  String msgUsingInMemoryStateStore() {
    return format("Using in-memory state store.");
  }

  /**
   * No such message exchange {1} for process {0}.
   */
  String msgNoSuchExchange(String procName, String instanceId) {
    return format("No such message exchange {1} for process {0}.", procName, instanceId);
  }

  /**
   * The service {0} defines but does not use the exports: {1}
   */
  String msgServiceDefinesUnusedExports(String procName,
      String listOfUnusedExports) {
    return format("The service {0} defines but does not use the exports: {1}", procName,listOfUnusedExports);
  }

  /**
   * The BPEL service {0} defines but does not use the imports: {1}
   */
  String msgServiceDefinesUnusedImports(String procName,
      String listOfUnusedImports) {
    return format("The BPEL service {0} defines but does not use the imports: {1}", procName,listOfUnusedImports);
  }

  /**
   * The BPEL service {0} does not export the partnerlink {1} with role
   *          {2} on port {3}. Check you system configuration.
   */
  String msgServiceDoesNotExportPortRequiredByPartnerLinkRole(String procName,
      String name, String role, String portName) {
    return format("The BPEL service {0} does not export the partnerlink {1} with role" +
        " {2} on port {3}. Check you system configuration.",procName, name, role, portName);
  }

  /**
   * The BPEL service {0} does not import the partnerlink {1} with role
   *          {2} on port {3}. Check you system configuration.
   */
  String msgServiceDoesNotImportPortRequiredByPartnerLinkRole(String procName,
      String name, String role, String portName) {
    return format("The BPEL service {0} does not import the partnerlink {1} with role {2} " +
        "on port {3}. Check you system configuration.",procName,name,role,portName);
  }

  /**
   * The BPEL service {0} with partnerlink {1} and role {2} does not
   *          match the portType for port {3}. Check you system configuration.
   */
  String msgServiceExportsWrongPortTypeForPartnerLinkRole(String procName,
      String name, String myRole, String portName) {
    return format("The BPEL service {0} with partnerlink {1} and role {2} does not " +
        "match the portType for port {3}. Check you system configuration.",
        procName,name,myRole,portName);
  }

  /**
   * The BPEL service {0} with partnerlink {1} and role {2} does not
   *          match the portType for port {3}. Check you system configuration.
   */
  String msgServiceImportsWrongPortTypeForPartnerLinkRole(String procName,
      String name, String myRole, String portName) {
    return format("The BPEL service {0} with partnerlink {1} and role {2} does not " +
        "match the portType for port {3}. Check you system configuration.", 
        name,myRole,portName);
  }

  /**
   * Message-Exchange "{0}" failed.
   */
  String msgMessageExchangeFailure(String instanceId) {
    return format("Message-Exchange \"{0}\" failed.", instanceId);
  }

  /**
   * Unable to instantiate processor {0}.
   */
  String msgProcessorInstantiationError(String serviceName) {
    return format("Unable to instantiate processor {0}.",serviceName);
  }

  /**
   * Not connected.
   */
  String msgIntrospectorNotConnected() {
    return format("Not connected.");
  }

  /** Protocol-binding error. */
  String msgProtocolBindingError() {
    return format("Protocol-binding error. ");
  }

  /** Documents not available. */
  String msgDocumentsNotAvailable(){ 
    return format("Documents not available. ");
  }


  /** A database error prevented the operation from succeeding. */
  String msgDbError() {
    return format("A database error prevented the operation from succeeding.");
  }

  /** The instance "{0}" was not found in the database. */
  String msgInstanceNotFound(Long pid) {
    return format("The instance \"{0}\" was not found in the database.",pid);
  }

  /** Instance management error: {0} */
  String msgBpelManagementException(String message) {
    return format("Instance management error: {0}", message);
  }

  public String msgBpelDeploymentDescriptorNotFound(String canonicalPath, String service) {
    return format("Deployment descriptor {0} not found, cannot deploy service {1}", canonicalPath, service);
  }

  public String msgBpelDeployFailure(String canonicalPath, String serviceName) {
    return format("Failed to deploy BPEL process from descriptor {0} for service {1}");
  }

  public String msgBpelUndeployFailure(String serviceName) {
    return format("Failed to undeploy BPEL process for service {0}.");
  }

  public String msgBpelDatabaseNotFound(String jndiUri) {
    return format("BPEL database datasource \"{0}\" not found in JNDI.",jndiUri);
  }


}