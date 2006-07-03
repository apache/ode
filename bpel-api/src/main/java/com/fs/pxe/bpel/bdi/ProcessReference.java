/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi;

import java.util.Collection;

import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.pxe.bpel.o.OProcess;

/**
 * <p>
 * Debugger access to a process and its instances.
 * </p>
 */
public interface ProcessReference {
  
    
  /**
   * <p>
   * Return a process instances reference by its unique identifier or <code>null</code>
   * if no such process exists.
   * </p>
   * @param pid the identifier
   * @return a reference to the instance
   */
  public InstanceReference getProcessReference(Long pid) throws DebuggerException;
  
  /**
   * <p>
   * Return a process reference by correlation key values for a globally defined
   * correlation set.
   * </p>
   * @param ckey key to match
   * @return a reference to the instance.
   */
  public InstanceReference getProcessReference(CorrelationKey ckey) throws DebuggerException;
  
  /**
   * <p>
   * Return a process reference by correlation key values for a scope-local
   * correlation set.
   * </p>
   * @param scopeName name of scope in which correlation set is defined.
   * @param ckey correlation key to match
   * @return
   */
  public InstanceReference getProcessReference(String scopeName, CorrelationKey ckey) throws DebuggerException;
	
 
  /**
   * <p>
   * Obtain a break point manager for this process.  Operations with the manager
   * will have an effect on every instance, including those yet to be created.
   * </p>
   * @return a manager
   * @see InstanceReference#getBreakpointManager()
   */
  public BreakpointManager getBreakpointManager() throws DebuggerException;
  
  /**
   * <p>
   * Construct the process object model for this process.
   * </p>
   * @return the object model.
   */
  public OProcess getProcessDef() throws DebuggerException;
}
