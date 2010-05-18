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

package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompilationMessageBundle;

import javax.xml.namespace.QName;

/**
 * General compilation messages.
 */
public class CommonCompilationMessages extends CompilationMessageBundle {

    /** Compiling BPEL process. */
    public CompilationMessage infCompilingProcess() {
        return this.formatCompilationMessage("Compiling BPEL process.");
    }

    /** Error parsing BPEL process: the BPEL is either malformed or is invalid. */
    public CompilationMessage errBpelParseErr() {
        return this
            .formatCompilationMessage("Error parsing BPEL process: the BPEL is either malformed or is invalid.");
    }

    /** Compilation completed with {0} error(s): {1} */
    public CompilationMessage errCompilationErrors(int errorCount, String prettyErrors) {
        return this.formatCompilationMessage("Compilation completed with {0} error(s):\n{1}",
            errorCount, prettyErrors);
    }

    /** Attempt to reference undeclared link "{0}". */
    public CompilationMessage errUndeclaredLink(String linkName) {
        return this.formatCompilationMessage("Attempt to reference undeclared link \"{0}\".",
            linkName);
    }

    /** The import "{0}" is invalid / malformed. */
    public CompilationMessage errInvalidImport(String importUri) {
        return this.formatCompilationMessage("The import \"{0}\" is invalid/malformed. */",
            importUri);
    }

    /** Unable to import WSDL at URI "{0}"; WSDL fault code "{1}". */
    public CompilationMessage errWsdlImportFailed(String wsdlUri, String faultCode) {
        return this.formatCompilationMessage(
            "Unable to import WSDL at URI \"{0}\"; WSDL fault code \"{1}\".", wsdlUri, faultCode);
    }

    /** The WSDL for namespace "{0}" could not be found in "{1}". */
    public CompilationMessage errWsdlImportNotFound(String wsdlUri, String location) {
        return this.formatCompilationMessage(
            "The WSDL for namespace \"{0}\" could not be found in \"{1}\".", wsdlUri, location);
    }

    /** {1}: [{0}] {2} */
    public CompilationMessage errWsdlParseError(String faultCode, String location, String message) {
        return this.formatCompilationMessage("{1}: [{0}] {2}", faultCode, location, message);
    }

    /** The import type "{0}" is unknown. */
    public CompilationMessage errUnknownImportType(String importType) {
        return this.formatCompilationMessage("The import type \"{0}\" is unknown.", importType);
    }

    /** Must specify an import type. */
    public CompilationMessage errUnspecifiedImportType() {
        return this.formatCompilationMessage("Must specify an import type.");
    }

    /** Missing import location. */
    public CompilationMessage errMissingImportLocation() {
        return this.formatCompilationMessage("Missing import location.");
    }

    /** Attempt to reference undeclared property "{0}". */
    public CompilationMessage errUndeclaredProperty(QName propertyName) {
        return this.formatCompilationMessage("Attempt to reference undeclared property \"{0}\".",
            propertyName);
    }

    /** Attempt to reference undeclared variable "{0}". */
    public CompilationMessage errUndeclaredVariable(String varName) {
        return this.formatCompilationMessage("Attempt to reference undeclared variable \"{0}\".",
            varName);
    }

    /** Attempt to reference undeclared correlation set "{0}". */
    public CompilationMessage errUndeclaredCorrelationSet(String csetName) {
        return this.formatCompilationMessage(
            "Attempt to reference undeclared correlation set \"{0}\".", csetName);
    }

    /** Attempt to reference undeclared correlation set "{0}". */
    public CompilationMessage errDuplicateUseCorrelationSet(String csetName) {
        return this.formatCompilationMessage(
            "Attempt to use a correlation set \"{0}\" more than once for a set of correlations.", csetName);
    }

    /**
     * Cannot use non-message variable "{0}" in this context (message variable is
     * required).
     */
    public CompilationMessage errMessageVariableRequired(String variableName) {
        return this.formatCompilationMessage(
            "Cannot use non-message variable \"{0}\" in this context" +
            " (message variable is required).", variableName);
    }

    /** Attempt to declare property "{0}" with complex type "{1}". */
    public CompilationMessage errPropertyDeclaredWithComplexType(QName propertyName, QName complexType) {
        return this.formatCompilationMessage(
            "Attempt to declare property \"{0}\" with complex type \"{1}\".", propertyName,
            complexType);
    }

    /** Duplicate declaration of target for link "{0}". */
    public CompilationMessage errDuplicateLinkTarget(String linkName) {
        return this.formatCompilationMessage("Duplicate declaration of target for link \"{0}\".",
            linkName);
    }

    /** Duplicate declaration of source for link "{0}". */
    public CompilationMessage errDuplicateLinkSource(String linkName) {
        return this.formatCompilationMessage("Duplicate declaration of source for link \"{0}\".",
            linkName);
    }

    /** Duplicate declaration of partnerLink "{0}". */
    public CompilationMessage errDuplicatePartnerLinkDecl(String partnerLinkName) {
        return this.formatCompilationMessage("Duplicate declaration of partnerLink \"{0}\".",
            partnerLinkName);
    }

    /**
     * Declaration of variable "{0}" does not specify the required type (either
     * MessageType or ElementType).
     */
    public CompilationMessage errVariableDeclMissingType(String varName) {
        return this.formatCompilationMessage("Declaration of variable \"{0}\" does not specify"
            + " the required type (either MessageType or ElementType).", varName);
    }

    public CompilationMessage errVariableDeclInvalid(String variable) {
        return this.formatCompilationMessage("Declaration of variable \"{0}\" specifies "
                + " both a MessageType and an ElementType.", variable);
    }

    /**
     * Declaration of variable "{0}" can specify either MessageType or
     * ElementType, but not both.
     */
    public CompilationMessage errVariableDeclMutipleTypes(String varName) {
        return this.formatCompilationMessage("Declaration of variable \"{0}\" can specify either"
            + " MessageType or ElementType, but not both.", varName);
    }

    /** Duplicate declaration of variable "{0}". */
    public CompilationMessage errDuplicateVariableDecl(String varName) {
        return this
            .formatCompilationMessage("Duplicate declaration of variable \"{0}\".", varName);
    }

    /** The BOM activity class "{0}" is unrecoginized. */
    public CompilationMessage errUnknownActivity(String className) {
        return this.formatCompilationMessage("The BOM activity class \"{0}\" is unrecoginized.",
            className);
    }

    /** Process has no root activity. */
    public CompilationMessage errNoRootActivity() {
        return this.formatCompilationMessage("Process has no root activity.");
    }

    /** The expression language "{0}" is unrecognized. */
    public CompilationMessage errUnknownExpressionLanguage(String expressionLanguage) {
        return this.formatCompilationMessage("The expression language \"{0}\" is unrecognized.",
            expressionLanguage);
    }

    /** No WSDL definition for namespace "{0}". */
    public CompilationMessage errNoWsdlDefinitionForNamespace(String namespaceURI) {
        return this.formatCompilationMessage("No WSDL definition for namespace \"{0}\".",
            namespaceURI);
    }

    /** Attempt to reference undeclared WSDL message "{0}" in namespace {1}. */
    public CompilationMessage errUndeclaredMessage(String msgName, String namespaceURI) {
        return this.formatCompilationMessage(
            "Attempt to reference undeclared WSDL message \"{0}\"" + " in namespace {1}.",
            msgName, namespaceURI);
    }

    /** Missing message type reference in property alias {0} in namespace {1}. */
    public CompilationMessage errAliasUndeclaredMessage(QName property, String path) {
        return this.formatCompilationMessage(
            "Missing message type reference in property alias for property {0} using path {1}.",
            property.toString(), path);
    }

    /** The property "{1}" does not have an alias for message type "{0}". */
    public CompilationMessage errUndeclaredPropertyAlias(String messageType, QName propertyName) {
        return this.formatCompilationMessage("The property \"{1}\" does not have an alias"
            + " for message type \"{0}\".", messageType, propertyName);
    }

    /** Attempt to reference undeclared partner link "{0}". */
    public CompilationMessage errUndeclaredPartnerLink(String plinkName) {
        return this.formatCompilationMessage(
            "Attempt to reference undeclared partner link \"{0}\".", plinkName);
    }

    /** Attempt to reference undeclared partner link type "{0}". */
    public CompilationMessage errUndeclaredPartnerLinkType(QName partnerLinkType) {
        return this.formatCompilationMessage(
            "Attempt to reference undeclared partner link type \"{0}\".", partnerLinkType);
    }

    /** Attempt to reference undeclared role "{0}" for partnerLink "{1}". */
    public CompilationMessage errUndeclaredRole(String roleName, QName partnerLinkTypeName) {
        return this.formatCompilationMessage(
            "Attempt to reference undeclared role \"{0}\" for partnerLink \"{1}\".", roleName,
            partnerLinkTypeName);
    }

    /** Attempt to reference undeclared portType "{0}". */
    public CompilationMessage errUndeclaredPortType(QName portType) {
        return this.formatCompilationMessage("Attempt to reference undeclared portType \"{0}\".",
            portType);
    }

    /**
     * Attempt to reference undeclared part "{2}" for variable "{0}": the WSDL
     * message type "{1}" does not declare "{2}".
     */
    public CompilationMessage errUndeclaredMessagePart(String varName, QName messageType, String partName) {
        return this.formatCompilationMessage("Attempt to reference undeclared part \"{2}\""
            + " for variable \"{0}\": the WSDL message type \"{1}\" does not declare \"{2}\".",
            varName, messageType, partName);
    }

    /** The partnerLink "{0}" does not define a myRole. */
    public CompilationMessage errPartnerLinkDoesNotDeclareMyRole(String partnerLinkName) {
        return this.formatCompilationMessage("The partnerLink \"{0}\" does not define a myRole.",
            partnerLinkName);
    }

    /** The partnerLink "{0}" does not define a partnerRole. */
    public CompilationMessage errPartnerLinkDoesNotDeclarePartnerRole(String partnerLinkName) {
        return this.formatCompilationMessage(
            "The partnerLink \"{0}\" does not define a partnerRole.", partnerLinkName);
    }

    /** The partnerLink "{0}" does not define a partnerRole but is set to initializePartnerRole=yes. */
    public CompilationMessage errPartnerLinkNoPartnerRoleButInitialize(String partnerLinkName) {
        return this.formatCompilationMessage(
            "The partnerLink \"{0}\" does not define a partnerRole but is set to initializePartnerRole=yes.", partnerLinkName);
    }

    /** The operation "{1}" is not declared on portType "{0}". */
    public CompilationMessage errUndeclaredOperation(QName portType, String operationName) {
        return this.formatCompilationMessage(
            "The operation \"{1}\" is not declared on portType \"{0}\".", portType, operationName);
    }

    /**
     * The variable "{0}" must be of type "{1}" to be used in this context; its
     * actual type is "{1}".
     */
    public CompilationMessage errVariableTypeMismatch(String varName, QName expectedType,
        QName actualType) {
        return this.formatCompilationMessage("The variable \"{0}\" must be of type \"{1}\""
            + " to be used in this context; its actual type is \"{2}\".", varName, expectedType,
            actualType);
    }

    /** The port type "{0}" does not match the expected port type ("{1}"). */
    public CompilationMessage errPortTypeMismatch(QName portType, QName expectedPortType) {
        return this.formatCompilationMessage("The port type \"{0}\" does not match the expected"
            + " port type (\"{1}\").", portType, expectedPortType);
    }

    /** Attempt to use correlation set "{0}" before it has been initialized. */
    public CompilationMessage errUseOfUninitializedCorrelationSet(String correlationSet) {
        return this.formatCompilationMessage("Attempt to use correlation set \"{0}\" before"
            + " it has been initialized.", correlationSet);
    }

    /**
     * Attempt to use one-way operation "{0}" in a context requiring a
     * request-response.
     */
    public CompilationMessage errTwoWayOperationExpected(String opname) {
        return this
            .formatCompilationMessage("Attempt to use one-way operation \"{0}\" in a context"
                + " requiring a request-response.");
    }

    /** A required query language expression was not present. */
    public CompilationMessage errMissingQueryExpression() {
        return this
            .formatCompilationMessage("A required query language expression was not present.");
    }

    /** Compensation is not applicable in this (non-recovery) context. */
    public CompilationMessage errCompensateNAtoContext() {
        return this
            .formatCompilationMessage("Compensation is not applicable in this (non-recovery) context.");
    }

    /** The scope "{0}" is not available for compensation in this context. */
    public CompilationMessage errCompensateOfInvalidScope(String scopeToCompensate) {
        return this.formatCompilationMessage(
            "The scope \"{0}\" is not available for compensation in this context.",
            scopeToCompensate);
    }

    /** Unrecognized BPEL version! */
    public CompilationMessage errUnrecognizedBpelVersion() {
        return this.formatCompilationMessage("Unrecognized BPEL version!");
    }

    /** Attempt to reference undeclared XSD type "{0}". */
    public CompilationMessage errUndeclaredXsdType(QName typeName) {
        return this.formatCompilationMessage("Attempt to reference undeclared XSD type \"{0}\".",
            typeName);
    }

    /** The declaration of variable "{0}" was not recognized. */
    public CompilationMessage errUnrecognizedVariableDeclaration(String varName) {
        return this.formatCompilationMessage(
            "The declaration of variable \"{0}\" was not recognized.", varName);
    }

    /** FEATURE NOT SUPPORTED: {0} */
    public CompilationMessage errTODO(String description) {
        return this.formatCompilationMessage("FEATURE NOT SUPPORTED: {0}");
    }

    /** A WSDL document must be specified for a BPEL4WS 1.1 process. */
    public CompilationMessage errBpel11RequiresWsdl() {
        return this
            .formatCompilationMessage("A WSDL document must be specified for a BPEL4WS 1.1 process.");
    }

    /** The link "{0}" crosses an event handler boundary. */
    public CompilationMessage errLinkCrossesEventHandlerBoundary(String linkName) {
        return this
            .formatCompilationMessage("The link \"{0}\" crosses an event handler boundary.");
    }

    /** Invalid alarm handler (check for/until/repeatEvery). */
    public CompilationMessage errInvalidAlarm() {
        return this
            .formatCompilationMessage("Invalid alarm handler (check for/until/repeatEvery or child activity).");
    }

    /** Invalid alarm handler (check for/until/repeatEvery). */
    public CompilationMessage errInvalidEvent() {
        return this
            .formatCompilationMessage("Invalid event handler (no child activity?).");
    }

    /** Process WSDL URI is ignored for WS-BPEL 2.0 processes. */
    public CompilationMessage warnWsdlUriIgnoredFor20Process() {
        return this
            .formatCompilationMessage("Process WSDL URI is ignored for WS-BPEL 2.0 processes.");
    }

    /**
     * Attempted to import WSDL for namespace {0} from multiple locations:
     * definitions from {1} will be ignored!
     */
    public CompilationMessage errDuplicateWSDLImport(String tns, String location) {
        return this.formatCompilationMessage("Attempted to import WSDL for namespace {0} from"
            + " multiple locations: definitions from {1} will be ignored!", tns, location);
    }

    /**
     * Error in schema processing: {0}
     */
    public CompilationMessage errSchemaError(String detailMessage) {
        return this.formatCompilationMessage("Error in schema processing: {0}", detailMessage);
    }

    /**
     * XSLT stylesheet URI is invalid/malformed: {0}
     */
    public CompilationMessage errInvalidDocXsltUri(String docStrUri) {
        return this.formatCompilationMessage("XSLT stylesheet URI is invalid/malformed: {0}", docStrUri);
    }

    /**
     * Could not find the XSLT stylesheet referenced with URI {0}, make sure it has been properly provided to the compiler.
     */
    public CompilationMessage errCantFindXslt(String docStrUri) {
        return this.formatCompilationMessage("Could not find the XSLT stylesheet referenced with URI {0}, make " +
                "sure it has been properly provided to the compiler.", docStrUri);
    }

    /**
     * Partner link {0} used in receive activity doesn't define role myRole.
     */
    public CompilationMessage errNoMyRoleOnReceivePartnerLink(String plink) {
        return this.formatCompilationMessage("Partner link {0} used in receive activity doesn't define role myRole.", plink);
    }

    /**
     * Deployment descriptor is invalid: {0}
     */
    public CompilationMessage errInvalidDeploymentDescriptor(String message) {
        return this.formatCompilationMessage("Deployment descriptor is invalid: {0}", message);
    }

    /**
     * The retryFor attribute must be a positive integer, found {0}
     */
    public CompilationMessage errInvalidRetryForValue(String message) {
        return this.formatCompilationMessage("The retryFor attribute must be a positive integer, found {0}", message);
    }

    /**
     * The retryDelay attribute must be a positive integer, found {0}
     */
    public CompilationMessage errInvalidRetryDelayValue(String message) {
        return this.formatCompilationMessage("The retryDelay attribute must be a positive integer, found {0}", message);
    }

    public CompilationMessage errAtomicScopeNesting(boolean atomic) {
        if (atomic)
            return this.formatCompilationMessage("Cannot nest atomic scopes inside each other.");
        else
            return this.formatCompilationMessage("A scope enclosed inside an atomic scope cannot declare itself as not atomic");
    }

    public CompilationMessage errProcessNameNotSpecified() {
        return this.formatCompilationMessage("The process name was not specified.");
    }
    
    public CompilationMessage errProcessNamespaceNotSpecified() {
        return this.formatCompilationMessage("The process namespace was not specified.");
    }

    public CompilationMessage errMissingMyRolePortType(QName portType, String myRole, QName plnkType) {
        return formatCompilationMessage("Missing portType {0} on partnerLinkType {1} for myRole {2}", portType, plnkType, myRole);
    }

    public CompilationMessage errMissingPartnerRolePortType(QName portType, String partnerRole, QName plnkType) {
        return formatCompilationMessage("Missing portType {0} on partnerLinkType {1} for partnerRole {2}", portType, plnkType, partnerRole);
    }

    /**
     * The part {0} declared in property alias for messageType {1} couldn't be found.
     */
    public CompilationMessage errUnknownPartInAlias(String part, String message) {
        return this.formatCompilationMessage("The part {0} declared in property alias for " +
                "messageType {1} couldn't be found.", part, message);
    }

    /** Empty scopes are forbidden. */
    public CompilationMessage errEmptyScope() {
        return this.formatCompilationMessage("Empty scopes are forbidden.");
    }

	public CompilationMessage errEmptyCatch() {
		return this.formatCompilationMessage("Empty catch faut handlers are forbidden.");
	}

	public CompilationMessage errMustSpecifyRelatedVariable(String name) {
		return this.formatCompilationMessage("The external variable declaration for \"{0}\" must specify a related variable.", name);
	}

	public CompilationMessage errMustSpecifyExternalVariableId(String name) {
		return this.formatCompilationMessage("The external variable declaration for \"{0}\" must specify an external variable identifier.", name);
	}

	public CompilationMessage errEmptySequence() {
		return this.formatCompilationMessage("Empty sequences are forbidden.");
	}

}
