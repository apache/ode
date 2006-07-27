/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.TerminationHandler;
import org.apache.ode.utils.NSContext;

/**
 * Normalized representation of a BPEL termination handler block (a
 * <code>terminationHandler</code> element). 
 */
public class TerminationHandlerImpl extends BpelObjectImpl implements TerminationHandler {

  private static final long serialVersionUID = -1L;
  private ActivityImpl _activity;
	private ScopeImpl _declaredIn;

  public TerminationHandlerImpl(NSContext nsContext) {
    super(nsContext);
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
