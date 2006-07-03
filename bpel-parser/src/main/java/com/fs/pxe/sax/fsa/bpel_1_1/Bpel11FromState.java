/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import org.w3c.dom.Element;

import com.fs.pxe.bom.api.From;
import com.fs.pxe.bom.impl.nodes.*;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.SaxEvent;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class Bpel11FromState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private From _f;
  
  private XmlAttributeSpec VAR = new FilterSpec(
      new String[] {"variable"},new String[] {"part","query"});
  private XmlAttributeSpec PLINK_EP = new FilterSpec(
      new String[] {"partnerLink","endpointReference"},new String[] {});
  private XmlAttributeSpec EXPR = new FilterSpec(
      new String[] {"expression"}, new String[] {});
  private XmlAttributeSpec VAR_PROP = new FilterSpec(
      new String[] {"variable","property"},new String[] {});
  // Kludge, perhaps -- no attributes.
  private XmlAttributeSpec LITERAL = new FilterSpec(
      new String[] {},new String[] {});

//  private XmlAttributeSpec VALID = new OrSpec(VAR_PART,
//      new OrSpec(PLINK_EP,
//          new OrSpec(VAR_PROP,LITERAL)));
  
  private boolean _literalMode = false;
  
  private DOMGenerator _domGenerator;
  
  Bpel11FromState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    
    if (VAR.matches(atts)) {
      VariableValImpl vvi = new VariableValImpl();
      vvi.setNamespaceContext(se.getNamespaceContext());
      vvi.setLineNo(se.getLocation().getLineNumber());
      vvi.setVariable(atts.getValue("variable"));
      if(atts.hasAtt("part")){
        vvi.setPart(atts.getValue("part"));
      }
      if(atts.hasAtt("query")){
        ExpressionImpl expr = new ExpressionImpl();
        expr.setXPathString(atts.getValue("query"));
        expr.setLineNo(se.getLocation().getLineNumber());
        expr.setNamespaceContext(se.getNamespaceContext());
        vvi.setLocation(expr);
      }
      _f = vvi;
    } else if (VAR_PROP.matches(atts)) {
      PropertyValImpl pvi = new PropertyValImpl(se.getNamespaceContext());
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setVariable(atts.getValue("variable"));
      pvi.setProperty(se.getNamespaceContext().derefQName(atts.getValue("property")));
      _f = pvi;
    } else if (PLINK_EP.matches(atts)) {
      PartnerLinkValImpl pvi = new PartnerLinkValImpl();
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setPartnerLink(atts.getValue("partnerLink"));
      pvi.setEndpointReference(atts.getValue("endpointReference"));
      _f = pvi;
    } else if (EXPR.matches(atts)) {
      ExpressionValImpl evi = new ExpressionValImpl(se.getNamespaceContext());
      evi.setLineNo(se.getLocation().getLineNumber());
      ExpressionImpl expr = new ExpressionImpl();
      expr.setLineNo(se.getLocation().getLineNumber());
      expr.setNamespaceContext(se.getNamespaceContext());
      expr.setXPathString(atts.getValue("expression"));
      evi.setExpression(expr);
      _f = evi;
    } else if (LITERAL.matches(atts)) {
      _literalMode = true;
      LiteralValImpl lvi = new LiteralValImpl(se.getNamespaceContext());
      _domGenerator = new DOMGenerator();
      _f = lvi;
    } else {
      throw new IllegalStateException("Unknown action." );
    }
  }
  
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (_literalMode) {
      _domGenerator.handleSaxEvent(se);
    } else{
      super.handleSaxEvent(se);
    }
  }
  
  public void done(){
  	if(_literalMode){
  		// For compatibility with 2.0 BPEL, we create a <literal> element
  		Element lit = _domGenerator.getRoot().getOwnerDocument().createElement("literal");
  		lit.appendChild(_domGenerator.getRoot());
      ((LiteralValImpl)_f).setLiteral(lit);   
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
    return BPEL11_FROM;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11FromState(se,pc);
    }
  }
}
