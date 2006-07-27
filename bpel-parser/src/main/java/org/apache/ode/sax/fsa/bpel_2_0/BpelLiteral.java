/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;

import org.w3c.dom.Node;

class BpelLiteral extends BaseBpelState {

  private static final StateFactory _factory = new Factory();

	private DOMGenerator _domGenerator;

  BpelLiteral(StartElement se,ParseContext pc) throws ParseException {
    super(pc);
    _domGenerator = new DOMGenerator();
  }

  Node getLiteral(){
    return _domGenerator.getRoot();
  }

  /**
   * @see org.apache.ode.sax.fsa.State#handleSaxEvent(org.apache.ode.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    _domGenerator.handleSaxEvent(se);
  }
  /**
   * @see org.apache.ode.sax.fsa.State#done()
   */
  public void done(){
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
    return BPEL_LITERAL;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLiteral(se,pc);
    }
  }
}
