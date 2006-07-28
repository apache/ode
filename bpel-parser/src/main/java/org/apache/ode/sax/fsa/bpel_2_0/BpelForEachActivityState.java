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
import org.apache.ode.bom.api.ScopeActivity;
import org.apache.ode.bom.impl.nodes.ForEachActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

public class BpelForEachActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new BpelProcessState.Factory();


  BpelForEachActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }

  protected Activity createActivity(StartElement se) {
    XmlAttributes atts = se.getAttributes();
    ForEachActivityImpl forEach = new ForEachActivityImpl();
    forEach.setCounterName(atts.getValue("counterName"));
    forEach.setParallel(checkYesNo(atts.getValue("parallel")));
    return forEach;
  }

  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof BpelScopeState) {
      ((ForEachActivityImpl)getActivity()).setScope((ScopeActivity) ((BpelScopeState)pn).getActivity());
    } else if (pn.getType() == BPEL_START_COUNTER_VALUE) {
      ((ForEachActivityImpl)getActivity()).setStartCounter(((BpelStartCounterValueState)pn).getExpression());
    } else if (pn.getType() == BPEL_FINAL_COUNTER_VALUE) {
      ((ForEachActivityImpl)getActivity()).setFinalCounter(((BpelFinalCounterValueState)pn).getExpression());
    } else if (pn.getType() == BPEL_ITERATOR) {
      ((ForEachActivityImpl)getActivity()).setStartCounter(((BpelIteratorState)pn).getStartCounterValue());
      ((ForEachActivityImpl)getActivity()).setFinalCounter(((BpelIteratorState)pn).getFinalCounterValue());
    } else if (pn.getType() == BPEL_COMPLETION_CONDITION) {
      ((ForEachActivityImpl)getActivity())
              .setCompletionCondition(((BpelCompletionConditionState)pn).getCompletionCondition());
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
    return BPEL_FOREACH;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelForEachActivityState(se,pc);
    }
  }

}
