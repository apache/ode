/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * BPEL <code>&lt;scope&gt;</code> activity. A {@link ScopeActivity} is a scope-like construct
 * (see {@link Scope}) and contains a single child activity.
 */
public interface ScopeActivity extends Activity , ScopeLikeConstruct {
  /**
   * Set the child activity for this {@link ScopeActivity}
   *
   * @param activity the child {@link Activity}
   */
  void setChildActivity(Activity activity);

  /**
   * Get the child activity for this {@link ScopeActivity}
   *
   * @return the child {@link Activity}
   */
  Activity getChildActivity();
    
}
