/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.capi.CompilationMessage;
import org.apache.ode.bpel.capi.CompilationMessageBundle;

public class InvokeGeneratorMessages extends CompilationMessageBundle {

  /**
   * Invoke doesn't define an output variable even though the operation {0}
   * declares an output message.
   */
  public CompilationMessage errInvokeNoOutputMessageForOutputOp(String operation) {
    return this.formatCompilationMessage(
        "Invoke doesn't define an output variable even though the operation \"{0}\" "
            + "declares an output message.", operation);
  }

  /**
   * Invoke doesn't define an input variable even though the operation {0}
   * declares an input message.
   */
  public CompilationMessage errInvokeNoInputMessageForInputOp(String operation) {
    return this.formatCompilationMessage(
        "Invoke doesn't define an output variable even though the operation \"{0}\" "
            + "declares an output message.", operation);
  }

}
