/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.SwitchActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class Bpel11SwitchActivityState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();

  Bpel11SwitchActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    return new SwitchActivityImpl();
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_CASE) {
      Bpel11CaseState c = (Bpel11CaseState)pn;
      ((SwitchActivityImpl)getActivity()).addCase(c.getExpression(),c.getActivity());
    } else if (pn.getType() == BPEL11_OTHERWISE){
      ((SwitchActivityImpl)getActivity()).addCase(
          null,((Bpel11OtherwiseState)pn).getActivity());      
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
    return BPEL11_SWITCH;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11SwitchActivityState(se,pc);
    }
  }
}
