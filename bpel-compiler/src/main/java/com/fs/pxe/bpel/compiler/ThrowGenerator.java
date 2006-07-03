/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.ThrowActivity;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OThrow;


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
