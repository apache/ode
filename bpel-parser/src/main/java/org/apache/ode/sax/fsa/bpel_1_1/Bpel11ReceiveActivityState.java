/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import java.util.Iterator;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.ReceiveActivity;
import org.apache.ode.bom.impl.nodes.ReceiveActivityImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class Bpel11ReceiveActivityState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();

  Bpel11ReceiveActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    ReceiveActivityImpl rai = new ReceiveActivityImpl();
    XmlAttributes atts = se.getAttributes();
    if (atts.hasAtt("portType")) {
      rai.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    }
    rai.setPartnerLink(atts.getValue("partnerLink"));
    rai.setOperation(atts.getValue("operation"));
    rai.setVariable(atts.getValue("variable"));
    if (atts.hasAtt("createInstance")) {
      rai.setCreateInstance(checkYesNo(atts.getValue("createInstance")));
    }
    return rai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_CORRELATIONS) {
      for (Iterator it = ((Bpel11CorrelationsState)pn).getCorrelations();it.hasNext();) {
        Correlation c = (Correlation)it.next();
        if (c.getPattern() != Correlation.CORRPATTERN_IN) {
          getParseContext().parseError(ParseError.WARNING,
              c,"PARSER_WARNING","Only the \"in\" pattern makes sense for a correlation on an onMessage.");
          // TODO: Get an error key here.
        }
        c.setPattern(Correlation.CORRPATTERN_IN);
        ((ReceiveActivity)getActivity()).addCorrelation(c);
      }
    } else {
      super.handleChildCompleted(pn);
    }
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
    return BPEL11_RECEIVE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11ReceiveActivityState(se,pc);
    }
  }
}
