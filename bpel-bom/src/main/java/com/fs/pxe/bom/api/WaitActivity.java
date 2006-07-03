/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Representation of the BPEL <code>&lt;wait&gt;</code> activity.
 */
public interface WaitActivity extends Activity {
  /**
   * Set the "for" expression.
   *
   * @param for_ the "for" expression.
   */
  void setFor(Expression for_);

  /**
   * Get the for expression.
   *
   * @return Returns the for.
   */
  Expression getFor();

  /**
   * Set the "until" expression.
   *
   * @param until the "until" expression
   */
  void setUntil(Expression until);

  /**
   * Get the "until" expression.
   *
   * @return the "until" expression
   */
  Expression getUntil();
}
