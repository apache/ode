/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.tools.mngmt;

import com.fs.utils.msg.MessageBundle;

import javax.management.remote.JMXServiceURL;

/**
 * Messages for the {@link JmxAdmin} commandline tool and Ant task.
 */
public class MngmtMessages extends MessageBundle {

  /**
   * Format an error message about a malformed domain name.
   * 
   * @param handle
   *          the requested domain name
   * @param message
   *          a descriptive error message
   * 
   * Unable to create an ObjectName for the PXE domain "{0}": {1}
   */
  public String msgBadDomainON(String handle, String message) {
    return this.format("Unable to create an ObjectName for the PXE domain \"{0}\": {1}",
        handle, message);
  }

  /**
   * Format an error message about a bad JMX service URL
   * 
   * @param serviceurl
   *          the (erroneous) service URL
   * @param message
   *          a descriptive message about the error
   * 
   * The JMX service URL {0} does not appear to be valid: {1}
   */
  public String msgBadJMXURL(String serviceurl, String message) {
    return this.format("The JMX service URL {0} does not appear to be valid: {1}", serviceurl,
        message);
  }

  /**
   * Format an error message about a malformed SAR URL.
   * 
   * @param url
   *          the URL
   * @param message
   *          an explantory error message
   * 
   * The SAR URL {0} is malformed: {1}
   */
  public String msgBadSarUrl(String url, String message) {
    return this.format("The SAR URL {0} is malformed: {1}", url, message);
  }

  /**
   * Format a message about a
   * {@link com.fs.pxe.sfwk.deployment.DeploymentException} on the server.
   * 
   * @param dom
   *          the target domain
   * @param url
   *          the URL of the SAR being deployed
   * @param message
   *          a descriptive error message
   * 
   * Unable to deploy system from SAR {1} to domain {0}: {2}
   */
  public String msgDeploymentException(String dom, String url, String message) {
    return this.format("Unable to deploy system from SAR {1} to domain {0}: {2}", dom, url,
        message);
  }

  /**
   * Format an error message about no value or default for the JMX service URL.
   * 
   * A JMX service URL or a default value (via URL alias) must be set.
   */
  public String msgJmxUrlMustBeSet() {
    return this.format("A JMX service URL or a default value (via URL alias) must be set.");
  }

  /**
   * Format an error message about the -name and -uuid options being mutually
   * exclusive.
   * 
   * Exactly one of -name and -uuid must be specified.
   */
  public String msgNameAndUuidAreMutEx() {
    return this.format("Exactly one of -name and -uuid must be specified.");
  }

  /**
   * One of name and uuid must be specified.
   */
  public String msgNameOrUuidRequired() {
    return this.format("One of name and uuid must be specified.");
  }

  /**
   * Format an error message about no domain being found in JMX.
   * 
   * No domain found in JMX.
   */
  public String msgNoDomainFound() {
    return this.format("No domain found in JMX.");
  }

  /**
   * Format an error message about the requested domain not being found in JMX.
   * 
   * @param name
   *          the requested name of the domain.
   * 
   * The domain {0} was not found in JMX.
   */
  public String msgNoDomainFound(String name) {
    return this.format("The domain {0} was not found in JMX.", name);
  }

  /**
   * Format an error message about no domain with the requested name
   * 
   * @param handle
   *          the handle for the domain
   * 
   * Unable to locate a PXE domain named "{0}" on the server.
   */
  public String msgNoSuchDomain(String handle) {
    return this.format("Unable to locate a PXE domain named \"{0}\" on the server.", handle);
  }

  /**
   * Format an error message about the requested system not being found in the
   * domain.
   * 
   * System "{0}" not found in domain {1}.
   */
  public String msgNoSuchSystem(String systemName, String domain) {
    return this.format("System \"{0}\" not found in domain {1}.", systemName, domain);
  }

  /**
   * The "pxe.jmxurl" property is not set; default JMX URL will not be
   * available.
   */
  public String msgPxeJmxUrlNotSet() {
    return this.format("The \"pxe.jmxurl\" property is not set; default JMX URL"
        + " will not be available.");
  }

  /**
   * Format an error message about the SAR file not existing or being
   * unreadable.
   * 
   * The SAR file {0} does not exist, is not readable, or is a directory.
   */
  public String msgSarMustBeReadable(String name) {
    return this.format("The SAR file {0} does not exist, is not readable, or is a directory.",
        name);
  }

  /**
   * Format an error message about the SAR URL being required.
   * 
   * A SAR file path or remote SAR URL is required.
   */
  public String msgSarOrUrlIsRequired() {
    return this.format("A SAR file path or remote SAR URL is required.");
  }

  /**
   * Format an error message about an exception with staging deployment on the
   * target server.
   * 
   * @param dom
   *          the target domain
   * @param url
   *          the URL of the SAR
   * @param message
   *          a descriptive message
   * 
   * Unable to deploy SAR {1} to domain {0}: {2}
   */
  public String msgStagingException(String dom, String url, String message) {
    return this.format("Unable to deploy SAR {1} to domain {0}: {2}", dom, url, message);
  }

  /**
   * Format a message reporting that a system was successfully deployed.
   * 
   * @param dom
   *          the target domain
   * @param sys
   *          the name of the system
   * 
   * Deployed system "{1}" to domain "{0}"
   */
  public String msgSuccessDeployingSystem(String dom, String sys) {
    return this.format("Deployed system \"{1}\" to domain \"{0}\"", dom, sys);
  }

  /**
   * Format a message about the activation of a system.
   * 
   * @param dom
   *          the target domain
   * @param name
   *          the name of the system
   * 
   * Activated system "{1}" in domain "{0}".
   */
  public String msgSystemActivated(String dom, String name) {
    return this.format("Activated system \"{1}\" in domain \"{0}\".", dom, name);
  }

  /**
   * Format a message about being unable to activate a system.
   * 
   * @param dom
   *          the target domain
   * @param name
   *          the name of the system
   * @param message
   *          a descriptive error message
   * 
   * Unable to activate system "{1}" in domain "{0}": {2}
   */
  public String msgUnableToActivateSystem(String dom, String name, String message) {
    return this.format("Unable to activate system \"{1}\" in domain \"{0}\": {2}", dom, name,
        message);
  }

  /**
   * Format an error message about being unable to connect to the JMX server at
   * the specified JMX service URL.
   * 
   * @param surl
   *          the URL
   * @param message
   *          a descriptive error message
   * 
   * Unable to connect to JMX server at {0}: {1}
   */
  public String msgUnableToConnectToJmxServer(JMXServiceURL serviceurl, String message) {
    return this.format("Unable to connect to JMX server at {0}: {1}", serviceurl, message);
  }

  /**
   * Format an error message about being unable to connect to the
   * {@link javax.management.MBeanServer} (within the
   * {@link javax.management.remote.JMXConnectorServer}).
   * 
   * @param message
   *          a descriptive error message
   * 
   * Unable to connect to MBeanServer: {0}
   */
  public String msgUnableToConnectToMBeanServer(String message) {
    return this.format("Unable to connect to MBeanServer: {0}", message);
  }

  /**
   * Format an error message about being unable to determine the registration
   * status of the MBean for the domain.
   * 
   * @param handle
   *          the name of the domain
   * @param message
   *          a descriptive error message
   * 
   * Unable to determine JMX registration status for PXE domain "{0}": {1}
   */
  public String msgUnableToDetermineDomainStatus(String handle, String message) {
    return this.format(
        "Unable to determine JMX registration status for PXE domain \"{0}\": {1}", handle,
        message);
  }

}
