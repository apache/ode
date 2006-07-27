/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class Bpel11CaseState extends Bpel11OtherwiseState {

  private static final StateFactory _factory = new Factory();
    
  private Expression _e;
  
  Bpel11CaseState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
    XmlAttributes atts = se.getAttributes();
    if (atts.hasAtt("condition")) {
    	_e = new ExpressionImpl();
      _e.setNamespaceContext(se.getNamespaceContext());
      _e.setXPathString(atts.getValue("condition"));
      _e.setLineNo(se.getLocation().getLineNumber());
    }
  }
  
  public Expression getExpression() {
    return _e;
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
    return BPEL11_CASE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se,ParseContext pc) throws ParseException {
      return new Bpel11CaseState(se,pc);
    }
  }

}
