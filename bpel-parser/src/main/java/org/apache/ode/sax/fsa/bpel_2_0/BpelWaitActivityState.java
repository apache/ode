/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.WaitActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelWaitActivityState extends BpelBaseActivityState {
  
  private static final StateFactory _factory = new Factory();
  
  BpelWaitActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    return new WaitActivityImpl();
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    WaitActivityImpl wait = (WaitActivityImpl)getActivity();
    switch (pn.getType()) {
    case BPEL_FOR:
      wait.setFor(((BpelExpressionState)pn).getExpression());
      break;
    case BPEL_UNTIL:
      wait.setUntil(((BpelExpressionState)pn).getExpression());
      break;
    default:
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
    return BPEL_WAIT;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelWaitActivityState(se,pc);
    }
  }
}
