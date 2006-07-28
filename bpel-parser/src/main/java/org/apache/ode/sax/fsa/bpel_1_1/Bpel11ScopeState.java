/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import java.util.Iterator;

import org.apache.ode.bom.api.*;
import org.apache.ode.bom.impl.nodes.ScopeActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

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
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
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
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
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
