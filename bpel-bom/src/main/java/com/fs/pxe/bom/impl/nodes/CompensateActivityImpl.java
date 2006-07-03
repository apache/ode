/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.CompensateActivity;
import com.fs.utils.NSContext;

/**
 * BPEL object model representation of a <code>&lt;compensate&gt;</code> activity.
 */
public class CompensateActivityImpl extends ActivityImpl implements CompensateActivity {

  private static final long serialVersionUID = -1L;

  /**
   * The scope that this activity compensates.
   */
  private String _scope;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public CompensateActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public CompensateActivityImpl() {
    super();
  }

  public void setScopeToCompensate(String scope) {
    _scope = scope;
  }

  public String getScopeToCompensate() {
    return _scope;
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "compensate";
  }
}
