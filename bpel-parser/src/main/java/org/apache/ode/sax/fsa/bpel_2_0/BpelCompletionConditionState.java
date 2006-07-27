package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.CompletionCondition;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;

class BpelCompletionConditionState extends BaseBpelState {

  private static final StateFactory _factory = new BpelCompletionConditionState.Factory();
  private CompletionCondition _branches;
  private DOMGenerator _domGenerator;

  private BpelCompletionConditionState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _domGenerator = new DOMGenerator();
  }

  public CompletionCondition getCompletionCondition() {
    return _branches;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_BRANCHES) {
      _branches = ((BpelBranchesState)pn).getCompletionCondition();
    } else {
      super.handleChildCompleted(pn);
    }
  }

  public void handleSaxEvent(SaxEvent se) throws ParseException {
    _domGenerator.handleSaxEvent(se);
  }

  /**
   * @see org.apache.ode.sax.fsa.State#done()
   */
  public void done(){
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return BpelCompletionConditionState._factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_COMPLETION_CONDITION;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCompletionConditionState(se,pc);
    }
  }
}
