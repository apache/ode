/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime.monitor;

import org.apache.ode.bpel.o.OProcess;


/**
 * Interface for monitoring active processes.
 *
 * @author jguinney
 */
public interface ProcessMonitor {
  
  /**
   * Returns the process object to be monitored.  Special care should be taken
   * to insure that the compiled instance classes  specific for this
   * <code>OProcess</code> are in the classpath.
   *
   * @return process definition
   */
  public OProcess getProcess();

  /**
   * Return a list of process instances.
   * @param filter process instance filter 
   * @return process instances
   * @throws MonitorException
   */
  public ProcessInstance[] getProcessInstances(InstanceFilter filter)
                                        throws MonitorException;

  /**
   * Inspect data variable.  If variable has not been initialized, a null
   * value will be returned.
   *
   * @param scopeId scope instance id of the variable
   * @param variableName variable name
   *
   * @return variable data
   *
   * @throws MonitorException
   */
  public String getVariableData(String processInstance, String scopeId, String variableName)
                         throws MonitorException;

  /**
   * Terminates the process instance
   */
  public void kill(String processInstanceId)
            throws MonitorException;

  /**
   * Pauses the process instance
   */
  public void pause(String processInstanceId)
             throws MonitorException;

  /**
   * Resumes the process instance (from paused state)
   */
  public void resume(String processInstanceId)
              throws MonitorException;
}
