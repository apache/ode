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

package org.apache.ode.axis2;

import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.io.File;

public class Messages extends MessageBundle {

    public String msgOdeInstallErrorCfgNotFound(File configFile) {
        return format("Config file {0} not found, default values will be used.", configFile);
    }

    public String msgOdeInstallErrorCfgReadError(File configFile) {
        return format("Error installing ODE component: config file {0} could not be read.", configFile);
    }

    public String msgOdeInstalled(String dir) {
        return format("ODE Service Engine installed in {0}", dir);
    }

    public String msgOdeInitHibernatePropertiesNotFound(File expected) {
        return format("Hibernate configuration file \"{0}\" not found, defaults will be used.", expected);
    }

    public String msgOdeUsingExternalDb(String dbDataSource) {
        return format("ODE using external DataSource \"{0}\".", dbDataSource);
    }

    public String msgOdeInitExternalDbFailed(String dbDataSource) {
        return format("Failed to resolved external DataSource at \"{0}\".", dbDataSource);
    }

    public String msgOdeInitDAOErrorReadingProperties(File propfile) {
        return format("Error reading DAO properties file \"{0}\".", propfile);
    }

    public String msgOdeStarting() {
        return format("Starting ODE ServiceEngine.");
    }

    public String msgOdeStarted() {
        return format("ODE Service Engine has been started.");
    }

    public String msgPollingStarted(String path) {
        return format("Process deployment polling started on path {0}.", path);
    }

    public String msgOdeInitialized() {
        return "ODE BPEL Server Initialized.";
    }

    public String msgOdeDbPoolStartupFailed(String url) {
        return format("Error starting connection pool for \"{0}\".", url);
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

    /** Port {0} is missing <wsdl:binding> or <soapbind:binding> */
    public String msgNoBindingForPort(QName sericeName) {
        return format("Couldn't find any port in service {0} having <wsdl:binding> and <soapbind:binding>", sericeName);
    }

    /** Port {0} has multiple <soapbind:binding> elements */
    public String msgMultipleSoapBindingsForPort(String portName) {
        return format("Port {0} has multiple <soapbind:binding> elements", portName);
    }

    /**
     * Attempted to import WSDL for namespace {0} from multiple locations:
     * definitions from {1} will be ignored
     */
    public String msgDuplicateWSDLImport(String tns, String location) {
        return format("Attempted to import WSDL for namespace {0} from"
                + " multiple locations: definitions from {1} will be ignored", tns, location);
    }

    /** The WSDL for namespace "{0}" could not be found in "{1}". */
    public String msgWsdlImportNotFound(String wsdlUri, String location) {
        return format("The WSDL for namespace \"{0}\" could not be found in \"{1}\".", wsdlUri, location);
    }

    public String msgBpelEventListenerRegistered(String listenerCN) {
        return format("Registered custom BPEL event listener: {0}", listenerCN);
    }

    public String msgOdeShutdownCompleted() {
        return "Shutdown completed. ";
    }

    public String msgOdeUsingDAOImpl(String className) {
        return format("Using DAO Connection Factory class {0}.", className);
    }

    public String msgDAOInstantiationFailed(String className) {
        return format("Error instantiating DAO Connection Factory class {0}.", className);

    }

    public Throwable msgOdeMessageMissingRequiredPart(String partName) {
        String s = format("Message is missing required part: {0}", partName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgServiceDefinitionNotFound(QName serviceName) {
        String s = format("Servicd definition not found: {0}.", serviceName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgPortDefinitionNotFound(QName serviceName, String portName) {
        String s = format("Port definition not found: service {0} port {1}.", serviceName, portName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgBindingOperationNotFound(QName serviceName, String portName, String name) {
        String s = format("Binding operation not found: service {0} port {1} name {2}.", serviceName, portName, name);
        return new IllegalArgumentException(s);
    }

    public Throwable msgBindingInputNotFound(QName serviceName, String portName, String name) {
        String s = format("Binding input not found: service {0} port {1} name {2}.", serviceName, portName, name);
        return new IllegalArgumentException(s);
    }

    public Throwable msgBindingNotFound(QName serviceName, String portName) {
        String s = format("Binding not found: service {0} port {1}.", serviceName, portName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgNoSOAPBindingForPort(String name) {
        String s = format("No SOAP binding for port: {0}.", name);
        return new IllegalArgumentException(s);
    }

    public Throwable msgSoapHeaderReferencesUnkownPart(String part) {
        String s = format("SOAP header references unknown part: {0}.", part);
        return new IllegalArgumentException(s);
    }

    public Throwable msgBindingDefinesNonElementDocListParts() {
        String s = format("Binding defines non-element document literal part(s)");
        return new IllegalArgumentException(s);
    }

    public Throwable msgUnexpectedElementInSOAPBody(QName name, QName elementName) {
        String s = format("Unexpected element in SOAP body: message {0} element {1}.", name, elementName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgSOAPBodyDoesNotContainAllRequiredParts() {
        String s = format("SOAP body does not contain all required parts");
        return new IllegalArgumentException(s);
    }

    public Throwable msgSOAPBodyDoesNotContainRequiredPart(String name) {
        String s = format("SOAP body does not contain required part: {0}.", name);
        return new IllegalArgumentException(s);
    }

    public Throwable msgSoapHeaderMissingRequiredElement(QName elementType) {
        String s = format("SOAP header missing required element: {0}.", elementType);
        return new IllegalArgumentException(s);
    }

    public Throwable msgBindingOutputNotFound(QName serviceName, String portName, String name) {
        String s = format("Binding output not found: service {0} port {1} name {2}.", serviceName, portName, name);
        return new IllegalArgumentException(s);
    }

    public Throwable msgUndefinedFault(QName serviceName, String portName, String opname, QName faultName) {
        String s = format("Undefined fault: service {0} port {1} operation {2} fault {3}.", serviceName, portName, opname, faultName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgOdeMessagePartMissingRequiredElement(QName serviceName, String portName, String opname, QName elementName) {
        String s = format("Message part is missing required element: service {0} port {1} operation {2} element {3}.",
                          serviceName, portName, opname, elementName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgSoapBodyDoesNotContainExpectedPartWrapper(QName serviceName, String portName, QName rpcWrapQName) {
        String s = format("SOAP body does not contain expected part wrapper: service {0} port {1} wrapper {2}",
                          serviceName, portName, rpcWrapQName);
        return new IllegalArgumentException(s);
    }

    public String msgOdeDbConfigError() {
        return format("Error starting database connections, check the database configuration!");
    }

}
