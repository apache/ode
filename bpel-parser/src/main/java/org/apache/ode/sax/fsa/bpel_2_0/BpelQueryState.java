/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Query;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class BpelQueryState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ExpressionImpl _expr;
  private DOMGenerator _domGenerator;
  
  BpelQueryState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    
    XmlAttributes attr = se.getAttributes();
    if(attr.hasAtt("queryLanguage")){
      _expr = new ExpressionImpl(attr.getValue("queryLanguage"));
    }else{
      _expr = new ExpressionImpl();
    }
    _expr.setNamespaceContext(se.getNamespaceContext());
    _domGenerator = new DOMGenerator();
  }
  
  Query getQuery(){
    return _expr;
  }
  
	/**
	 * @see org.apache.ode.sax.fsa.State#handleSaxEvent(org.apache.ode.sax.evt.SaxEvent)
	 */
	public void handleSaxEvent(SaxEvent se) throws ParseException {
		_domGenerator.handleSaxEvent(se);
	}
  /**
   * @see org.apache.ode.sax.fsa.State#done()
   */
  public void done(){
    _expr.setNode(_domGenerator.getRoot());
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
    return BPEL_QUERY;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelQueryState(se,pc);
    }
  }
}
