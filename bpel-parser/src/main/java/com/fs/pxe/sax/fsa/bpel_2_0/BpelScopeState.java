/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.*;
import com.fs.pxe.bom.impl.nodes.ScopeActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelScopeState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();
  
  BpelScopeState(StartElement se, ParseContext pc) throws ParseException {
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
    case BPEL_VARIABLES:
      for (Iterator<Variable> it = ((BpelVariablesState)pn).getVariables();it.hasNext();) {
        scope.addVariable(it.next());
      }
      break;
    case BPEL_CORRELATIONSETS:
      for (Iterator<CorrelationSet> it = ((BpelCorrelationSetsState)pn).getCorrelationSets();it.hasNext();) {
        scope.addCorrelationSet(it.next());
      }
      break;
    case BPEL_PARTNERLINKS:
      for (Iterator<PartnerLink> it = ((BpelPartnerLinksState)pn).getPartnerLinks();it.hasNext();) {
        scope.addPartnerLink(it.next());
      }
      break;
    case BPEL_FAULTHANDLERS:
      scope.setFaultHandler(((BpelFaultHandlersState)pn).getFaultHandler());
      break;
    case BPEL_COMPENSATIONHANDLER:
      scope.setCompensationHandler(((BpelCompensationHandlerState)pn).getCompensationHandler());
      break;
    case BPEL_EVENTHANDLERS:
      for (Iterator<OnEvent> it = ((BpelEventHandlersState)pn).getOnEventHandlers();it.hasNext();) {
        scope.addOnEventHandler(it.next());
      }
      for (Iterator<OnAlarm> it = ((BpelEventHandlersState)pn).getOnAlarmHandlers();it.hasNext();) {
        scope.addOnAlarmEventHandler(it.next());
      }      
      break;
    case BPEL_TERMINATIONHANDLER:
      scope.setTerminationHandler(((BpelTerminationHandlerState)pn).getTerminationHandler());
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
    return BPEL_SCOPE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelScopeState(se,pc);
    }
  }

}
