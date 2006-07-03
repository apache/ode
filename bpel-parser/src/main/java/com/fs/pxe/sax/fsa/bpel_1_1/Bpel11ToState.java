/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.To;
import com.fs.pxe.bom.impl.nodes.ExpressionImpl;
import com.fs.pxe.bom.impl.nodes.PropertyValImpl;
import com.fs.pxe.bom.impl.nodes.VariableValImpl;
import com.fs.pxe.bom.impl.nodes.PartnerLinkValImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class Bpel11ToState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private To _t;
  
  private XmlAttributeSpec VAR = new FilterSpec(
      new String[] {"variable"},new String[] {"part","query"});
  private XmlAttributeSpec PLINK = new FilterSpec(
      new String[] {"partnerLink"},new String[] {});
  private XmlAttributeSpec VAR_PROP = new FilterSpec(
      new String[] {"variable","property"},new String[] {});

  
  Bpel11ToState(StartElement se, ParseContext pc) throws ParseException {
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
      _t = vvi;
    } else if (VAR_PROP.matches(atts)) {
      PropertyValImpl pvi = new PropertyValImpl(se.getNamespaceContext());
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setVariable(atts.getValue("variable"));
      pvi.setProperty(se.getNamespaceContext().derefQName(atts.getValue("property")));
      _t = pvi;
    } else if (PLINK.matches(atts)) {
      PartnerLinkValImpl pvi = new PartnerLinkValImpl();
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setPartnerLink(atts.getValue("partnerLink"));
      _t = pvi;
    } else {
      pc.parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "Unsupported attribute combination " + atts.toString());
      // TODO: Get real error key here.
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
    return BPEL11_TO;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11ToState(se,pc);
    }
  }
}
