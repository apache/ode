/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
