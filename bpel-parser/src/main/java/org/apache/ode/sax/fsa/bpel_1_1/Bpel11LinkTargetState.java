/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.LinkTarget;
import org.apache.ode.bom.impl.nodes.LinkTargetImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class Bpel11LinkTargetState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkTargetImpl _t;
  
  
  Bpel11LinkTargetState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _t = new LinkTargetImpl();
    _t.setNamespaceContext(se.getNamespaceContext());
    _t.setLineNo(se.getLocation().getLineNumber());
    _t.setLinkName(atts.getValue("linkName"));
  }
  
  public LinkTarget getTarget() {
    return _t;
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
    return BPEL11_TARGET;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11LinkTargetState(se,pc);
    }
  }
}
