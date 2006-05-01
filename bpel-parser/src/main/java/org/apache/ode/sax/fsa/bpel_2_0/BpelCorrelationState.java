/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.impl.nodes.CorrelationImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class BpelCorrelationState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CorrelationImpl _c;
  
  BpelCorrelationState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _c = new CorrelationImpl();
    _c.setNamespaceContext(se.getNamespaceContext());
    _c.setLineNo(se.getLocation().getLineNumber());
    _c.setCorrelationSet(atts.getValue("set"));
    _c.setInitiate(getInitiateYesNo(atts));
    if (atts.hasAtt("pattern")) {
      String pat = atts.getValue("pattern");
      if (pat.equals("out") || pat.equals("response")) {
        _c.setPattern(Correlation.CORRPATTERN_OUT);
      } else if (pat.equals("in") || pat.equals("request")) {
        _c.setPattern(Correlation.CORRPATTERN_IN);
      } else if (pat.equals("out-in") || pat.equals("request-response")) {
        _c.setPattern(Correlation.CORRPATTERN_INOUT);
      } else {
      	throw new IllegalStateException("Bad correlation pattern: " + pat);
      }
    }

  }
  
  public Correlation getCorrelation() {
    return _c;
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
    return BPEL_CORRELATION;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCorrelationState(se,pc);
    }
  }
}
