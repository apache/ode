/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.TerminationHandler;
import org.apache.ode.bom.impl.nodes.TerminationHandlerImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelTerminationHandlerState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private TerminationHandlerImpl _th;
  
  BpelTerminationHandlerState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _th = new TerminationHandlerImpl(se.getNamespaceContext());
    _th.setLineNo(se.getLocation().getLineNumber());
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      _th.setActivity(((ActivityStateI)pn).getActivity());
    } else {
      super.handleChildCompleted(pn);
    }
  }
  
  public TerminationHandler getTerminationHandler() {
    return _th;
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
    return BPEL_TERMINATIONHANDLER;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelTerminationHandlerState(se,pc);
    }
  }
}
