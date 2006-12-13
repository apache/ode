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

package org.apache.ode.jbi;

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.ode.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

    public String msgOdeInstallErrorCfgNotFound(File configFile) {
        return format("ODE-JBI config file {0} not found; using defaults!", configFile);
    }

    public String msgOdeInstallErrorCfgReadError(File configFile) {
        return format("Error installing ODE component: config file {0} could not be read!", configFile);
    }

    public String msgOdeInstalled(String dir) {
        return format("ODE Service Engine installed in {0}", dir);

    }

    public String msgOdeInitHibernatePropertiesNotFound(File expected) {
        return format("Hibernate configuration file \"{0}\" not found!", expected);
    }

    public String msgOdeUsingExternalDb(String dbDataSource) {
        return format("ODE using external DataSource \"{0}\".", dbDataSource);
    }

    public String msgOdeInitExternalDbFailed(String dbDataSource) {
        return format("Failed to resolved external DataSource at \"{0}\".", dbDataSource);
    }

    public String msgOdeInitHibernateErrorReadingHibernateProperties(File hibernatePropFile) {
        return format("Error reading Hibernate properties file \"{0}\".", hibernatePropFile);
    }

    public String msgOdeStarting() {
        return format("Starting ODE ServiceEngine.");
    }

    public String msgOdeStarted() {
        return format("ODE Service Engine has been started.");
    }

    public String msgOdeInitHibernateDialectDetectFailed() {
        return format("Error detecting database dialect; Hibernate DAO could not be started.");
    }

    public String msgOdeInitialized() {
        return "ODE BPEL Server Initialized.";
    }

    public String msgOdeDbPoolStartupFailed(String url) {
        return format("Error starting Minerva connection pool for \"{0}\".", url);
    }

    public String msgOdeBpelServerStartFailure() {
        return format("ODE BPEL Server failed to start.");
    }

    public String msgOdeProcessDeploymentFailed(File dd, String suid) {
        return format("Error deploying process described by deployment descriptor \"{0}\" for service unit \"{1}\".",
                dd, suid);
    }

    public String msgOdeProcessUndeploymentFailed(QName pid) {
        return format("Error undeploying process \"{0}\".", pid);
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

    public String msgOdeInitMapperClassNotFound(String messageMapper) {
        return format("Message mapper class \"{0}\" not found!", messageMapper);
    }

    public String msgOdeInitMapperClassLoadFailed(String messageMapper) {
        return format("Message mapper class \"{0}\" could not be loaded!", messageMapper);
    }

    public String msgOdeInitMapperInstantiationFailed(String messageMapper) {
        return format("Message mapper class \"{0}\" could not be instantiated!", messageMapper);
    }

    public String msgOdeEmbeddedDbNotFoundUsingTemp(File dbDir, File tmpDir) {
        return format("Embedded database directory \"{0}\" does not exist, creating temporary database in \"{1}\"!", dbDir,tmpDir);
    }

}
