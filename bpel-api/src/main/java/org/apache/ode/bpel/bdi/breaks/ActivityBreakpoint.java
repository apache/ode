/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.bdi.breaks;

/**
 * Break-point on a BPEL activity: breaks at the start of activity execution and on
 * activity completion.
 */
public interface ActivityBreakpoint extends Breakpoint{
  
  /**
   * Get the name of the activity that has been breakpointed.
   * @return the name of the activity
   */
  public String activityName();
  
}
