/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.impl.nodes.WhileActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
