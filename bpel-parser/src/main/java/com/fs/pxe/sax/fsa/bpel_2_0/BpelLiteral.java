/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.SaxEvent;
import com.fs.sax.evt.StartElement;

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
   * @see com.fs.pxe.sax.fsa.State#handleSaxEvent(com.fs.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    _domGenerator.handleSaxEvent(se);
  }
  /**
   * @see com.fs.pxe.sax.fsa.State#done()
   */
  public void done(){
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
    return BPEL_LITERAL;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLiteral(se,pc);
    }
  }
}
