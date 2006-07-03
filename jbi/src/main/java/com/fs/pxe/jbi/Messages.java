package com.fs.pxe.jbi;

import java.io.File;

import javax.xml.namespace.QName;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgPxeInstallErrorCfgNotFound(File configFile) {
    return format("Error installing PXE component: config file {0} not found!",
        configFile);
  }

  public String msgPxeInstallErrorCfgReadError(File configFile) {
    return format(
        "Error installing PXE component: config file {0} could not be read!",
        configFile);
  }

  public String msgPxeInstalled(String dir) {
    return format("PXE Service Engine installed in {0}", dir);

  }

  public String msgPxeInitHibernatePropertiesNotFound(File expected) {
    return format("Hibernate configuration file \"{0}\" not found!", expected);
  }

  public String msgPxeUsingExternalDb(String dbDataSource) {
    return format("PXE using external DataSource \"{0}\".", dbDataSource);
  }

  public String msgPxeInitExternalDbFailed(String dbDataSource) {
    return format("Failed to resolved external DataSource at \"{0}\".",
        dbDataSource);
  }

  public String msgPxeInitHibernateErrorReadingHibernateProperties(
      File hibernatePropFile) {
    return format("Error reading Hibernate properties file \"{0}\".",
        hibernatePropFile);
  }

  public String msgPxeStarting() {
    return format("Starting PXE ServiceEngine.");
  }

  public String msgPxeStarted() {
    return format("PXE Service Engine has been started.");
  }

  public String msgPxeInitHibernateDialectDetectFailed() {
    return format("Error detecting database dialect; Hibernate DAO could not be started.");
  }

  public String msgPxeInitialized() {
    return "PXE BPEL Server Initialized.";
  }

  public String msgPxeDbPoolStartupFailed(String url) {
    return format("Error starting Minerva connection pool for \"{0}\".", url);
  }

  public String msgPxeBpelServerStartFailure() {
    return format("PXE BPEL Server failed to start.");
  }

  public String msgPxeProcessDeploymentFailed(File dd, String suid) {
    return format(
        "Error deploying process described by deployment descriptor \"{0}\" for service unit \"{1}\".",
        dd, suid);
  }

  public String msgPxeProcessUndeploymentFailed(QName pid) {
    return format(
        "Error undeploying process \"{0}\".",pid);   
  }

  public String msgServiceUnitDeployFailed(String serviceUnitID) {
    return format("Error deploying service unit \"{0}\".", serviceUnitID);
  }

  public String msgServiceUnitInitFailed(String serviceUnitID) {

    return format("Error initializing service unit \"{0}\".", serviceUnitID);
  }

  public String msgServiceUnitStartFailed(String serviceUnitID) {

    return format("Error starting service unit \"{0}\".", serviceUnitID);
  }
  
  public String msgServiceUnitStopFailed(String serviceUnitID) {

    return format("Error stopping service unit \"{0}\".", serviceUnitID);
  }

  public String msgServiceUnitShutdownFailed(String serviceUnitID) {
    return format("Error shutting down service unit \"{0}\".", serviceUnitID);
  }

  public String msgPxeInitMapperClassNotFound(String messageMapper) {
    return format("Message mapper class \"{0}\" not found!", messageMapper);
  }

  public String msgPxeInitMapperClassLoadFailed(String messageMapper) {
    return format("Message mapper class \"{0}\" could not be loaded!", messageMapper);
  }

  public String msgPxeInitMapperInstantiationFailed(String messageMapper) {
    return format("Message mapper class \"{0}\" could not be instantiated!", messageMapper);
  }

  
}
