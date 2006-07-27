package org.apache.ode.bpel.o;

/**
 * Base model class for forEach activity.
 */
public class OForEach extends OActivity {
  static final long serialVersionUID = -1L;

  public OScope.Variable counterVariable;
  public boolean parallel;
  public OExpression startCounterValue;
  public OExpression finalCounterValue;
  public CompletionCondition completionCondition;

  public OScope innerScope;

  public OForEach(OProcess owner) {
    super(owner);
  }

  public String toString() {
    return "+{OForEach : " + name +
            ", counterName=" + counterVariable.name +
            ", parallel=" + parallel +
            ", startCounterValue=" + startCounterValue +
            ", finalCounterValue=" + finalCounterValue +
            ", completionCondition=" + (completionCondition == null ? "" : completionCondition.branchCount) + "}";
  }

  public static class CompletionCondition extends OBase {
    static final long serialVersionUID = -1L;

    public boolean successfulBranchesOnly;
    public OExpression branchCount;

    public CompletionCondition(OProcess owner) {
      super(owner);
    }
  }
}
