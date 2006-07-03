/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.CompensateActivity;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OCompensate;


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
