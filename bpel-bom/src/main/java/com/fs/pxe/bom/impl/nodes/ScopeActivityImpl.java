/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.ScopeActivity;
import com.fs.utils.NSContext;


/**
 * ScopeActivityImpl definition
 *
 * @author jguinney
 */
public class ScopeActivityImpl extends ScopeImpl implements ScopeActivity {

  private static final long serialVersionUID = -1L;

  private Activity _child;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public ScopeActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public ScopeActivityImpl() {
    super();
  }

  public void setChildActivity(Activity activity) {
    _child = activity;
  }

  public Activity getChildActivity() {
    return _child;
  }

  /**
   * @see Activity#getType()
   */
  public String getType() {
    return "scope";
  }
}
