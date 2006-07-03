/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.Collections;
import java.util.Iterator;

import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;

class Bpel11PartnersState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
   
  private Bpel11PartnersState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    pc.parseError(ParseError.WARNING,se,
        "PARSER_WARNING","The <partners> element is ignored and should be omitted.");
    // TODO: Internationalize
  }

  public Iterator getPartners() {
    return Collections.emptyList().iterator();
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_PARTNER) {
      // ignore
    } else {
      super.handleChildCompleted(pn);
    }
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
    return BPEL11_PARTNERS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11PartnersState(se,pc);
    }
  }
}
