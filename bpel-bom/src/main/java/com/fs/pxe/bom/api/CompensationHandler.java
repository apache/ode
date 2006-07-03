/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Representation of a BPEL compensation handler.
 */
public interface CompensationHandler extends BpelObject {

  /**
   * Get the scope to which this compensation handler belongs.
   *
   * @return the scope to which this compensation handler belongs
   */
  Scope getScope();

  /**
   * Get the compensating activity. This is the
   * activity that gets activated if the compensation handler
   * is activated.
   *
   * @return compensating activity.
   */
  Activity getActivity();


  /**
   * Set the compensating compensatingActivity. This is the
   * activity that gets activated if the compensation handler
   * is activated.
   *
   * @param compensatingActivity compensating
   */
  void setActivity(Activity compensatingActivity);

}
