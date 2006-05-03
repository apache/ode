/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import java.util.Iterator;

import org.apache.ode.bom.api.*;
import org.apache.ode.bom.api.Process;
import org.apache.ode.bom.impl.nodes.ProcessImpl;
import org.apache.ode.bpel.parser.BpelProcessState;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

class Bpel11ProcessState extends BaseBpelState implements BpelProcessState {

  private static final StateFactory _factory = new Factory();
    
  // TODO still required?
  private static final XmlAttributeSpec spec = new FilterSpec(
        new String[] {"name","targetNamespace"},
        new String[] {"queryLanguage","expressionLanguage","suppressJoinFailure",
            "enableInstanceCompensation","abstractProcess"}
      );
  
  private Process _process;
  
  Bpel11ProcessState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    String name = atts.getValue("name");
    String tns = atts.getValue("targetNamespace");
    // TODO still required?
    short suppressJoinFailure;
    
    _process = new ProcessImpl();
    if (se.getLocation() != null) {
      _process.setSource(se.getLocation().getSystemId());
    }

    _process.setBpelVersion(Process.BPEL_V110);
    _process.setNamespaceContext(se.getNamespaceContext());
    _process.setName(name);
    _process.setTargetNamespace(tns);
    _process.setSuppressJoinFailure(getSuppressJoinFailure(atts));
    if (atts.hasAtt("queryLanguage")) {
      _process.setQueryLanguage(atts.getValue("queryLanguage"));
    }
    if (atts.hasAtt("expressionLanguage")) {
      _process.setExpressionLanguage(atts.getValue("expressionLanguage"));
    }
    /*
     * NOTE: Xerces automatically fills in the default value of the attribute for us,
     * in this case "no" for enableInstanceCompensation and abstractProcess.  Thus,
     * we can't warn if it's present because we don't know if the user or the parser
     * put it in...
     */
    if (atts.hasAtt("enableInstanceCompensation") && checkYesNo(atts.getValue("enableInstanceCompensation"))) {
      pc.parseError(ParseError.WARNING,se,
          "PARSER_WARNING","Instance compensation is not well-defined; it will not be used at runtime.");
    }
    if (atts.hasAtt("abstractProcess") && checkYesNo(atts.getValue("abstractProcess"))) {
      pc.parseError(ParseError.FATAL,se,
          "PARSER_FATAL","PXE does not support the parsing or compilation of abstract processes.");
      // TODO: Internationalize.
    }
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL11_PARTNERLINKS:
      for (Iterator it = ((Bpel11PartnerLinksState)pn).getPartnerLinks();it.hasNext();) {
        _process.addPartnerLink((PartnerLink)it.next());
      }
      break;
    case BPEL11_PARTNERS:
      throw new IllegalStateException("" + BPEL11_PARTNERS);
    case BPEL11_VARIABLES:
      for (Iterator it = ((Bpel11VariablesState)pn).getVariables();it.hasNext();) {
        _process.addVariable((Variable)it.next());
      }
      break;
    case BPEL11_CORRELATIONSETS:
      for (Iterator it = ((Bpel11CorrelationSetsState)pn).getCorrelationSets();it.hasNext();) {
        _process.addCorrelationSet((CorrelationSet)it.next());
      }
      break;
    case BPEL11_FAULTHANDLERS:
      _process.setFaultHandler(((Bpel11FaultHandlersState)pn).getFaultHandler());
      break;
    case BPEL11_COMPENSATIONHANDLER:
      _process.setCompensationHandler(((Bpel11CompensationHandlerState)pn).getCompensationHandler());
      break;
    case BPEL11_EVENTHANDLERS:
      for (Iterator<OnEvent> it = ((Bpel11EventHandlersState)pn).getOnEventHandlers();it.hasNext();) {
        _process.addOnEventHandler(it.next());
      }
      for (Iterator<OnAlarm> it = ((Bpel11EventHandlersState)pn).getOnAlarmHandlers();it.hasNext();) {
        _process.addOnAlarmEventHandler(it.next());
      }      
      break;
    default:
      if (pn instanceof ActivityStateI) {
        _process.setRootActivity(((ActivityStateI)pn).getActivity()); 
      } else {
        super.handleChildCompleted(pn);
      }
    }
  }

  public Process getProcess() {
    return _process;
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
    return BPEL11_PROCESS;
  }
  
  public static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11ProcessState(se,pc);
    }
  }
}
