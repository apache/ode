/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.httpsoap;

import java.net.URL;

import com.fs.utils.msg.MessageBundle;

import javax.xml.namespace.QName;

/**
 * Internationalization (Messages) for the SAAJ Connector.
 */
public class Messages extends MessageBundle {

  /**
   * The "{0}" servlet property was not set or set to an invalid value; value
   * was "{1}".
   */
  public String msgServletInitPropertyNotSetOrInvalid(String propName, String value) {
    return this.format("The \"{0}\" servlet property was not set or set to an"
        + "invalid value; value was \"{1}\".", propName, value);
  }

  /**
   * A PXE connection factory was not found at JNDI location "{0}"!
   */
  public String msgPxeConnectionFactoryNotFound(String spcfJndiName) {
    return this.format("A PXE connection factory was not found at JNDI location \"{0}\"!",
        spcfJndiName);
  }

  /**
   * Could not obtain the initial JNDI context; check configuration!
   */
  public String msgJndiInitialContextError() {
    return this.format("Could not obtain the initial JNDI context; check configuration!");
  }

  /**
   * The PXE connection factory at JNDI location "{0}" is invalid or
   * incompatible!
   */
  public String msgPxeConnectionFactoryError(String spcfJndiName) {
    return this.format("The PXE connection factory at JNDI location \"{0}\" is invalid"
        + "or incompatible!", spcfJndiName);
  }

  /** The HTTP method "{0}" is not supported. */
  public String msgMethodNotSupported(String method) {
    return this.format("The HTTP method \"{0}\" is not supported.", method);
  }

  /** The WSDL URI "{0}" is malformed. */
  public String msgMalformedWsdlURI(String wsdlURI) {
    return this.format("The WSDL URI \"{0}\" is malformed.", wsdlURI);
  }

  /** Servlet Init Property "{0}" = "{1}" */
  public String msgServletInitPropertyValue(String name, String value) {
    return this.format("Servlet Init Property \"{0}\" = \"{1}\"", name, value);
  }

  /** A PXE error prevented processing of request URI "{0}". */
  public String msgPxeInteractionError(String requestUri) {
    return this.format("A PXE error prevented processing of request URI \"{0}\".", requestUri);
  }

  /**
   * The deployment property "{1}" for service "{0}" was not set or invalid;
   * value was "{2}").
   */
  public String msgServiceDeploymentPropertyNotSetOrInvalid(String serviceName, String propertyName,
      String propertyValue) {
    return this.format("The deployment property \"{1}\" for service \"{0}\" was not set"
        + " or invalid; value was \"{2}\").", serviceName, propertyName, propertyValue);
  }

  /**
   * The service "{0}" did not specify a concrete WSDL URL in the deployment
   * property "{1}"; will attempt to use system-wide WSDL.
   */
  public String msgSystemDoesNotSpecifyConcreteWsdlUrl(String serviceName, String propertyName) {
    return this.format("The service \"{0}\" did not specify a concrete WSDL URL"
        + "in the deployment property \"{1}\"; will attempt to use system-wide WSDL.",
        serviceName, propertyName);
  }

  /** Could not parse concrete WSDL for service "{0}". */
  public String msgErrorParsingConcreteWSDL(String serviceName) {
    return this.format("Could not parse concrete WSDL for service \"{0}\".", serviceName);
  }

  /** Could not find the service description named "{1}" for service "{0}". */
  public String msgServiceNotInWSDL(String serviceName, QName serviceQName) {
    return this.format("Could not find the service description named \"{1}\""
        + "for service \"{0}\".", serviceName, serviceQName);
  }

  /** The service "{0}" declared in-ports, which are not supported. */
  public String msgInPortsNotSupported(String serviceName) {
    return this.format("The service \"{0}\" declared in-ports, which are not supported.",
        serviceName);
  }

  /** The service "{0}" declared an in-port "{1}" that was not found in the WSDL. */
  public String msgPortNotInWsdl(String serviceName, String portName) {
    return this.format("The service \"{0}\" declared an in-port \"{1}\" that"
        + " was not found in the WSDL.", serviceName, portName);
  }

  /** Unable to map the HTTP request to known port; request port was "{0}". */
  public String msgCannotMapRequestToPort(String port) {
    return this.format("Unable to map the HTTP request to known port;"
        + " request port was \"{0}\".", port);
  }

  /**
   * A WSDL namespace was NOT specified for the service "{0}" (the "{1}"
   * property was not set); using WSDL target namespace "{2}" instead.
   */
  public String msgServiceWsdlNamespaceNotSpecified(String serviceName, String propertyName,
      String targetNamespace) {
    return this.format("A WSDL namespace was NOT specified for the service \"{0}\""
        + "(the \"{1}\" property was not set); using WSDL target namespace \"{2}\" instead.",
        serviceName, propertyName, targetNamespace);
  }

  /**
   * A WSDL service name was NOT specified for the service "{0}" (the "{1}"
   * property was not set); assuming WSDL service name is "{0}".
   */
  public String msgServiceWsdlServiceNameNotSpecified(String serviceName, String propertyName) {
    return this.format("A WSDL service name was NOT specified for the service \"{0}\""
        + "(the \"{1}\" property was not set); assuming WSDL service name is \"{0}\".",
        serviceName, propertyName);
  }

  /**
   * Invalid WSDL service name for service "{0}": Namespace "{1}" and service
   * name "{2}" are invalid.
   */
  public String msgInvalidWsdlServiceName(String serviceName, String wsdlNamespace,
      String serviceWsdlName) {
    return this
        .format("Invalid WSDL service name for service \"{0}\": Namespace \"{1}\""
            + "and service name \"{2}\" are invalid.", serviceName, wsdlNamespace,
            serviceWsdlName);
  }

  /** Error obtaining PXE connection. Check Servlet Init properties {0}. */
  public String msgPxeConnectionError(String props) {
    return this.format("Error obtaining PXE connection. Check Servlet Init properties {0}.",
        props);
  }

  // TODO better message text
   public String msgRequestMappingError(String mexId) {
    return this.format("msgRequestMappingError ${0}", mexId);
  }

  /** The one-way SOAP invocation with message exchange identifier "{0}" failed. */
  public String msgOneWayFailure(String mexId, URL url) {
    return this.format("The one-way SOAP invocation on {1} for message exchange identifier"
        + "\"{0}\" failed.", mexId);
  }

   public String msgResponseMappingError(String mexId, String partName, URL url) {
    return this.format("Error mapping SOAP response from {3} for part \"{1}\" for message exchange \"{0}\". ",mexId,partName,url);
  }

   // TODO better message text
  public String msgResponseFormatError(String mexId, URL url) {
    return this.format("Error parsing SOAP response from {1} for message exchange \"{0}\".", mexId, url);
  }

  public String msgFaultMappingError(String mexId, String serverFaultString, URL url) {
    return this.format("Error mapping fault '{1}' received from {2} for message exchange \"{1}\".");
  }

  public String msgHttpInvokeError(String mexId, URL url) {
    return this.format("Error invoking SOAP service at {1} for message exchange \"{0}\".", mexId, url);
  }

  // TODO better message text
  public String msgInvokeHazard(String instanceId) {
    return this.format("msgInvokeHazard ${0}", instanceId);
  }

  // TODO better message text
  public String msgIncompleteResponse(String instanceId, String missingParts) {
    return this.format("msgIncompleteResponse ${0} ${1}", instanceId, missingParts);
  }

  public String msgResponseMissingPart(String instanceId, String partName, URL url) {
    return this.format("The SOAP response from {3} for message exchange \"{1}\" is missing part \"{2}\"", instanceId, partName, url);
  }

  /**
   * Unable to create SOAP binding for port "{1}" on service "{0}": {2}
   */
  public String msgSoapBindingError(String serviceName, String portName, String cause) {
    return this.format("Unable to create SOAP binding for port \"{1}\" on service"
        + " \"{0}\": {2}", serviceName, portName, cause);
  }

  /**
   * The WSDL for port "{1}" on service "{0}" does not contain the required SOAP
   * binding element!
   */
  public String msgBindingMissingSoapExtensibilityElement(String serviceName, String portName) {
    return this.format("The WSDL for port \"{1}\" on service \"{0}\" does not contain"
        + " the required SOAP binding element!", serviceName, portName);
  }

  /**
   * The WSDL for port "{1}" on service "{0}" does not contain the required SOAP
   * address element!
   */
  public String msgPortMissingSoapAddressExtensibilityElement(String serviceName, String portName) {
    return this.format("The WSDL for port \"{1}\" on service \"{0}\" does not contain"
        + " the required SOAP address element!", serviceName, portName);
  }

  /**
   * WSDL error for port "{1}" on service "{0}": "{3}".
   */
  public String msgWSDLError(String serviceName, String portName, String reason) {
    return this.format("WSDL error for port \"{1}\" on service \"{0}\": \"{3}\".",
        serviceName, portName, reason);
  }

  /** Invalid SOAP endpoint URL "{1}" for port "{0}". */
  public String msgInvalidSoapAddressLocationURI(String portName, String urlStr) {
    return this.format("Invalid SOAP endpoint URL \"{1}\" for port \"{0}\".",
        portName, urlStr);
  }

  // TODO better message text
   public String msgNoResponseFailure(int statusCode, String statusText) {
    return this.format("msgNoResponseFailure (${0}): ${1}", statusCode, statusText);
  }

}
