/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.CompositeActivity;
import org.apache.ode.bom.api.SequenceActivity;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OSequence;

import java.util.Iterator;


/**
 * Generates code for <code>&lt;sequence&gt;</code> activities.
 */

class SequenceGenerator extends DefaultActivityGenerator {

  public OActivity newInstance(Activity src) {
    return new OSequence(_context.getOProcess());
  }

  public void compile(OActivity output, Activity src)  {
    OSequence oseq = (OSequence) output;
    compileChildren(oseq, (SequenceActivity) src);
  }

  protected void compileChildren(OSequence dest, CompositeActivity src) {
    for (Iterator<Activity> i = src.getChildren().iterator(); i.hasNext();) {
      Activity child = i.next();
      try {
        OActivity compiledChild = _context.compile(child);
        dest.sequence.add(compiledChild);
      }
      catch (CompilationException ce) {
        _context.recoveredFromError(child, ce);
      }
    }
  }

}
