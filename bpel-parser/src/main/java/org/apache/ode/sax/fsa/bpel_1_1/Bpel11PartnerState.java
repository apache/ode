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

import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;

class Bpel11PartnerState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  
  private Bpel11PartnerState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    pc.parseError(ParseError.WARNING,se,
        "PARSER_WARNING","The <partner> element is ignored and should be omitted.");
    // TODO: Get real error key.
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL11_PARTNERLINK:
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
    return BPEL11_PARTNER;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11PartnerState(se,pc);
    }
  }

}
