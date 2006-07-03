/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.To;
import com.fs.pxe.bom.impl.nodes.*;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.SaxEvent;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class BpelToState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private To _t;
  
  private XmlAttributeSpec PLINK = new FilterSpec(
      new String[] {"partnerLink"},new String[] {});

  private XmlAttributeSpec VAR = new FilterSpec(
        new String[] {"variable"},
        new String[] {"part"});

  private XmlAttributeSpec VAR_PROP = new FilterSpec(
      new String[] {"variable","property"},new String[] {});

  private StartElement _se;
  private String _queryLanguage;
  private DOMGenerator _domBuilder;
  private ExpressionImpl _expr;

  BpelToState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _se = se;
    XmlAttributes atts = se.getAttributes();
    
    _queryLanguage = atts.getValue("queryLanguage");
    
    if (VAR_PROP.matches(atts)) {
      PropertyValImpl pvi = new PropertyValImpl(se.getNamespaceContext());
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setVariable(atts.getValue("variable"));
      pvi.setProperty(se.getNamespaceContext().derefQName(atts.getValue("property")));
      _t = pvi;
    } if (VAR.matches(atts)) {
      VariableValImpl vvi = new VariableValImpl();
      vvi.setLineNo(se.getLocation().getLineNumber());
      vvi.setVariable(atts.getValue("variable"));
      vvi.setPart(atts.getValue("part"));
      _t = vvi;
    } else if (PLINK.matches(atts)) {
    PartnerLinkValImpl pvi = new PartnerLinkValImpl();
    pvi.setLineNo(se.getLocation().getLineNumber());
    pvi.setPartnerLink(atts.getValue("partnerLink"));
    _t = pvi;
    } else {
      if (_queryLanguage != null)
        _expr = new ExpressionImpl(_queryLanguage);
      else
        _expr = new ExpressionImpl();
      
			_expr.setNamespaceContext(_se.getNamespaceContext());
			_expr.setLineNo(_se.getLocation().getLineNumber());
    	_domBuilder = new DOMGenerator();
    }
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleSaxEvent(com.fs.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (_domBuilder == null) {
      getParseContext().parseError(ParseError.ERROR,se,"","Unexpected content in the <to> element.");
      assert false;
    }
    _domBuilder.handleSaxEvent(se);
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#done()
   */
  public void done(){
  	if(_domBuilder != null && _t == null){
			_expr.setNode(_domBuilder.getRoot());
			ExpressionValImpl evi = new ExpressionValImpl(_expr.getNamespaceContext());
      evi.setExpression(_expr);
      _t = evi;
    }
  }
  
  
  public To getTo() {
    return _t;
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
    return BPEL_TO;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelToState(se,pc);
    }
  }
}
