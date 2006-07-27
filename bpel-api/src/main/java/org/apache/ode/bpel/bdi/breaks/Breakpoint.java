/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.bdi.breaks;

/**
 * Base interface of all BPEL break-point. Used to enable/disable the
 * break-point.
 */
public interface Breakpoint {

  /**
   * Enable or disable the break point.
   * 
   * @param enabled
   *          if <code>true</code> enable, otherwise disable
   */
  public void setEnabled(boolean enabled);

  /**
   * Test whether the break point is enabled.
   * 
   * @return <code>true</code> if break-point is enabled, <code>false</code>
   *         otherwise
   */
  public boolean isEnabled();

}
