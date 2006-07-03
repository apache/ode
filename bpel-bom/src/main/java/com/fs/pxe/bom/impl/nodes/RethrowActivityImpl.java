/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.RethrowActivity;
import com.fs.utils.NSContext;

/**
 * BPEL object model representation of a <code>&lt;rethrow&gt;</code> activity.
 */
public class RethrowActivityImpl extends ActivityImpl implements RethrowActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public RethrowActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public RethrowActivityImpl() {
    super();
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "rethrow";
  }
}
