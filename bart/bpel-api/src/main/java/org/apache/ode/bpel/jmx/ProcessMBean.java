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
package org.apache.ode.bpel.jmx;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

/**
 * Managed MBean representing operations on a bpel process.
 */
public interface ProcessMBean {

  /**
   * Get the name of the process.
   * @return process name
   */
  String getName();

  /**
   * Suspend an active process.
   * 
   * @param pid process instance id
   */
  void suspend(Long pid) throws Exception ;
  
  /**
   * Resume a process that has been suspended.
   * 
   * @param pid process instance id
   */
  void resume(Long pid) throws Exception ;

  /**
   * Kill a process instance
   * @param pid process instance id
   */
  void kill(Long pid) throws Exception ;
  
  /**
   * Results will include scopeModelId and scopeInstance
   * @param pid process instance id
   * @param variableName variable
   * @param scopeModelId (optional, use 0 for no value)
   * @return
   */
  public TabularData showVariableData(Long pid, String variableName, int scopeModelId)
          throws Exception ;
  
  /**
   * Show the XML data for a given variable and scopeInstanceId.
   * 
   * @param pid process instance id
   * @param variableName variable 
   * @param scopeInstanceId scope instance id
   * @return
   */
  public String showVariableDataForScopeInstance(Long pid, String variableName, Long scopeInstanceId)
          throws Exception ;
   
  /**
   * Simple process instance query.  Use the 'advanced' form for more query features.
   * 
   * @param fromDate (optional) of the form mm/dd/yy hh:mm, e.g. 02/17/2005 13:05
   * @param toDate (optional) of the form mm/dd/yy hh:mm, e.g. 02/17/2005 13:05
   * @param state (optional) filters by process state (see {@link org.apache.ode.bpel.common.ProcessState} for a list of valid states.
   * @return
   */
  public TabularData instanceQuerySimple(String fromDate, String toDate, short state)
          throws Exception ;
  
  /**
   * Return detailed information for a process instance.
   * 
   * @param pid process instance id
   * @return
   */
  public CompositeData showInstanceDetail(Long pid)
          throws Exception ;

}
