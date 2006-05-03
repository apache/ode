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

import org.apache.ode.bom.api.Link;
import org.apache.ode.bom.impl.nodes.LinkImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class BpelLinkState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkImpl _link;
  
  BpelLinkState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _link = new LinkImpl();
    _link.setNamespaceContext(se.getNamespaceContext());
    _link.setLineNo(se.getLocation().getLineNumber());
    _link.setLinkName(atts.getValue("name"));
  }
  
  public Link getLink() {
    return _link;
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
    return BPEL_LINK;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLinkState(se,pc);
    }
  }
}
