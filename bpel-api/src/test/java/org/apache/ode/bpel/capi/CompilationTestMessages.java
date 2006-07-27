/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.bpel.capi;

public class CompilationTestMessages extends CompilationMessageBundle {

  public CompilationMessage infNoParameter() {
    return this.formatCompilationMessage("No parameter!");
  }

  public CompilationMessage warnNoParameter() {
    return this.formatCompilationMessage("No parameter!");
  }

  public CompilationMessage errNoParameter() {
    return this.formatCompilationMessage("No parameter!");
  }

  public CompilationMessage infWrongParameter(String parm) {
    return this.formatCompilationMessage("Wrong parameter {0}", parm);
  }

  public CompilationMessage warnWrongParameter(String parm) {
    return this.formatCompilationMessage("Wrong parameter {0}", parm);
  }

  public CompilationMessage errWrongParameter(String parm) {
    return this.formatCompilationMessage("Wrong parameter {0}", parm);
  }

  public CompilationMessage msgWrongMethod() {
    return this.formatCompilationMessage("Ouch!");
  }

}
