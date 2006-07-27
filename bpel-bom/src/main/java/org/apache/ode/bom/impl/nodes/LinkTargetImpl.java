/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.LinkTarget;

/**
 * Implementation of the {@link LinkTarget} interface.
 */
public class LinkTargetImpl extends BpelObjectImpl implements LinkTarget {
  private static final long serialVersionUID = 1L;

  private ActivityImpl _activity;
  private String _linkName;

  public Activity getActivity() {
    return _activity;
  }

  public String getLinkName() {
    return _linkName;
  }

  public void setLinkName(String linkName) {
    _linkName = linkName;
  }

  void setActivity(ActivityImpl activityImpl) {
    _activity = activityImpl;
  }

}
