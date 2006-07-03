/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompilationMessageBundle;

public class ReplyGeneratorMessages extends CompilationMessageBundle {

  /** The &lt;reply&gt; must specify a reply message. */
  public CompilationMessage errOutputVariableMustBeSpecified() {
    return this.formatCompilationMessage("The <reply> must specify a reply message.");
  }

  /**
   * The &lt;reply&gt; activity has an undeclared fault "{0}" for operation
   * "{1}".
   */
  public CompilationMessage errUndeclaredFault(String faultName, String operationName) {
    return this.formatCompilationMessage("The <reply> activity has an undeclared fault"
        + "\"{0}\" for operation \"{1}\".", faultName, operationName);
  }

}
