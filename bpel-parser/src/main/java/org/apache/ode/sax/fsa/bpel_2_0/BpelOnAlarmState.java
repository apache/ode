/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.OnAlarm;
import org.apache.ode.bom.impl.nodes.OnAlarmImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelOnAlarmState extends BaseBpelState {
  
  private static final StateFactory _factory = new Factory();
  private OnAlarmImpl _o;
  
  BpelOnAlarmState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _o = new OnAlarmImpl();
    
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      _o.setActivity(((ActivityStateI)pn).getActivity());
    } else if (pn instanceof BpelForState){
      _o.setFor(((BpelForState)pn).getExpression());
    } else if (pn instanceof BpelUntilState){
      _o.setUntil(((BpelUntilState)pn).getExpression());
    } else if(pn instanceof BpelRepeatEveryState){
      _o.setRepeatEvery(((BpelRepeatEveryState)pn).getExpression());
    } else {
      super.handleChildCompleted(pn);
    }
    // TODO repeatEvery element
  }
  
  public OnAlarm getOnAlarmHandler() {
    return _o;
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
    return BPEL_ONALARM;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelOnAlarmState(se,pc);
    }
  }
}
