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

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.ReceiveActivity;
import org.apache.ode.bom.impl.nodes.ReceiveActivityImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelReceiveActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelReceiveActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    ReceiveActivityImpl rai = new ReceiveActivityImpl();
    XmlAttributes atts = se.getAttributes();
    if (atts.hasAtt("portType")) {
      rai.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    }
    rai.setPartnerLink(atts.getValue("partnerLink"));
    rai.setOperation(atts.getValue("operation"));
    rai.setVariable(atts.getValue("variable"));
    rai.setMessageExchangeId(atts.getValue("messageExchange"));
    
    if (atts.hasAtt("createInstance")) {
      rai.setCreateInstance(checkYesNo(atts.getValue("createInstance")));
    }
    return rai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_CORRELATIONS) {
      for (Iterator<Correlation> it = ((BpelCorrelationsState)pn).getCorrelations();it.hasNext();) {
        Correlation c = it.next();
        if (c.getPattern() != Correlation.CORRPATTERN_IN) {
          getParseContext().parseError(ParseError.WARNING,
              c,"","Only the \"in\" pattern makes sense for a correlation on a <receive>.");
          // TODO: Get an error key here.
        }
        c.setPattern(Correlation.CORRPATTERN_IN);
        ((ReceiveActivity)getActivity()).addCorrelation(c);
      }
    } else {
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
    return BPEL_RECEIVE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelReceiveActivityState(se,pc);
    }
  }
}
