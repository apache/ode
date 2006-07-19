package com.fs.pxe.axis2;

import com.fs.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.io.File;

public class Messages extends MessageBundle {

  public String msgPxeInstallErrorCfgNotFound(File configFile) {
    return format("Config file {0} not found, default values will be used.", configFile);
  }

  public String msgPxeInstallErrorCfgReadError(File configFile) {
    return format(
        "Error installing PXE component: config file {0} could not be read!", configFile);
  }

  public String msgPxeInstalled(String dir) {
    return format("PXE Service Engine installed in {0}", dir);
  }

  public String msgPxeInitHibernatePropertiesNotFound(File expected) {
    return format("Hibernate configuration file \"{0}\" not foun, defaults will be used.!", expected);
  }

  public String msgPxeUsingExternalDb(String dbDataSource) {
    return format("PXE using external DataSource \"{0}\".", dbDataSource);
  }

  public String msgPxeInitExternalDbFailed(String dbDataSource) {
    return format("Failed to resolved external DataSource at \"{0}\".", dbDataSource);
  }

  public String msgPxeInitHibernateErrorReadingHibernateProperties(
      File hibernatePropFile) {
    return format("Error reading Hibernate properties file \"{0}\".", hibernatePropFile);
  }

  public String msgPxeStarting() {
    return format("Starting PXE ServiceEngine.");
  }

  public String msgPxeStarted() {
    return format("PXE Service Engine has been started.");
  }

  public String msgPollingStarted(String path) {
    return format("Process deployment polling started on path {0}.", path);
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
    return format("Error undeploying process \"{0}\".",pid);
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

  /** Port {0} is missing <wsdl:binding> or <soapbind:binding> */
  public String msgNoBindingForService(QName sericeName) {
    return format("Couldn't find any port in service {0} having <wsdl:binding> and <soapbind:binding>", sericeName);
  }

  /** Port {0} has multiple <soapbind:binding> elements! */
  public String msgMultipleSoapBindingsForPort(String portName) {
    return format("Port {0} has multiple <soapbind:binding> elements!", portName);
  }

  /**
   * Attempted to import WSDL for namespace {0} from multiple locations:
   * definitions from {1} will be ignored!
   */
  public String msgDuplicateWSDLImport(String tns, String location) {
    return format("Attempted to import WSDL for namespace {0} from"
        + " multiple locations: definitions from {1} will be ignored!", tns, location);
  }

  /** The WSDL for namespace "{0}" could not be found in "{1}". */
  public String msgWsdlImportNotFound(String wsdlUri, String location) {
    return format("The WSDL for namespace \"{0}\" could not be found in \"{1}\".", wsdlUri, location);
  }

  /** Error in schema processing: {0} **/
  public String errSchemaError(String detailMessage) {
    return format("Error in schema processing: {0}", detailMessage);
  }

}
