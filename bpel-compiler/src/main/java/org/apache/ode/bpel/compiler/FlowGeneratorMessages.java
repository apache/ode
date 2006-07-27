/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.capi.CompilationMessage;
import org.apache.ode.bpel.capi.CompilationMessageBundle;

public class FlowGeneratorMessages extends CompilationMessageBundle {

  /** The link "{0}" does not have a source. */
  public CompilationMessage errLinkMissingSourceActivity(String linkName) {
    return this.formatCompilationMessage("The link \"{0}\" does not have a source.", linkName);
  }

  /** The link "{0}" does not have a target. */
  public CompilationMessage errLinkMissingTargetActivity(String linkName) {
    return this.formatCompilationMessage("The link \"{0}\" does not have a target.", linkName);
  }

  /** Duplicate declaration of link "{0}". */
  public CompilationMessage errDuplicateLinkDecl(String linkName) {
    return this.formatCompilationMessage("Duplicate declaration of link \"{0}\".", linkName);
  }

}
