package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompilationMessageBundle;

public class ForEachGeneratorMessages extends CompilationMessageBundle {

  /** The &lt;reply&gt; must specify a reply message. */
  public CompilationMessage errForEachAndScopeVariableRedundant(String couterName) {
    return this.formatCompilationMessage("The <scope> activity nested inside forEach already declares a " +
            "variable named {0}, just like the forEach counterName.");
  }

}
