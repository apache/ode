/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.CompensationHandler;
import org.apache.ode.bom.api.Scope;
import org.apache.ode.utils.NSContext;

/**
 * Normalized representation of a BPEL compensation handler block (a
 * <code>compensationHandler</code> element). The compensation handler block
 * contains a reference to the activity that will be enabled in the event of
 * scope compensation.
 */
public class CompensationHandlerImpl extends BpelObjectImpl implements CompensationHandler {

  private static final long serialVersionUID = -1L;
  private ActivityImpl _activity;
  private ScopeImpl _declaredIn;

  public CompensationHandlerImpl() {
    super();
  }

  public CompensationHandlerImpl(NSContext nsContext) {
    super(nsContext);
  }

  public Scope getScope() {
    return _declaredIn;
  }

  public Activity getActivity() {
    return _activity;
  }

  public void setActivity(Activity activity) {
    _activity = (ActivityImpl) activity;
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _declaredIn = scopeLikeConstruct;
  }
}
