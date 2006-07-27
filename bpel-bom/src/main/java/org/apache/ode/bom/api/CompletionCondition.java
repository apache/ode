package org.apache.ode.bom.api;

/**
 * Interface for a <code>&lt;completionCondition&gt;</code> as used in a
 * forEach activity.
 */
public interface CompletionCondition extends Expression {

  /**
   * Defines whether the completion count should include all
   * terminated children or only successfully completed ones.
   * @param completed
   */
  void setSuccessfulBranchesOnly(boolean completed);

  /**
   * Gets whether the completion count should include all
   * terminated children or only successfully completed ones.
   * @return counts completed
   */
  boolean isSuccessfulBranchesOnly();
}
