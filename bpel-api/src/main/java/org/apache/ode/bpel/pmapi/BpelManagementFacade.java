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

import org.apache.ode.bpel.bdi.breaks.ActivityBreakpoint;
import org.apache.ode.bpel.bdi.breaks.Breakpoint;
import org.apache.ode.bpel.bdi.breaks.VariableModificationBreakpoint;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.o.OProcess;

import javax.xml.namespace.QName;
import java.util.Date;

/**
 * <p>
 * Instance management interaction interface. Instance management is a broad concept
 * that covers instance introspection, debugging, and monitoring. This interface allows
 * external systems to perform these functions.
 * </p>
 *
 * <p>
 * <em>NOTE: this interface is not currently intended to be published.
 * It is currently used by the higher-level debugging facility.
 * </em>
 * </p>
 */
public interface BpelManagementFacade extends ProcessManagement, InstanceManagement {

  /**
   * Get the time that a process instance was started (created).
   * @param iid process instance identifier
   * @return time the instance was started
   */
  Date getStartTime(Long iid) throws ManagementException;

  /**
   * Get the state of a process instance.
   * @param iid process instance identifier
   * @return state of the instance
   */
  short getState(Long iid) throws ManagementException;


  /**
   * Get the process instance with the given correlation correlationKey.
   *
   * @param pid the process identifier
   * @param ckey the correlation correlationKey to match
   * @return process instance identifier of instance matching the given correlationKey
   */
  Long getProcessInstance(String pid, CorrelationKey ckey) throws ManagementException;

  /**
   * Get exeuction event history for a process instance.
   * @param iid process instance identifier
   * @param startIdx first evet
   * @param count maximum number of events to get
   * @return array of {@link ProcessInstanceEvent}s.
   */
  EventInfoListDocument getEvents(Long iid, int startIdx, int count) throws ManagementException;

  /**
   * Get the current number of events for an instance.
   * @param iid process instance identifier
   * @return number of events in event history
   */
  int getEventCount(Long iid) throws ManagementException;

  /**
   * Returns all the scope instance ids for a given instance and scope name.
   * Multiple scopes instances are only possible due to a BPEL 'while' activity.
   *
   * */
  public Long[] getScopeInstancesForScope(Long iid, String scopeName) throws ManagementException;

  /**
   * Gets variable data.
   * @param iid
   * @param scopeId
   * @param varName
   * @return
   */
  String getVariable(Long iid, Long scopeId, String varName) throws ManagementException;

  /**
   * Sets a variable
   * @param iid
   * @param scopeId
   * @param varName
   * @param data
   */
  void setVariable(Long iid, Long scopeId, String varName, String data);

  /**
   * Sets a correlation.
   * @param iid
   * @param scopeId
   * @param correlationSet name of the correlation set
   * @param propertyNames properties to set on correlation set
   * @param values property values as a CorrelationKey object
   */
  void setCorrelation(Long iid, Long scopeId, String correlationSet, QName[] propertyNames, CorrelationKey values) throws ManagementException;

  /**
   * Gets a correlation.
   * @param iid
   * @param scopeId
   * @param correlationSet
   * @return
   */
  CorrelationKey getCorrelation(Long iid, Long scopeId, String correlationSet) throws ManagementException;

  /**
   * Return the process model.
   * @return
   * @param procId
   */
  OProcess getProcessDef(String procId) throws ManagementException;

  /**
   * Single step through a process instance.
   * @param iid
   */
  void step(Long iid) throws ManagementException;

  /**
   * Gets the fault associated with a completed process instance.
   * @param iid
   * @return
   */
  QName getCompletedFault(Long iid) throws ManagementException;

  /**
   * Returns the breakpoints registered with the process instance.
   *
   * @param iid process instance identifier
   * @return array of {@link Breakpoint}s.
   */
  Breakpoint[] getBreakpoints(Long iid) throws ManagementException;

  /**
   * Returns the global breakpoints registered with the process instance.
   *
   * @param procId
   * @return array of {@link Breakpoint}s.
   */
  Breakpoint[] getGlobalBreakpoints(String procId) throws ManagementException;

  /**
   * Removes a breakpoint
   * @param iid a iid of null removes a global breakpoint.
   * @param sp
   */
  void removeBreakpoint(Long iid, Breakpoint sp) throws ManagementException;

  /**
   * Removes a global breakpoint
   * @param procId
   * @param sp
   */
  void removeGlobalBreakpoint(String procId, Breakpoint sp) throws ManagementException;

  /**
   * Adds an activity breakpoint.
   * @param iid a iid of null adds a global activity breakpoint.
   * @param activity
   * @return
   */
  ActivityBreakpoint addActivityBreakpoint(Long iid, String activity) throws ManagementException;

  /**
   * Adds an global activity breakpoint.
   * @param procId
   * @param activity
   * @return
   */
  ActivityBreakpoint addGlobalActivityBreakpoint(String procId, String activity) throws ManagementException;

  /**
   * Adds a variable modification breakpoint.
   * @param iid
   * @param scopeName
   * @param variable
   * @return
   */
  VariableModificationBreakpoint addVariableModificationBreakpoint(Long iid, String scopeName, String variable);

}
