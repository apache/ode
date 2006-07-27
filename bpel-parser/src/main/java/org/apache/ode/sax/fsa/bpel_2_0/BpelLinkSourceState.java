/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.LinkSource;
import org.apache.ode.bom.impl.nodes.LinkSourceImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class BpelLinkSourceState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkSourceImpl _s;

  BpelLinkSourceState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _s = new LinkSourceImpl();
    _s.setNamespaceContext(se.getNamespaceContext());
    _s.setLineNo(se.getLocation().getLineNumber());
    _s.setLinkName(atts.getValue("linkName"));
  }
  
  
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_EXPRESSION:
      _s.setTransitionCondition(((BpelExpressionState)pn).getExpression());
      break;
    default:
      super.handleChildCompleted(pn);
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
    return BPEL_SOURCE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLinkSourceState(se,pc);
    }
  }
}
