/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.ArrayList;
import java.util.Iterator;

import com.fs.pxe.bom.api.OnAlarm;
import com.fs.pxe.bom.api.OnEvent;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

class Bpel11EventHandlersState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<OnEvent> _m;
  private ArrayList<OnAlarm> _a;
   
  Bpel11EventHandlersState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _m = new ArrayList<OnEvent>();
    _a = new ArrayList<OnAlarm>();
  }
  
  public Iterator<OnEvent> getOnEventHandlers() {
    return _m.iterator();
  }
  
  public Iterator<OnAlarm> getOnAlarmHandlers() {
    return _a.iterator();
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL11_ONMESSAGE:
      _m.add(((Bpel11OnMessageState)pn).getOnEventHandler());
      break;
    case BPEL11_ONALARM:
      _a.add(((Bpel11OnAlarmState)pn).getOnAlarmHandler());
      break;
    default:
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
    return BPEL11_EVENTHANDLERS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11EventHandlersState(se,pc);
    }
  }
}
