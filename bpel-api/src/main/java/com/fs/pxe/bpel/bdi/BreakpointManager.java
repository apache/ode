/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi;

import com.fs.pxe.bpel.bdi.breaks.ActivityBreakpoint;
import com.fs.pxe.bpel.bdi.breaks.Breakpoint;
import com.fs.pxe.bpel.bdi.breaks.VariableModificationBreakpoint;

/**
 *  Manages breakpoints associated with a process or instance.
 */
public interface BreakpointManager {
  
  /**
   * Adds a breakpoint on an activity by name.
   * @param activityName the name of the activity.
   * @return the newly constructed breakpoint.
   */
  public ActivityBreakpoint addBreakpointRequestOnActivity(String activityName) throws DebuggerException;
  
  /**
   * Removes a specific breakpoint request.
   */
  public void removeBreakpoint(Breakpoint breakpoint) throws DebuggerException;
  
  /**
   * Return all break point requests registered with the request manager
   * @return an array containing all of the break points.
   */
  public Breakpoint[] getBreakpoints() throws DebuggerException;
  
  /** Adds a variable modification request to the request manager
   * @param scopeName
   * @param variableName
   * @return the newly constructed break point.
   */
  public VariableModificationBreakpoint addVariableModificationBreakpoint(String scopeName, String variableName)throws DebuggerException;
  
}
