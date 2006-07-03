/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa;

import com.fs.sax.evt.Characters;
import com.fs.sax.evt.SaxEvent;

public abstract class AbstractState implements State{

  public static final int EXTENSIBILITY_ELEMENT = 0;
  
  private ParseContext _pc;

  protected AbstractState(ParseContext pc) {
    super();
      _pc = pc;
  }
  
  public final ParseContext getParseContext() {
    return _pc;
  }
  
   public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
      case EXTENSIBILITY_ELEMENT:
        // ignore.  This will get logged up front.
        break;
    default:
      throw new IllegalStateException(
          "Implementation error; unknown state " + pn.getClass().getName() +
          " encountered.");
    }
  }
  
  
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (se.getType() == SaxEvent.CHARACTERS) {
      Characters c = (Characters) se;
      if (c.getContent().trim().length() != 0) {
        // TODO: Non-whitespace content -- throw exception.
      }
    } else {
      getParseContext().parseError(ParseError.ERROR,se,"","Unexpected SAX event " + 
          se.toString()); // TODO: Error key.
    }
  }
  
  public void done(){}
}
