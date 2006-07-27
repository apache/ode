/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Link;
import org.apache.ode.bom.impl.nodes.LinkImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class Bpel11LinkState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkImpl _link;
  
  Bpel11LinkState(StartElement se, ParseContext pc) throws ParseException {
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
    return BPEL11_LINK;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11LinkState(se,pc);
    }
  }
}
