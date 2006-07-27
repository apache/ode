/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.WhileActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelWhileActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelWhileActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    WhileActivityImpl wai = new WhileActivityImpl();
    return wai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if(pn instanceof BpelExpressionState){
    	((WhileActivityImpl)getActivity()).setCondition(((BpelExpressionState)pn).getExpression());
    }else if (pn instanceof ActivityStateI) {
      ((WhileActivityImpl)getActivity()).setActivity(((ActivityStateI)pn).getActivity());
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
    return BPEL_WHILE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelWhileActivityState(se,pc);
    }
  }

}
