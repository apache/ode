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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ode.bom.api.OnAlarm;
import org.apache.ode.bom.api.OnEvent;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class Bpel11EventHandlersState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<OnEvent> _m;
  private ArrayList<OnAlarm> _a;
   
  Bpel11EventHandlersState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _m = new ArrayList<OnEvent>();
    _a = new ArrayList<OnAlarm>();
  }
  
  public Iterator<OnEvent> getOnEventHandlers() {
    return _m.iterator();
  }
  
  public Iterator<OnAlarm> getOnAlarmHandlers() {
    return _a.iterator();
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL11_ONMESSAGE:
      _m.add(((Bpel11OnMessageState)pn).getOnEventHandler());
      break;
    case BPEL11_ONALARM:
      _a.add(((Bpel11OnAlarmState)pn).getOnAlarmHandler());
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
    return BPEL11_EVENTHANDLERS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11EventHandlersState(se,pc);
    }
  }
}
