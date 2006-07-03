/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.SwitchActivity;
import com.fs.utils.NSContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * BPEL object model representation of a <code>&lt;switch&gt;</code> _activity.
 */
public class SwitchActivityImpl extends CompositeActivityImpl implements SwitchActivity {

  private static final long serialVersionUID = -1L;

  private ArrayList<Case> _cases = new ArrayList<Case>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public SwitchActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public SwitchActivityImpl() {
    super();
  }

  public List<Case> getCases() {
    return Collections.unmodifiableList(_cases);
  }


  public String getType() {
    return "switch";
  }

  public void addCase(Expression condition, Activity activity) {
    _cases.add(new CaseImpl(activity, condition));
  }

  /**
   * BPEL object model representation of a <code>&lt;case&gt;</code>.
   */
  static class CaseImpl extends BpelObjectImpl implements Case {

    private static final long serialVersionUID = 1L;
		private Expression _condition;
    private Activity _activity;

    /**
     * Constructor
     *
     * @param activity  the _activity for this case
     * @param condition the _condition for this case
     */
    CaseImpl(Activity activity, Expression condition) {
      this._activity = activity;
      this._condition = condition;
    }

    public void setActivity(Activity activity) {
      this._activity = activity;
    }

    public void setCondition(Expression condition) {
      _condition = condition;
    }

    /**
     * Get the _activity associated with this case.
     *
     * @return the _activity
     */
    public Activity getActivity() {
      return _activity;
    }

    /**
     * Get the _condition associated with this case.
     *
     * @return the _condition
     */
    public Expression getCondition() {
      return _condition;
    }
  }
}
