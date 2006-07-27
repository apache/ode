/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.*;
import org.apache.ode.bom.impl.nodes.ScopeActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

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
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
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
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
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
