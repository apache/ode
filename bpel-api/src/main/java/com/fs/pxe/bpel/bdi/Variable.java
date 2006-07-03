/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.bdi;

/**
 *  Debugger variable access.
 */
public interface Variable {
  
  /**
   * Get the name of the variable.
   * @return the name.
   */
  public String getName() throws DebuggerException;
  
  /**
   * Get the identifier of the scope in which the variable is declared.
   * @return the identifier for the scope.
   * @see com.fs.pxe.bpel.dao.ScopeDAO#getScopeInstanceId()
   */
  public Long getScopeId() throws DebuggerException;
  
  /** 
   * Returns the data of the variable.
   * @return the data in the variable or <code>null</code> if the variable is not
   * set. 
   */
  public String getData() throws DebuggerException;
  
  /**
   * Set the value of the variable.
   * @param node the value, as a raw text string.
   */
  public void setData(String node) throws DebuggerException;
}
