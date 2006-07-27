/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.ThrowActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OThrow;


/**
 * Generates code for <code>&lt;throw&gt;</code> activities.
 */
class ThrowGenerator extends DefaultActivityGenerator {

  public OActivity newInstance(Activity src) {
    return new OThrow(_context.getOProcess());
  }

  public void compile(OActivity output, Activity src) {
    ThrowActivity throwDef = (ThrowActivity)src;
    OThrow othrow = (OThrow) output;
    othrow.faultName = throwDef.getFaultName();
    if(throwDef.getFaultVariable() != null)
    	othrow.faultVariable = _context.resolveVariable(throwDef.getFaultVariable());
  }
}
