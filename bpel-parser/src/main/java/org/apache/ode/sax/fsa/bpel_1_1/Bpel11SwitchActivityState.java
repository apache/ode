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

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.SwitchActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class Bpel11SwitchActivityState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();

  Bpel11SwitchActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    return new SwitchActivityImpl();
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_CASE) {
      Bpel11CaseState c = (Bpel11CaseState)pn;
      ((SwitchActivityImpl)getActivity()).addCase(c.getExpression(),c.getActivity());
    } else if (pn.getType() == BPEL11_OTHERWISE){
      ((SwitchActivityImpl)getActivity()).addCase(
          null,((Bpel11OtherwiseState)pn).getActivity());      
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
    return BPEL11_SWITCH;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11SwitchActivityState(se,pc);
    }
  }
}
