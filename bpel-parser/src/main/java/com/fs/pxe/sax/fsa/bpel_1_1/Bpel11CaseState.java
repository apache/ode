/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.impl.nodes.ExpressionImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
