/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.store;

import java.io.File;
import java.net.URI;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.utils.msg.MessageBundle;

/**
 * Message bundle used by the BPEL provider implementation.
 *
 * @author mszefler
 */
public class Messages extends MessageBundle {

    String msgBarProcessLoadErr() {
        return format("Unable to load compiled BPEL process.");
    }

    String msgProcessDeployed(Object dir, QName processId) {
        return format("Process {1} deployed from \"{1}\"." , dir, processId);
    }

    /** A database error prevented the operation from succeeding. */
    String msgDbError() {
        return format("A database error prevented the operation from succeeding.");
    }

    /** The instance "{0}" was not found in the database. */
    String msgInstanceNotFound(Long pid) {
        return format("The instance \"{0}\" was not found in the database.",pid);
    }

    String msgUnknownEPR(String epr) {
        return format("Unkown EPR: {0}" , epr);
    }

    String msgProcessUndeployed(QName process) {
        return format("Process {0} has been undeployed." , process);
    }

    String msgProcessUndeployFailed(QName process) {
        return format("Failed to undeploy process {0}! Check database for consistency!" , process);
    }

    String msgProcessNotFound(QName pid) {
        return format("Process {0} not found. ",pid);
    }

    String msgProcessNotActive(QName processId) {
        return format("Process {0} is not active. ", processId);
    }

    String msgProcessLoadError(QName processId) {
        return format("Process {0}, could not be loaded. ", processId);
    }

    String msgDeployFailDescriptorURIInvalid(URI dduri) {
        return format("Deployment failure: invalid deployment descriptor URI \"{0}\" ", dduri);
    }

    String msgDeployFailDescriptorInvalid(URI dduri) {
        return format("Deployment failure: invalid/malformed deployment descriptor at \"{0}\"", dduri);
    }

    String msgDeployFailDescriptorIOError(URI dduri) {
        return format("Deployment failure: IO error reading deployment descriptor at \"{0}\"", dduri);
    }

    /** Partner link declared in process deployment descriptor could not be found in process definition: {0} */
    String msgDDPartnerLinkNotFound(String partnerLinkName) {
        return format("Partner link declared in process deployment descriptor could not be found in process " +
                "definition: {0}", partnerLinkName);
    }

    String msgDDPartnerRoleNotFound(String partnerLinkName) {
        return format("Role 'partnerRole' declared in process deployment descriptor isn't defined in process definition " +
                "for partner link : {0}", partnerLinkName);
    }

    String msgDDMyRoleNotFound(String partnerLinkName) {
        return format("Role 'myRole' declared in process deployment descriptor isn't defined in process definition for " +
                "partner link : {0}", partnerLinkName);
    }

    String msgDDNoInitiliazePartnerRole(String partnerLinkName) {
        return format("Partner link {0} is defined in process as initializePartnerRole=no, its partner role endpoint " +
                "can't be initialized by deployment descriptor.", partnerLinkName);
    }

    String msgProcessDeployErrAlreadyDeployed(QName processId) {
        return format("The process could not be deployed; another process is already deployed as {0}!",processId);
    }

    String msgScheduledJobReferencesUnknownInstance(Long iid) {
        return format("Received a scheduled job event for unknown instance {0}", iid);
    }

    String msgReschedulingJobForInactiveProcess(QName processId, String jobId, Date rescheduled) {
        return format("Received a scheduled job event for inactive process {0}; " +
                "rescheduling job {1} for {2}", processId, jobId, rescheduled);
    }

    String msgProcessActivationError(QName pid) {
        return format("Error activating process {0}",pid);
    }

    String msgOperationInterrupted() {
        return format("Operation was interrupted.");
    }

    String msgProcessActivated(QName pid) {
        return format("Activated process {0}.",pid);
    }

    String msgServerStarted() {
        return format("BPEL Server Started.");
    }

    String msgServerStopped() {
        return format("BPEL Server Stopped.");
    }

    String msgUndefinedServicePort(QName service, String port) {
        return format("The service name and port defined in your deployment descriptor couldn't be found " +
                "in any WSDL document: {0} {1}.", service, port);
    }

    String msgInterceptorAborted(String mexId, String interceptor, String msg) {
        return format("Message exchange {0} aborted by interceptor {1}: {2}", mexId, interceptor, msg);
    }

    String msgMyRoleRoutingFailure(String messageExchangeId) {
        return format("Unable to route message exchange {0}, EPR was not specified " +
                "and the target my-role could not be inferred.",messageExchangeId);
    }

    String msgPropertyAliasReturnedNullSet(String alias, String variable) {
        return this.format("msgPropertyAliasReturnedNullSet: {0} {1}", alias, variable);
    }

    String msgUnknownOperation(String operationName,QName portType) {
        return format("Unknown operation \"{0}\" for port type \"{1}\".",operationName,portType);
    }

    public String msgDeployStarting(File deploymentUnitDirectory) {
        return format("Starting deployment of processes from directory \"{0}\". ", deploymentUnitDirectory);
    }

    public String msgDeployFailed(QName name, File deploymentUnitDirectory) {
        return format("Deployment of process \"{0}\" from \"{1}\" failed.", name,deploymentUnitDirectory);
    }

    public String msgDeployRollback(File deploymentUnitDirectory) {
        return format("Deployment of processes from \"{0}\" failed, rolling back. ", deploymentUnitDirectory);
    }

    public String msgOdeInitHibernatePropertiesNotFound(File expected) {
        return format("Hibernate configuration file \"{0}\" not found, defaults will be used.", expected);
    }

    public String msgOdeInitHibernateErrorReadingHibernateProperties(
        File hibernatePropFile) {
      return format("Error reading Hibernate properties file \"{0}\".", hibernatePropFile);
    }

    public String msgOdeInitHibernateDialectDetectFailed() {
      return format("Error detecting database dialect; Hibernate DAO could not be started.");
    }

    public String msgDeployFailDuplicateDU(String name) {
        return format("Deploy failed; Deployment Unit \"{0}\" already deployed!", name);
    }

    public String msgDeployFailDuplicatePID(QName processId, String name) {
        return format("Deploy failed; process \"{0}\" already deployed!",processId);
    }

    public String msgDeployFailedProcessNotFound(QName pid, String du) {
        return format("Deploy failed; process \"{0}\" not found in deployment unit \"{1}\".",pid,du);
    }

    public String msgDeployFailCompileErrors(CompilationException ce) {
        if (ce != null) {
        	return format("Deploy failed; {0}", ce.getMessage());
        } else {
        	return format("Deploy failed; BPEL compilation errors." );
        }
    }
    
}
