/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OTerminate;


/**
 * Generates code for <code>&lt;terminate&gt;</code> activities.
 */
class TerminateGenerator extends DefaultActivityGenerator
{
  public void compile(OActivity output, Activity src) {
  }

  public OActivity newInstance(Activity src) {
    return new OTerminate(_context.getOProcess());
  }
}