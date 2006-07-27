package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

public class BpelStartCounterValueState extends BpelExpressionState {

  public BpelStartCounterValueState(StartElement se, ParseContext pc) throws ParseException {
    super(se, pc);
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_START_COUNTER_VALUE;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelStartCounterValueState(se,pc);
    }
  }

}
