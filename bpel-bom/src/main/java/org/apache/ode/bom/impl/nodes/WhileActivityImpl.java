/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.WhileActivity;
import org.apache.ode.utils.NSContext;

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

  public org.apache.ode.bom.api.Activity getActivity() {
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
