/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.From;
import com.fs.pxe.bom.api.LiteralVal;
import com.fs.pxe.bom.impl.nodes.*;
import com.fs.pxe.bpel.parser.BpelProcessBuilder;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.SaxEvent;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;
import com.fs.utils.NSContext;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

class BpelFromState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private From _f;
  
  private XmlAttributeSpec PLINK_EP = new FilterSpec(
      new String[] {"partnerLink","endpointReference"},new String[] {});

  private XmlAttributeSpec VAR = new FilterSpec(
      new String[] {"variable"},
      new String[] {"part"});

  private XmlAttributeSpec VAR_PROP = new FilterSpec(
      new String[] {"variable","property"},new String[] {});
  
  private NSContext _nsctx;
  private String _expressionLanguage;
  private Locator _locator;
  private ExpressionImpl _expr;
  private DOMGenerator _domGenerator;
  private StartElement _se;


  BpelFromState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _se = se;
    _locator = se.getLocation();
    _nsctx = se.getNamespaceContext();
    XmlAttributes atts = se.getAttributes();
    _expressionLanguage = atts.getValue("expressionLanguage");
    if (VAR_PROP.matches(atts)) {
      PropertyValImpl pvi = new PropertyValImpl(se.getNamespaceContext());
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setVariable(atts.getValue("variable"));
      pvi.setProperty(se.getNamespaceContext().derefQName(atts.getValue("property")));
      _f = pvi;
    } else if (VAR.matches(atts)) {
      VariableValImpl vvi = new VariableValImpl();
      vvi.setLineNo(se.getLocation().getLineNumber());
      vvi.setVariable(atts.getValue("variable"));
      vvi.setPart(atts.getValue("part"));
      _f = vvi;
    } else if (PLINK_EP.matches(atts)) {
      PartnerLinkValImpl pvi = new PartnerLinkValImpl();
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setPartnerLink(atts.getValue("partnerLink"));
      pvi.setEndpointReference(atts.getValue("endpointReference"));
      _f = pvi;
    } else {
      _domGenerator = new DOMGenerator();
    }
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleSaxEvent(com.fs.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (_domGenerator == null) {
      getParseContext().parseError(ParseError.ERROR,se,"", "Unexpected content in <from> element.");
      
    }
    // if we're here, then we're parsing a literal
    _domGenerator.handleSaxEvent(se);
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#done()
   */
  public void done(){
  	if(_domGenerator != null && _f == null){
  		if(_domGenerator.getRoot() instanceof Element
              && ((Element)_domGenerator.getRoot()).getLocalName().equals("literal")
              && ((Element)_domGenerator.getRoot()).getNamespaceURI() != null
              && ((Element)_domGenerator.getRoot()).getNamespaceURI().equals(BpelProcessBuilder.WSBPEL2_0_NS)){
	  		LiteralVal literal = new LiteralValImpl(_nsctx);
	      literal.setLiteral((Element)_domGenerator.getRoot());
	      _f = literal;
      }else{
        if(_expressionLanguage != null){
          _expr = new ExpressionImpl(_expressionLanguage);
        }else{
          _expr = new ExpressionImpl();
        }

      _expr.setNamespaceContext(_se.getNamespaceContext());
      _expr.setLineNo(_se.getLocation().getLineNumber());
  			_expr.setNode(_domGenerator.getRoot());
  			ExpressionValImpl evi = new ExpressionValImpl(_expr.getNamespaceContext());
        evi.setExpression(_expr);
        _f = evi;
  		}
    }
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    switch(pn.getType()){  
      case BPEL_SERVICE_REF:
        // TODO: Implement assignments from a service-ref.
        getParseContext().parseError(ParseError.ERROR,_f,"",
            "Assignment from a service reference is not currently supported.");
        break;
      default:
        super.handleChildCompleted(pn);
    }
  }
  
  public From getFrom() {
    return _f;
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
    return BPEL_FROM;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelFromState(se,pc);
    }
  }
}
