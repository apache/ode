/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.WhileActivity;
import com.fs.utils.NSContext;

/**
 * BPEL object model representation of a <code>&lt;while&gt;</code> activity.
 */
public class WhileActivityImpl extends ActivityImpl implements WhileActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public WhileActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  private Activity _activity;

  private Expression _condition;

  public WhileActivityImpl() {
    super();
  }

  public void setActivity(Activity activity) {
    _activity = activity;
  }

  public com.fs.pxe.bom.api.Activity getActivity() {
    return _activity;
  }

  public void setCondition(Expression condition) {
    _condition = condition;
  }

  public Expression getCondition() {
    return _condition;
  }

  /**
   * @see Activity#getType()
   */
  public String getType() {
    return "while";
  }
}
