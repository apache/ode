/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.OnAlarm;
import org.apache.ode.bom.api.OnEvent;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

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
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
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
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
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
