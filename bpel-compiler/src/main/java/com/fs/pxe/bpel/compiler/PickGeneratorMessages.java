/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompilationMessageBundle;

public class PickGeneratorMessages extends CompilationMessageBundle {

  /**
   * Attempt to use multiple non-initiate correlation sets; second set was
   * "{0}".
   */
  public CompilationMessage errSecondNonInitiateCorrelationSet(String setName) {
    return this.formatCompilationMessage(
        "Attempt to use multiple non-initiate correlation sets;" + " second set was \"{0}\".",
        setName);
  }

  // TODO: better error message
  public CompilationMessage errForOrUntilMustBeGiven() {
    return this.formatCompilationMessage("errForOrUntilMustBeGiven");
  }

  // TODO: better error message
  public CompilationMessage errOnAlarmWithCreateInstance() {
    return this.formatCompilationMessage("errOnAlarmWithCreateInstance");
  }

}
