package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.capi.CompilationMessage;
import org.apache.ode.bpel.capi.CompilationMessageBundle;

public class ForEachGeneratorMessages extends CompilationMessageBundle {

  /** The &lt;reply&gt; must specify a reply message. */
  public CompilationMessage errForEachAndScopeVariableRedundant(String couterName) {
    return this.formatCompilationMessage("The <scope> activity nested inside forEach already declares a " +
            "variable named {0}, just like the forEach counterName.");
  }

}
