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
package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Message bundle used by the BPEL provider implementation.
 *
 * @author mszefler
 */
public class Messages extends MessageBundle {

    String msgBarProcessLoadErr() {
        return format("Unable to load compiled BPEL process.");
    }

    String msgProcessDeployed(QName processId) {
        return format("Process {0} deployed.", processId);
    }

    /** A database error prevented the operation from succeeding. */
    String msgDbError() {
        return format("A database error prevented the operation from succeeding.");
    }

    /** The instance "{0}" was not found in the database. */
    String msgInstanceNotFound(Long pid) {
        return format("The instance \"{0}\" was not found in the database.", pid);
    }

    String msgUnknownEPR(String epr) {
        return format("Unkown EPR: {0}", epr);
    }

    String msgProcessRegistered(QName pid) {
        return format("Registered process {0}.", pid);
    }

    String msgProcessUnregistered(QName pid) {
        return format("Unregistered process {0}.", pid);
    }

    String msgProcessUnregisterFailed(QName process) {
        return format("Failed to unregister process {0}! Check database for consistency!", process);
    }

    String msgProcessNotFound(QName pid) {
        return format("Process {0} not found. ", pid);
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

    /**
     * Partner link declared in process deployment descriptor could not be found
     * in process definition: {0}
     */
    String msgDDPartnerLinkNotFound(String partnerLinkName) {
        return format("Partner link declared in process deployment descriptor could not be found in process " + "definition: {0}",
                partnerLinkName);
    }

    String msgDDPartnerRoleNotFound(String partnerLinkName) {
        return format("Role 'partnerRole' declared in process deployment descriptor isn't defined in process definition "
                + "for partner link : {0}", partnerLinkName);
    }

    String msgDDMyRoleNotFound(String partnerLinkName) {
        return format("Role 'myRole' declared in process deployment descriptor isn't defined in process definition for "
                + "partner link : {0}", partnerLinkName);
    }

    String msgDDNoInitiliazePartnerRole(String partnerLinkName) {
        return format("Partner link {0} is defined in process as initializePartnerRole=no, its partner role endpoint "
                + "can't be initialized by deployment descriptor.", partnerLinkName);
    }

    String msgProcessDeployErrAlreadyDeployed(QName processId) {
        return format("The process could not be deployed; another process is already deployed as {0}!", processId);
    }

    String msgScheduledJobReferencesUnknownInstance(Long iid) {
        return format("Received a scheduled job event for unknown instance {0}", iid);
    }

    String msgReschedulingJobForInactiveProcess(QName processId, String jobId, Date rescheduled) {
        return format("Received a scheduled job event for inactive process {0}; " + "rescheduling job {1} for {2}", processId,
                jobId, rescheduled);
    }

    String msgProcessActivationError(QName pid) {
        return format("Error activating process {0}", pid);
    }

    String msgOperationInterrupted() {
        return format("Operation was interrupted.");
    }

    String msgServerStarted() {
        return format("BPEL Server Started.");
    }

    String msgServerStopped() {
        return format("BPEL Server Stopped.");
    }

    String msgUndefinedServicePort(QName service, String port) {
        return format("The service name and port defined in your deployment descriptor couldn't be found "
                + "in any WSDL document: {0} {1}.", service, port);
    }

    String msgInterceptorAborted(String mexId, String interceptor, String msg) {
        return format("Message exchange {0} aborted by interceptor {1}: {2}", mexId, interceptor, msg);
    }

    String msgMyRoleRoutingFailure(String messageExchangeId) {
        return format("Unable to route message exchange {0}, EPR was not specified "
                + "and the target my-role could not be inferred.", messageExchangeId);
    }

    String msgPropertyAliasReturnedNullSet(String alias, String variable) {
        return this.format("msgPropertyAliasReturnedNullSet: {0} {1}", alias, variable);
    }

    String msgUnknownOperation(String operationName, QName portType) {
        return format("Unknown operation \"{0}\" for port type \"{1}\".", operationName, portType);
    }

    String msgPropertyAliasDerefFailedOnMessage(String aliasDescription, String reason) {
        return this.format("Unable to evaluate property alias \"{0}\" to incoming message: {1}", aliasDescription, reason);
    }

    public String msgDeployStarting(File deploymentUnitDirectory) {
        return format("Starting deployment of processes from directory \"{0}\". ", deploymentUnitDirectory);
    }

    public String msgDeployFailed(QName name, File deploymentUnitDirectory) {
        return format("Deployment of process \"{0}\" from \"{1}\" failed.", name, deploymentUnitDirectory);
    }

    public String msgDeployRollback(File deploymentUnitDirectory) {
        return format("Deployment of processes from \"{0}\" failed, rolling back. ", deploymentUnitDirectory);
    }

    public String msgExpLangRegistrationError(String expressionLanguageUri, Map<String, String> properties) {
        return format("Error registering expression language \"" + expressionLanguageUri + "\" with properties " + properties);
    }

    public String msgScheduledJobFailed(JobDetails jobDetail) {
        return format("Scheduled job failed; jobDetail={0}", jobDetail);
    }

    public String msgExtensionMustUnderstandError(QName name, String extensionUri) {
        return format("Deployment of process \"{0}\" failed. The process model requires the " +
        		"engine to understand language extensions defined by {1}. No extension bundle " +
        		"has been registered for this namespace.", name, extensionUri);
    }
}
