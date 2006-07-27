/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.pmapi;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * Process Management API
 */
public interface ProcessManagement {
  
  /**
   * List the processes known to the engine (including instance summaries).
   * @param filter selection filter or <code>null</code> (for no filtering).
   * @param orderKeys keys used to order the results
   * @return list of {@link ProcessInfoDocument}s (including instance summaries)
   */
  ProcessInfoListDocument listProcesses(String filter, String orderKeys);

  /**
   * List the processes known to the engine.
   * @param filter selection filter or <code>null</code> (for no filtering).
   * @param orderKeys keys used to order the results
   * @param custom used to customize the quantity of information returned
   * @return list of {@link ProcessInfoDocument}s (including instance summaries)
   */
  ProcessInfoListDocument listProcesses(String filter, String orderKeys, ProcessInfoCustomizer custom);

  /**
   * List the processes known to the engine.
   * @return list of {@link ProcessInfoDocument}s (including instance summaries)
   */
  ProcessInfoListDocument listProcesses();
  
  /**
   * Get the process info for a process.
   * @param pid name of the process
   * @param custom used to customize the quantity of information returned
   * @return {@link ProcessInfoDocument}
   */
  ProcessInfoDocument getProcessInfo(QName pid, ProcessInfoCustomizer custom) throws ManagementException;

  /**
   * Get the process info for a process (includingthe instance summary). 
   * @param pid name of the process
   * @return {@link ProcessInfoDocument} with all details.
   */
  ProcessInfoDocument getProcessInfo(QName pid)
          throws ManagementException;

  /**
   * Set a process property as a Node.
   * @param pid name of process
   * @param propertyName qname of property
   * @param value property value
   * @return {@link ProcessInfoDocument} reflecting the modification
   */
  ProcessInfoDocument setProcessProperty(QName pid, QName propertyName, Node value)
          throws ManagementException; 

  /**
   * Set a process property as a simple type.
   * @param pid name of process
   * @param propertyName qname of property
   * @param value property value
   * @return {@link ProcessInfoDocument} reflecting the modification
   */
  ProcessInfoDocument setProcessProperty(QName pid, QName propertyName, String value)
          throws ManagementException;

  /**
   * Associates an endpoint reference with the provided partner link role (overriding
   * default endpoint address extracted from WSDL).
   * @param pid
   * @param partnerLink
   * @param partnerRole
   * @param endpointRef
   * @return {@link ProcessInfoDocument} reflecting the modification
   * TODO: avoid using Element arguments
   */
  ProcessInfoDocument setEndpointReference(QName pid, String partnerLink, String role, Element endpointRef)
          throws ManagementException;

  
  /**
   * Activate a process.
   * @param pid identifier for the process to activate
   * @return {@link ProcessInfoDocument} reflecting the modification
    */
  ProcessInfoDocument activate(QName pid)
          throws ManagementException;

  /**
   * Retire a process.
   * @param pid identifier of the process to retire
   * @param retired TODO
   * @return {@link ProcessInfoDocument} reflecting the modification
   */
  ProcessInfoDocument setRetired(QName pid, boolean retired)
          throws ManagementException;

}
