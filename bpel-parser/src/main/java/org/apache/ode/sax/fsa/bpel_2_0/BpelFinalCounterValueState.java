package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

public class BpelFinalCounterValueState extends BpelExpressionState {

  public BpelFinalCounterValueState(StartElement se, ParseContext pc) throws ParseException {
    super(se, pc);
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_FINAL_COUNTER_VALUE;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelFinalCounterValueState(se,pc);
    }
  }

}
