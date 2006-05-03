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

import org.apache.ode.bom.api.LinkSource;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.bom.impl.nodes.LinkSourceImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class Bpel11LinkSourceState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkSourceImpl _s;
  
  
  Bpel11LinkSourceState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _s = new LinkSourceImpl();
    _s.setNamespaceContext(se.getNamespaceContext());
    _s.setLineNo(se.getLocation().getLineNumber());
    _s.setLinkName(atts.getValue("linkName"));
    if(atts.hasAtt("transitionCondition")){
      ExpressionImpl expr = new ExpressionImpl();
      expr.setLineNo(se.getLocation().getLineNumber());
      expr.setNamespaceContext(se.getNamespaceContext());
      expr.setXPathString(atts.getValue("transitionCondition"));
      _s.setTransitionCondition(expr);
    }
  }
  
  public LinkSource getSource() {
    return _s;
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
    return BPEL11_SOURCE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11LinkSourceState(se,pc);
    }
  }
}
