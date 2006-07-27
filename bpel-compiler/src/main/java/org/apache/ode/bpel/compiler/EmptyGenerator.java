/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OEmpty;


/**
 * Generates code for <code>&lt;empty&gt;</code> activities.
 */
class EmptyGenerator extends DefaultActivityGenerator {

  public void compile(OActivity output, Activity src) {
  }

  public OActivity newInstance(Activity src) {
    return new OEmpty(_context.getOProcess());
  }

}
