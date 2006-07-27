/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

/**
 * Representation of a BPEL termination handler.
 */
public interface TerminationHandler extends BpelObject {


  /**
   * Get the termination activity. This is the
   * activity that gets activated if the termination handler
   * is activated.
   *
   * @return termination activity.
   */
  Activity getActivity();


  /**
   * Set the terrmination activity. This is the
   * activity that gets activated if the termination handler
   * is activated.
   *
   * @param terminationActivity
   */
  void setActivity(Activity terminationActivity);

}
