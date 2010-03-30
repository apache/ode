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
package org.apache.ode.bpel.pmapi;

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
  ProcessInfoListDocument listProcessesCustom(String filter, String orderKeys, ProcessInfoCustomizer custom);

  /**
   * List the processes known to the engine.
   * @return list of {@link ProcessInfoDocument}s (including instance summaries)
   */
  ProcessInfoListDocument listAllProcesses();
  
  /**
   * List the processes known to the engine, excluding the document list, properties and end point configs.
   * @return list of {@link ProcessInfoDocument}s (including instance summaries)
   */
  ProcessInfoListDocument listProcessesSummaryOnly();
  
  /**
   * Get the process info for a process.
   * @param pid name of the process
   * @param custom used to customize the quantity of information returned
   * @return {@link ProcessInfoDocument}
   */
  ProcessInfoDocument getProcessInfoCustom(QName pid, ProcessInfoCustomizer custom) throws ManagementException;

  /**
   * Get the process info for a process (includingthe instance summary). 
   * @param pid name of the process
   * @return {@link ProcessInfoDocument} with all details.
   */
  ProcessInfoDocument getProcessInfo(QName pid) throws ManagementException;

  /**
   * Get all extensibility elements associated with provided activity ids. Extensibility
   * elements are extracted from the original BPEL document.
   * @param pid process identifier
   * @param aids activity indentifiers
   * @return a list of activity extension as {@link ActivityExtInfoListDocument}
   */
  ActivityExtInfoListDocument getExtensibilityElements(QName pid, Integer[] aids);


  /**
   * Set a process property as a Node.
   * @param pid name of process
   * @param propertyName qname of property
   * @param value property value
   * @return {@link ProcessInfoDocument} reflecting the modification
   */
  ProcessInfoDocument setProcessPropertyNode(QName pid, QName propertyName, Node value)
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
   * Activate a process.
   * @param pid identifier for the process to load
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

    /**
     * Retires all the processes contained in a package.
     * @param packageName
     * @param retired
     */
    void setPackageRetired(final String packageName, final boolean retired);
}
