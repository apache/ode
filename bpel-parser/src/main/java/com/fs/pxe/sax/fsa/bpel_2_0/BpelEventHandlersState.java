/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.OnAlarm;
import com.fs.pxe.bom.api.OnEvent;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

import java.util.ArrayList;
import java.util.Iterator;

class BpelEventHandlersState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<OnEvent> _e;
  private ArrayList<OnAlarm> _a;
   
  BpelEventHandlersState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _e = new ArrayList<OnEvent>();
    _a = new ArrayList<OnAlarm>();
  }
  
  public Iterator<OnEvent> getOnEventHandlers() {
    return _e.iterator();
  }
  
  public Iterator<OnAlarm> getOnAlarmHandlers() {
    return _a.iterator();
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_ONEVENT:
      _e.add(((BpelOnEventState)pn).getOnEventHandler());
      break;
    case BPEL_ONALARM:
      _a.add(((BpelOnAlarmState)pn).getOnAlarmHandler());
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
    return BPEL_EVENTHANDLERS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelEventHandlersState(se,pc);
    }
  }
}
