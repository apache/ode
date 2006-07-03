/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.api.OnMessage;
import com.fs.pxe.bom.impl.nodes.OnMessageImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelOnMessageState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private OnMessageImpl _o;
  
  BpelOnMessageState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _o = new OnMessageImpl();
    _o.setNamespaceContext(se.getNamespaceContext());
    _o.setLineNo(se.getLocation().getLineNumber());
    _o.setPartnerLink(atts.getValue("partnerLink"));
    _o.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    _o.setOperation(atts.getValue("operation"));
    _o.setMessageExchangeId(atts.getValue("messageExchange"));
    if (atts.hasAtt("variable"))
      _o.setVariable(atts.getValue("variable"));
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      _o.setActivity(((ActivityStateI)pn).getActivity());
    } else if (pn.getType() == BaseBpelState.BPEL_CORRELATIONS){
      for (Iterator<Correlation> it = ((BpelCorrelationsState)pn).getCorrelations();it.hasNext();) {
        Correlation c = it.next();
        if (c.getPattern() != Correlation.CORRPATTERN_IN) {
          getParseContext().parseError(ParseError.WARNING,
              c,"","Only the \"in\" pattern makes sense for a correlation on an <onMessage>.");
          // TODO: Get an error key here.
        }
        c.setPattern(Correlation.CORRPATTERN_IN);
        _o.addCorrelation(c);
      }
    } else {
      super.handleChildCompleted(pn);
    }
  }
  
  public OnMessage getOnMessageHandler() {
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
    return BPEL_ONMESSAGE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelOnMessageState(se,pc);
    }
  }
}
