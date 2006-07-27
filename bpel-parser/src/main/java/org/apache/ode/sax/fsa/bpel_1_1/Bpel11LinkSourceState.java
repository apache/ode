/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
