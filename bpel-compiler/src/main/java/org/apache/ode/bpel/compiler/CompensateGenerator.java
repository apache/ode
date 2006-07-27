/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.CompensateActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OCompensate;


/**
 * Generates code for the <code>&lt;switch&gt;</code> activities.
 */
class CompensateGenerator extends DefaultActivityGenerator {

  public void compile(OActivity output, Activity src) {
    CompensateActivity compSrc = (CompensateActivity) src;
    ((OCompensate)output).compensatedScope = _context.resolveCompensatableScope(compSrc.getScopeToCompensate());
  }

  public OActivity newInstance(Activity src) {
    return new OCompensate(_context.getOProcess());
  }
}
