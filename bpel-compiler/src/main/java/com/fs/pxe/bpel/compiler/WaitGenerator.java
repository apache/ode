/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.WaitActivity;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OWait;
import com.fs.utils.msg.MessageBundle;

/**
 * Generates code for the <code>&lt;wait&gt;</code> activities.
 */
class WaitGenerator extends DefaultActivityGenerator {

  private WaitGeneratorMessages _msgs = MessageBundle.getMessages(WaitGeneratorMessages.class);

  public OActivity newInstance(Activity src) {
    return new OWait(_context.getOProcess());
  }

  public void compile(OActivity output, Activity src) {
    WaitActivity waitDef = (WaitActivity)src;
    OWait owait = (OWait)output;
    if (waitDef.getFor() != null && waitDef.getUntil() == null) {
      owait.forExpression = _context.compileExpr(waitDef.getFor());
    }
    else if (waitDef.getFor() == null && waitDef.getUntil() != null) {
      owait.untilExpression = _context.compileExpr(waitDef.getUntil());
    }
    else {
      throw new CompilationException(_msgs.errWaitMustDefineForOrUntilDuration().setSource(
          waitDef));
    }
  }
}
