/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.CompositeActivity;
import com.fs.pxe.bom.api.SequenceActivity;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OSequence;

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
