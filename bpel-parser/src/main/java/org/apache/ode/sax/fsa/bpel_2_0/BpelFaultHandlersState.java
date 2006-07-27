/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.FaultHandler;
import org.apache.ode.bom.impl.nodes.FaultHandlerImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelFaultHandlersState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private FaultHandlerImpl _f;
   
  private BpelFaultHandlersState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _f = new FaultHandlerImpl();
    _f.setNamespaceContext(se.getNamespaceContext());
    _f.setLineNo(se.getLocation().getLineNumber());
  }
  
  public FaultHandler getFaultHandler() {
    return _f;
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_CATCH:
    case BPEL_CATCHALL:
      _f.addCatch(((BpelCatchAllState)pn).getCatch());
      break;
    default:
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
    return BPEL_FAULTHANDLERS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelFaultHandlersState(se,pc);
    }
  }
}
