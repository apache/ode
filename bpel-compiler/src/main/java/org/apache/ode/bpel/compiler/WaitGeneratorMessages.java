/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.capi.CompilationMessage;
import org.apache.ode.bpel.capi.CompilationMessageBundle;

public class WaitGeneratorMessages extends CompilationMessageBundle {

  /** Must specify exactly one "for" or "until" expression. */
  public CompilationMessage errWaitMustDefineForOrUntilDuration() {
    return this.formatCompilationMessage("Must specify exactly one \"for\" or \"until\" expression.");
  }

}
