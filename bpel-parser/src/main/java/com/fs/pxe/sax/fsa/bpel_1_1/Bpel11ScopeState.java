/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.Iterator;

import com.fs.pxe.bom.api.*;
import com.fs.pxe.bom.impl.nodes.ScopeActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

class Bpel11ScopeState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();
  
  Bpel11ScopeState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }

  protected Activity createActivity(StartElement se) {
    ScopeActivityImpl sai = new ScopeActivityImpl();
    XmlAttributes atts = se.getAttributes();
    if (atts.hasAtt("variableAccessSerializable")) {
      sai.setVariableAccessSerialized(
          checkYesNo(atts.getValue("variableAccessSerializable")));
    }
    return sai;
  }  
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    ScopeActivityImpl scope = (ScopeActivityImpl)getActivity();
    switch (pn.getType()) {
    case BPEL11_VARIABLES:
      for (Iterator it = ((Bpel11VariablesState)pn).getVariables();it.hasNext();) {
        scope.addVariable((Variable)it.next());
      }
      break;
    case BPEL11_CORRELATIONSETS:
      for (Iterator it = ((Bpel11CorrelationSetsState)pn).getCorrelationSets();it.hasNext();) {
        scope.addCorrelationSet((CorrelationSet)it.next());
      }
      break;
    case BPEL11_FAULTHANDLERS:
      scope.setFaultHandler(((Bpel11FaultHandlersState)pn).getFaultHandler());
      break;
    case BPEL11_COMPENSATIONHANDLER:
      scope.setCompensationHandler(((Bpel11CompensationHandlerState)pn).getCompensationHandler());
      break;
    case BPEL11_EVENTHANDLERS:
      for (Iterator<OnEvent> it = ((Bpel11EventHandlersState)pn).getOnEventHandlers();it.hasNext();) {
        scope.addOnEventHandler(it.next());
      }
      for (Iterator<OnAlarm> it = ((Bpel11EventHandlersState)pn).getOnAlarmHandlers();it.hasNext();) {
        scope.addOnAlarmEventHandler(it.next());
      }      
      break;
    default:
      if (pn instanceof ActivityStateI) {
        scope.setChildActivity(((ActivityStateI)pn).getActivity()); 
      } else {
        super.handleChildCompleted(pn);
      }
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
    return BPEL11_SCOPE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11ScopeState(se,pc);
    }
  }

}
