/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.LinkSource;

/**
 * Implementation of the {@link LinkSource} interface.
 */
public class LinkSourceImpl extends BpelObjectImpl implements LinkSource {
  private static final long serialVersionUID = 1L;

  private ActivityImpl _activity;
  private String _linkName;
  private Expression _transitionCondition;

  public Activity getActivity() {
    return _activity;
  }

  public String getLinkName() {
    return _linkName;
  }

  public void setLinkName(String linkName) {
    _linkName = linkName;
  }

  public Expression getTransitionCondition() {
    return _transitionCondition;
  }

  public void setTransitionCondition(Expression transitionCondition) {
    _transitionCondition = transitionCondition;
  }

  void setActivity(ActivityImpl activity) {
    _activity = activity;
  }
}
