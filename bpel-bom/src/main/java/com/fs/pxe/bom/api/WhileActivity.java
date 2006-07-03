/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Representation of the BPEL <code>&lt;while&gt;</code> activity.
 */
public interface WhileActivity extends Activity {
  /**
   * Set the child (repeated) activity.
   *
   * @param activity repeated activity
   */
  void setActivity(Activity activity);

  /**
   * Get the child (repeated) activity.
   *
   * @return repeated activity
   */
  Activity getActivity();

  /**
   * Set the while condition.
   *
   * @param condition the while condition
   */
  void setCondition(Expression condition);

  /**
   * Get the while condition.
   *
   * @return the while condition
   */
  Expression getCondition();
}
