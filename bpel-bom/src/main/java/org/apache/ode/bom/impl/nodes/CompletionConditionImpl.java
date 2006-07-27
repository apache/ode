package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.CompletionCondition;

/**
 * BPEL completion condition as used inside a forEach activity.
 */
public class CompletionConditionImpl extends ExpressionImpl implements CompletionCondition {

  private boolean successfulBranchesOnly = false;

  public CompletionConditionImpl() {
    super();
  }

  public CompletionConditionImpl(String expressionLanguage) {
    super(expressionLanguage);
  }

  public boolean isSuccessfulBranchesOnly() {
    return successfulBranchesOnly;
  }

  public void setSuccessfulBranchesOnly(boolean successfulBranchesOnly) {
    this.successfulBranchesOnly = successfulBranchesOnly;
  }
}
