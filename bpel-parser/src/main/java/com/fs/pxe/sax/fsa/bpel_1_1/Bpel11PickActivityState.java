/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.impl.nodes.PickActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
