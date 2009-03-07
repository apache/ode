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
package org.apache.ode.utils.wsdl;

import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author alexismidon@gmail.com
 */
public class Messages extends MessageBundle {


    /**
     * Attempted to import WSDL for namespace {0} from multiple locations:
     * definitions from {1} will be ignored
     */
    public String msgDuplicateWSDLImport(String tns, String location) {
        return format("Attempted to import WSDL for namespace {0} from"
                + " multiple locations: definitions from {1} will be ignored", tns, location);
    }

    /**
     * The WSDL for namespace "{0}" could not be found in "{1}".
     */
    public String msgWsdlImportNotFound(String wsdlUri, String location) {
        return format("The WSDL for namespace \"{0}\" could not be found in \"{1}\".", wsdlUri, location);
    }

    /**
     * No <http:operation> or <soap:operation> for operation {0}
     */
    public String msgNoBindingForOperation(String operationName) {
        return format("No <http:operation> or <soap:operation> for operation {0}", operationName);
    }

    /**
     * Opeartion {0} has multiple binding elements
     */
    public String msgMultipleBindingsForOperation(String operationName) {
        return format("Operation {0} has multiple binding elements", operationName);
    }

    /**
     * No <http:binding> or <soap:binding> for port {0}
     */
    public String msgNoBinding(QName bindingName) {
        return format("No <http:binding> or <soap:binding> for binding {0}", bindingName);
    }

    /**
     * Port {0} has multiple binding elements
     */
    public String msgMultipleBindings(QName bindingName) {
        return format("Binding {0} has multiple binding elements", bindingName);
    }

    /**
     * No HTTP binding for port: {0}
     */
    public String msgNoHTTPBindingForPort(String name) {
        return format("No HTTP binding for port: {0}", name);
    }

    /**
     * No SOAP binding for port: {0}
     */
    public String msgNoSOAPBindingForPort(String name) {
        return format("No SOAP binding for port: {0}", name);
    }

    /**
     * No address for port {0}
     */
    public String msgNoAddressForPort(String portName) {
        return format("No address for port {0}", portName);
    }

    /**
     * Multiple addresses for port {0}
     */
    public String msgMultipleAddressesForPort(String portName) {
        return format("Multiple addresses for port {0}", portName);
    }

    public Throwable msgSOAPBodyDoesNotContainAllRequiredParts() {
        String s = format("SOAP body does not contain all required parts");
        return new IllegalArgumentException(s);
    }

    public Throwable msgSoapHeaderMustBeAnElement(Node headerNode) {
        String s = format("SOAP header must be an element: {0}.", headerNode);
        return new IllegalArgumentException(s);
    }

    public Throwable msgSoapHeaderMissingRequiredElement(QName elementType) {
        String s = format("SOAP header missing required element: {0}.", elementType);
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

    public Throwable msgBindingDefinesNonElementDocListParts() {
        String s = format("Binding defines non-element document literal part(s)");
        return new IllegalArgumentException(s);
    }

    public Throwable msgUnexpectedElementInSOAPBody(QName name, QName elementName) {
        String s = format("Unexpected element in SOAP body: message {0} element {1}.", name, elementName);
        return new IllegalArgumentException(s);
    }


    public Throwable msgSOAPBodyDoesNotContainRequiredPart(String name) {
        String s = format("SOAP body does not contain required part: {0}.", name);
        return new IllegalArgumentException(s);
    }


    public Throwable msgSoapBodyDoesNotContainExpectedPartWrapper(QName serviceName, String portName, QName rpcWrapQName) {
        String s = format("SOAP body does not contain expected part wrapper: service {0} port {1} wrapper {2}",
                serviceName, portName, rpcWrapQName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgSoapHeaderReferencesUnkownPart(String part) {
        String s = format("SOAP header references unknown part: {0}.", part);
        return new IllegalArgumentException(s);
    }

    public Throwable msgOdeMessageExpected() {
        String s = format("Message expected!");
        return new IllegalArgumentException(s);
    }

    public Throwable msgOdeMessageMissingRequiredPart(String partName) {
        String s = format("Message is missing required part: {0}", partName);
        return new IllegalArgumentException(s);
    }

    public Throwable msgUnexpectedBindingClass(Class passedClass) {
        String s = format("Unexpected class: {0}! Must be passed javax.wsdl.extensions.soap.SOAPBinding or javax.wsdl.extensions.http.HTTPBinding", passedClass);
        return new IllegalArgumentException(s);
    }

    public String msgPortDefinitionNotFound(QName serviceName, String portName) {
        return format("Port definition not found: service {0} port {1}.", serviceName, portName);
    }

    public Throwable msgBindingOperationNotFound(QName serviceName, String portName, String name) {
        String s = format("Binding operation not found: service {0} port {1} name {2}.", serviceName, portName, name);
        return new IllegalArgumentException(s);
    }

    public Throwable msgBindingInputNotFound(QName serviceName, String portName, String name) {
        String s = format("Binding input not found: service {0} port {1} name {2}.", serviceName, portName, name);
        return new IllegalArgumentException(s);
    }


    public Throwable msgBindingOutputNotFound(QName serviceName, String portName, String name) {
        String s = format("Binding output not found: service {0} port {1} name {2}.", serviceName, portName, name);
        return new IllegalArgumentException(s);
    }

    public String msgServiceDefinitionNotFound(QName serviceName) {
        return format("Service definition not found: {0}.", serviceName);
    }

    public String msgBindingNotFound(String portName) {
        return format("Binding not found: port {0}.", portName);
    }

    public String msgMultipleMimeContent() {
        return format("Multiple MIME Contents found!");
    }
}
