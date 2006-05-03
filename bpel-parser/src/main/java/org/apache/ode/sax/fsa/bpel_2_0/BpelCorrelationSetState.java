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
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.CorrelationSet;
import org.apache.ode.bom.impl.nodes.CorrelationSetImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.utils.NSContext;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

class BpelCorrelationSetState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CorrelationSetImpl _s;

  BpelCorrelationSetState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _s = new CorrelationSetImpl();
    _s.setNamespaceContext(se.getNamespaceContext());
    _s.setLineNo(se.getLocation().getLineNumber());
    _s.setName(atts.getValue("name"));
    if (atts.hasAtt("properties")) {
      StringTokenizer st = new StringTokenizer(atts.getValue("properties"));
      ArrayList<QName> al = new ArrayList<QName>();
      NSContext nsc = se.getNamespaceContext();
      for (;st.hasMoreTokens();) {
        al.add(nsc.derefQName(st.nextToken()));
      }
      _s.setProperties(al.toArray(new QName[] {}));
    }
    
  }
  
  public CorrelationSet getCorrelationSet() {
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
    return BPEL_CORRELATIONSET;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCorrelationSetState(se,pc);
    }
  }
}
