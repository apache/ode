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

import org.apache.ode.bom.api.OnAlarm;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.bom.impl.nodes.OnAlarmImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

class Bpel11OnAlarmState extends BaseBpelState {

  private static final XmlAttributeSpec FOR = new FilterSpec(
      new String[] {"for"},new String[] {}); 
  private static final XmlAttributeSpec UNTIL = new FilterSpec(
      new String[] {"until"},new String[] {}); 
  
  private static final StateFactory _factory = new Factory();
  private OnAlarmImpl _o;
  
  Bpel11OnAlarmState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    
    _o = new OnAlarmImpl();
    
    ExpressionImpl expr = new ExpressionImpl();
    expr.setNamespaceContext(se.getNamespaceContext());
    expr.setLineNo(se.getLocation().getLineNumber());
    if (FOR.matches(atts)) {
      expr.setXPathString(atts.getValue("for"));
      _o.setFor(expr);
    } else if (UNTIL.matches(atts)) {
      expr.setXPathString(atts.getValue("until"));
      _o.setUntil(expr);
    } else {
      getParseContext().parseError(ParseError.ERROR,se,
          "PARSER_ERROR",
          "Invalid attribute spec for onAlarm; expected @for=\"<duration>\" or @until=\"<datetime>\"");
      // TODO: Error key.
    }
    
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      _o.setActivity(((ActivityStateI)pn).getActivity());
    } else {
      super.handleChildCompleted(pn);
    }
  }
  
  public OnAlarm getOnAlarmHandler() {
    return _o;
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
    return BPEL11_ONALARM;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11OnAlarmState(se,pc);
    }
  }
}
