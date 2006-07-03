/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.engine;

import java.net.URI;
import java.util.Date;

import javax.xml.namespace.QName;

import com.fs.utils.msg.MessageBundle;

/**
 * Message bundle used by the BPEL provider implementation.
 */
public class Messages extends MessageBundle {

  String msgBarProcessLoadErr() {
    return format("Unable to load compiled BPEL process.");
  }

  String msgProcessDeployed(QName processId) {
    return format("Process {0} deployed." , processId);
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

  public String msgUnknownEPR(String epr) {
    return format("Unkown EPR: {0}" , epr);
  }

  public String msgProcessUndeployed(QName process) {
    return format("Process {0} has been undeployed." , process);
  }

  public String msgProcessUndeployFailed(QName process) {
    return format("Failed to undeploy process {0}! Check database for consistency!" , process);
  }

  public String msgProcessNotFound(QName pid) {
    return format("Process {0} not found. ",pid);
  }

  public String msgProcessNotActive(QName processId) {
    return format("Process {0} is not active. ", processId);
  }

  public String msgProcessLoadError(QName processId) {
    return format("Process {0}, could not be loaded. ", processId);
  }

  public String msgDeployFailDescriptorURIInvalid(URI dduri) {
    return format("Deployment failure: invalid deployment descriptor URI \"{0}\" ", dduri);
  }

  public String msgDeployFailDescriptorInvalid(URI dduri) {
    return format("Deployment failure: invalid/malformed deployment descriptor at \"{0}\"", dduri);
  }

  public String msgDeployFailDescriptorIOError(URI dduri) {
    return format("Deployment failure: IO error reading deployment descriptor at \"{0}\"", dduri);
  }

  /** Partner link declared in process deployment descriptor could not be found in process definition: {0} */
  String msgDDPartnerLinkNotFound(String partnerLinkName) {
    return format("Partner link declared in process deployment descriptor could not be found in process " +
            "definition: {0}", partnerLinkName);
  }

  /** Role 'partnerRole' declared in process deployment descriptor isn't defined in process definition for partner link : {0} */
  String msgDDPartnerRoleNotFound(String partnerLinkName) {
    return format("Role 'partnerRole' declared in process deployment descriptor isn't defined in process definition " +
            "for partner link : {0}", partnerLinkName);
  }

  /** Role 'myRole' declared in process deployment descriptor isn't defined in process definition for partner link : {0} */
  String msgDDMyRoleNotFound(String partnerLinkName) {
    return format("Role 'myRole' declared in process deployment descriptor isn't defined in process definition for " +
            "partner link : {0}", partnerLinkName);
  }

  /**
   * Partner link {0} is defined in process as initializePartnerRole=no, its partner role endpoint can't be
   * initialized by deployment descriptor.
   */
  String msgDDNoInitiliazePartnerRole(String partnerLinkName) {
    return format("Partner link {0} is defined in process as initializePartnerRole=no, its partner role endpoint " +
            "can't be initialized by deployment descriptor.", partnerLinkName);
  }

  public String msgProcessDeployErrAlreadyDeployed(QName processId) {
    return format("The process could not be deployed; another process is already deployed as {0}!",processId); 
  }

  public String msgScheduledJobReferencesUnknownInstance(Long iid) {
    return format("Received a scheduled job event for unknown instance {0}", iid); 
  }

  public String msgReschedulingJobForInactiveProcess(QName processId, String jobId, Date rescheduled) {
    return format("Received a scheduled job event for inactive process {0}; " +
        "rescheduling job {1} for {2}", processId, jobId, rescheduled); 
  }

  public String msgProcessActivationError(QName pid) {
    return format("Error activating process {0}",pid);
  }

  public String msgOperationInterrupted() {
    return format("Operation was interrupted.");
  }

  public String msgProcessActivated(QName pid) {
    return format("Activated process {0}.",pid);
  }

  public String msgServerStarted() {
    return format("BPEL Server Started.");
  }

  public String msgServerStopped() {
    return format("BPEL Server Stopped.");
  }

  public String msgUndefinedServicePort(QName service, String port) {
    return format("The service name and port defined in your deployment descriptor couldn't be found " +
            "in any WSDL document: {0} {1}.", service, port);
  }
}