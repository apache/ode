/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.Iterator;

import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.api.OnEvent;
import com.fs.pxe.bom.impl.nodes.OnEventImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

class Bpel11OnMessageState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private OnEventImpl _o;
  
  Bpel11OnMessageState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _o = new OnEventImpl();
    _o.setNamespaceContext(se.getNamespaceContext());
    _o.setLineNo(se.getLocation().getLineNumber());
    _o.setPartnerLink(atts.getValue("partnerLink"));
    _o.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    _o.setOperation(atts.getValue("operation"));
    if (atts.hasAtt("variable"))
      _o.setVariable(atts.getValue("variable"));
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      _o.setActivity(((ActivityStateI)pn).getActivity());
    } else if (pn.getType() == BaseBpelState.BPEL11_CORRELATIONS){
      for (Iterator it = ((Bpel11CorrelationsState)pn).getCorrelations();it.hasNext();) {
        Correlation c = (Correlation)it.next();
        // force pattern IN
        if (c.getPattern() != Correlation.CORRPATTERN_IN) {
          getParseContext().parseError(ParseError.WARNING,
              c,"PARSER_WARNING","Only the \"in\" pattern makes sense for a correlation on an onMessage.");
          // TODO: Get an error key here.
        }
        c.setPattern(Correlation.CORRPATTERN_IN);
        _o.addCorrelation(c);
      }
    } else {
      super.handleChildCompleted(pn);
    }
  }
  
  public OnEvent getOnEventHandler() {
    return _o;
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
    return BPEL11_ONMESSAGE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11OnMessageState(se,pc);
    }
  }
}
