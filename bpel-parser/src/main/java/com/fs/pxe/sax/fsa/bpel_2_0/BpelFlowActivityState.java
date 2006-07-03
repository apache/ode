/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Link;
import com.fs.pxe.bom.impl.nodes.FlowActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

import java.util.Iterator;

class BpelFlowActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelFlowActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    return new FlowActivityImpl();
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      ((FlowActivityImpl)getActivity()).addChild(((ActivityStateI)pn).getActivity());
    } else if (pn.getType() == BaseBpelState.BPEL_LINKS){
      FlowActivityImpl fai = (FlowActivityImpl) getActivity();
      for (Iterator<Link> it = ((BpelLinksState)pn).getLinks();it.hasNext();) {
        fai.addLink(it.next());
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
    return BPEL_FLOW;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelFlowActivityState(se, pc);
    }
  }

}
