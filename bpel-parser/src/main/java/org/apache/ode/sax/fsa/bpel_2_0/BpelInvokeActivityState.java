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
import org.apache.ode.bom.impl.nodes.InvokeActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelInvokeActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelInvokeActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    InvokeActivityImpl iai = new InvokeActivityImpl();
    XmlAttributes atts = se.getAttributes();
    iai.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    iai.setPartnerLink(atts.getValue("partnerLink"));
    iai.setOperation(atts.getValue("operation"));
    iai.setInputVar(atts.getValue("inputVariable"));
    iai.setOutputVar(atts.getValue("outputVariable"));
    return iai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    InvokeActivityImpl iai = (InvokeActivityImpl) getActivity();
    switch (pn.getType()) {
    case BaseBpelState.BPEL_CORRELATIONS:
      for (Iterator<Correlation> it = ((BpelCorrelationsState)pn).getCorrelations();it.hasNext();) {
        iai.addCorrelation(it.next());
      }
      break;
    case BaseBpelState.BPEL_CATCH:
      iai.getFaultHandler().addCatch(((BpelCatchState)pn).getCatch());
      break;
    case BaseBpelState.BPEL_CATCHALL:
      iai.getFaultHandler().addCatch(((BpelCatchAllState)pn).getCatch());
      break;
    case BaseBpelState.BPEL_COMPENSATIONHANDLER:
      iai.setCompensationHandler(((BpelCompensationHandlerState)pn).getCompensationHandler());
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
    return BPEL_INVOKE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelInvokeActivityState(se,pc);
    }
  }
}
