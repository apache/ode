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
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.*;
import org.apache.ode.bom.api.Process;
import org.apache.ode.bom.impl.nodes.ProcessImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

import java.net.URL;
import java.util.Iterator;

class BpelProcessState extends BaseBpelState implements org.apache.ode.bpel.parser.BpelProcessState {

  private static final StateFactory _factory = new Factory();
    
  // TODO: still needed?
  private static final XmlAttributeSpec spec = new FilterSpec(
        new String[] {"name","targetNamespace"},
        new String[] {"queryLanguage","expressionLanguage","suppressJoinFailure",
            "abstractProcess"}
      );
  
  private Process _process;
  
  BpelProcessState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    String name = atts.getValue("name");
    String tns = atts.getValue("targetNamespace");
    // TODO: still needed?
    short suppressJoinFailure;
    // TODO: Come back and fix this!
    URL url = null;
    
    _process = new ProcessImpl();
    _process.setBpelVersion(Process.BPEL_V200);
    _process.setNamespaceContext(se.getNamespaceContext());
    _process.setName(name);
    _process.setTargetNamespace(tns);
    _process.setSource(se.getLocation().getSystemId());
    _process.setSuppressJoinFailure(getSuppressJoinFailure(atts));
    if (atts.hasAtt("queryLanguage")) {
      _process.setQueryLanguage(atts.getValue("queryLanguage"));
    }
    /*
     * NOTE: Xerces automatically fills in the default value of the attribute for us,
     * in this case "no" for enableInstanceCompensation and abstractProcess.  Thus,
     * we can't warn if it's present because we don't know if the user or the parser
     * put it in...
     */
    if (atts.hasAtt("enableInstanceCompensation") && checkYesNo(atts.getValue("enableInstanceCompensation"))) {
      pc.parseError(ParseError.WARNING,se,
          "","Instance compensation is not well-defined; it will not be used at runtime.");
    }
    if (atts.hasAtt("abstractProcess") && checkYesNo(atts.getValue("abstractProcess"))) {
      pc.parseError(ParseError.FATAL,se,
          "","ODE does not support the parsing or compilation of abstract processes.");
      // TODO: Get a real error key.
    }
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_IMPORT:
      _process.addImport(((BpelImportState)pn).getImport());
      break;
    case BPEL_PARTNERLINKS:
      for (Iterator<PartnerLink> it = ((BpelPartnerLinksState)pn).getPartnerLinks();it.hasNext();) {
        _process.addPartnerLink(it.next());
      }
      break;
    case BPEL_PARTNERS:
      throw new IllegalStateException("" + BPEL_PARTNERS);
    case BPEL_VARIABLES:
      for (Iterator<Variable> it = ((BpelVariablesState)pn).getVariables();it.hasNext();) {
        _process.addVariable(it.next());
      }
      break;
    case BPEL_CORRELATIONSETS:
      for (Iterator<CorrelationSet> it = ((BpelCorrelationSetsState)pn).getCorrelationSets();it.hasNext();) {
        _process.addCorrelationSet(it.next());
      }
      break;
    case BPEL_FAULTHANDLERS:
      _process.setFaultHandler(((BpelFaultHandlersState)pn).getFaultHandler());
      break;
    case BPEL_TERMINATIONHANDLER:
      _process.setTerminationHandler(((BpelTerminationHandlerState)pn).getTerminationHandler());
      break;
    case BPEL_EVENTHANDLERS:
      for (Iterator<OnEvent> it = ((BpelEventHandlersState)pn).getOnEventHandlers();it.hasNext();) {
        _process.addOnEventHandler(it.next());
      }
      for (Iterator<OnAlarm> it = ((BpelEventHandlersState)pn).getOnAlarmHandlers();it.hasNext();) {
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
    return BPEL_PROCESS;
  }
  
  public static class Factory implements StateFactory {
    

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelProcessState(se,pc);
    }
  }
}
