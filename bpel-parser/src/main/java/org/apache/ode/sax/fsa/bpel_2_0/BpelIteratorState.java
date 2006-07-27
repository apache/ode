package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelIteratorState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private Expression _startCounterValue;
  private Expression _finalCounterValue;

  private BpelIteratorState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
  }

  public Expression getStartCounterValue() {
    return _startCounterValue;
  }

  public Expression getFinalCounterValue() {
    return _finalCounterValue;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_START_COUNTER_VALUE) {
      _startCounterValue = ((BpelStartCounterValueState)pn).getExpression();
    } else if (pn.getType() == BPEL_FINAL_COUNTER_VALUE) {
      _finalCounterValue = ((BpelFinalCounterValueState)pn).getExpression();
    } else {
      super.handleChildCompleted(pn);
    }
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_ITERATOR;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelIteratorState(se,pc);
    }
  }
}
