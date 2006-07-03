/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.TerminateActivity;
import com.fs.utils.NSContext;

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
