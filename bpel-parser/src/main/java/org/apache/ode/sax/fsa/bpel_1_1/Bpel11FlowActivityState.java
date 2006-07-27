/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import java.util.Iterator;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Link;
import org.apache.ode.bom.impl.nodes.FlowActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

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
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
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
