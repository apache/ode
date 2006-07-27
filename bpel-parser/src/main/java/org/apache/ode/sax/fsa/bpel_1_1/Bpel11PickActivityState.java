/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.PickActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class Bpel11PickActivityState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();

  Bpel11PickActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) throws ParseException {
    PickActivityImpl pai = new PickActivityImpl();
    XmlAttributes atts = se.getAttributes();
    if (atts.hasAtt("createInstance")) {
      pai.setCreateInstance(checkYesNo(atts.getValue("createInstance")));
    }
    return pai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_ONMESSAGE) {
      ((PickActivityImpl)getActivity()).addOnMessage(
          ((Bpel11OnMessageState)pn).getOnEventHandler());
    } else if (pn.getType() == BPEL11_ONALARM) {
      ((PickActivityImpl)getActivity()).addOnAlarm(
          ((Bpel11OnAlarmState)pn).getOnAlarmHandler());      
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
    return BPEL11_PICK;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11PickActivityState(se,pc);
    }
  }
 
}
