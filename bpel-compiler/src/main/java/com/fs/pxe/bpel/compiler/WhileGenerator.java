/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.WhileActivity;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OWhile;


/**
 * Generates code for <code>&lt;while&gt;</code> activities.
 */
class WhileGenerator extends DefaultActivityGenerator {
  public OActivity newInstance(Activity src) {
    return new OWhile(_context.getOProcess());
  }

  public void compile(OActivity output, Activity srcx)  {
    OWhile owhile = (OWhile) output;
    WhileActivity src = (WhileActivity)srcx;
    owhile.whileCondition = _context.compileExpr(src.getCondition());
    owhile.activity = _context.compile(src.getActivity());
  }
}
