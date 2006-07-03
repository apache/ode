package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

public class BpelStartCounterValueState extends BpelExpressionState {

  public BpelStartCounterValueState(StartElement se, ParseContext pc) throws ParseException {
    super(se, pc);
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
