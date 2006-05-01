/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelUntilState extends BpelExpressionState {
  /**
   * @param se
   * @throws ParseException
   */
  public BpelUntilState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_UNTIL;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelUntilState(se,pc);
    }
  }

}
