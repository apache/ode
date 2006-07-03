/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompilationMessageBundle;

public class WaitGeneratorMessages extends CompilationMessageBundle {

  /** Must specify exactly one "for" or "until" expression. */
  public CompilationMessage errWaitMustDefineForOrUntilDuration() {
    return this.formatCompilationMessage("Must specify exactly one \"for\" or \"until\" expression.");
  }

}
