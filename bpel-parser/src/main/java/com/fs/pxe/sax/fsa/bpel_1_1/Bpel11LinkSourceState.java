/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.LinkSource;
import com.fs.pxe.bom.impl.nodes.ExpressionImpl;
import com.fs.pxe.bom.impl.nodes.LinkSourceImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
