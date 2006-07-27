/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.OnEvent;
import org.apache.ode.bom.impl.nodes.OnEventImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelOnEventState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private OnEventImpl _o;
  
  BpelOnEventState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _o = new OnEventImpl();
    _o.setNamespaceContext(se.getNamespaceContext());
    _o.setLineNo(se.getLocation().getLineNumber());
    _o.setPartnerLink(atts.getValue("partnerLink"));
    _o.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    _o.setOperation(atts.getValue("operation"));
    _o.setMessageExchangeId(atts.getValue("messageExchange"));
    _o.setMessageType(se.getNamespaceContext().derefQName(atts.getValue("messageType")));
    if (atts.hasAtt("variable")) {
      _o.setVariable(atts.getValue("variable"));
    }
    if (atts.hasAtt("element")) {
      
    }
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
  
  public OnEvent getOnEventHandler() {
    return _o;
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
    return BPEL_ONEVENT;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelOnEventState(se,pc);
    }
  }
}
