/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.impl.nodes.CorrelationImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

class Bpel11CorrelationState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CorrelationImpl _c;
  
  Bpel11CorrelationState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _c = new CorrelationImpl();
    _c.setNamespaceContext(se.getNamespaceContext());
    _c.setLineNo(se.getLocation().getLineNumber());
    _c.setCorrelationSet(atts.getValue("set"));
    _c.setInitiate(getInitiateYesNo(atts));
    if (atts.hasAtt("pattern")) {
      String pat = atts.getValue("pattern");
      if (pat.equals("out")) {
        _c.setPattern(Correlation.CORRPATTERN_OUT);
      } else if (pat.equals("in")) {
        _c.setPattern(Correlation.CORRPATTERN_IN);
      } else if (pat.equals("out-in")) {
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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL11_CORRELATION;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11CorrelationState(se,pc);
    }
  }
}
