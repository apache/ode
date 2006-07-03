/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.impl.nodes.AssignActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

class Bpel11AssignActivityState extends Bpel11BaseActivityState {

  private static final StateFactory _factory = new Factory();

  Bpel11AssignActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se, pc);
  }

  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_COPY) {
      ((AssignActivityImpl)getActivity()).addCopy(((Bpel11CopyState)pn).getCopy());
    }
    else {
      super.handleChildCompleted(pn);
    }
  }

  protected Activity createActivity(StartElement se) throws ParseException {
    return new AssignActivityImpl();
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
    return BPEL11_ASSIGN;
  }

  static class Factory implements StateFactory {
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11AssignActivityState(se, pc);
    }
  }
}
