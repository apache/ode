/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Representation of the BPEL <code>&lt;compensate&gt;</code> activity.
 */
public interface CompensateActivity extends Activity {

  /**
   * Set the name of the compensated scope: the scope which is compensated by this
   * activity.
   *
   * @param scope scope compensated by this activity
   */
  void setScopeToCompensate(String scope);

  /**
   * Get the name of the compensate scope: the scope which is compensated by this activity.
   *
   * @return scope compensated by this activity
   */
  String getScopeToCompensate();

}
