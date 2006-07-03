/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi.breaks;

/**
 * A break point that is activated when a variable is modified.
 */
public interface VariableModificationBreakpoint extends Breakpoint{
  
  /** 
   * Get the name of the declaring scope for the variable. 
   * @return the name of the scope of <code>null</code> if the scope is the process
   * itself.
   */
  public String scopeName();
  
  /**
   * Get the name of the variable for which the break point is set.
   * @return the name of the variable.
   */
  public String variableName();
}
