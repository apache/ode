/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.Iterator;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Link;
import com.fs.pxe.bom.impl.nodes.FlowActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

class Bpel11FlowActivityState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();

  Bpel11FlowActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    return new FlowActivityImpl();
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      ((FlowActivityImpl)getActivity()).addChild(((ActivityStateI)pn).getActivity());
    } else if (pn.getType() == BaseBpelState.BPEL11_LINKS){
      FlowActivityImpl fai = (FlowActivityImpl) getActivity();
      for (Iterator it = ((Bpel11LinksState)pn).getLinks();it.hasNext();) {
        fai.addLink((Link)it.next());
      }
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
    return BPEL11_FLOW;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11FlowActivityState(se,pc);
    }
  }

}
