/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.ORethrow;

class RethrowGenerator extends DefaultActivityGenerator {

  public void compile(OActivity output, Activity src) {
  }

  public OActivity newInstance(Activity src) {
    return new ORethrow(_context.getOProcess());
  }
}
