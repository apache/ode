/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.CompensationHandler;
import org.apache.ode.bom.impl.nodes.CompensationHandlerImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelCompensationHandlerState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CompensationHandlerImpl _c;
  
  BpelCompensationHandlerState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _c = new CompensationHandlerImpl();
    _c.setNamespaceContext(se.getNamespaceContext());
    _c.setLineNo(se.getLocation().getLineNumber());
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof ActivityStateI) {
      _c.setActivity(((ActivityStateI)pn).getActivity());
    } else {
      super.handleChildCompleted(pn);
    }
  }
  
  public CompensationHandler getCompensationHandler() {
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
    return BPEL_COMPENSATIONHANDLER;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCompensationHandlerState(se,pc);
    }
  }
}
