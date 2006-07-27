/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.FaultHandler;
import org.apache.ode.bom.impl.nodes.FaultHandlerImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class Bpel11FaultHandlersState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private FaultHandlerImpl _f;
   
  private Bpel11FaultHandlersState(StartElement se, ParseContext pc) throws ParseException {
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
    case BPEL11_CATCH:
    case BPEL11_CATCHALL:
      _f.addCatch(((Bpel11CatchAllState)pn).getCatch());
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
    return BPEL11_FAULTHANDLERS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11FaultHandlersState(se, null);
    }
  }
}
