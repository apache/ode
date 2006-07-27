/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.WaitActivity;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OWait;
import org.apache.ode.utils.msg.MessageBundle;

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
