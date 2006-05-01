/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.RethrowActivity;
import org.apache.ode.utils.NSContext;

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
