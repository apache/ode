/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.TerminateActivity;
import org.apache.ode.utils.NSContext;

/**
 * BPEL object model representation of a <code>&lt;empty&gt;</code> activity.
 */
public class TerminateActivityImpl extends ActivityImpl implements TerminateActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public TerminateActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public TerminateActivityImpl() {
    super();
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "terminate";
  }
}
