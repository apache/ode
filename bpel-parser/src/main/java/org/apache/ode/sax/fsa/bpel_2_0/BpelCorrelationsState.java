/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

import java.util.ArrayList;
import java.util.Iterator;

class BpelCorrelationsState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<Correlation> _cs;
   
  private BpelCorrelationsState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _cs = new ArrayList<Correlation>();
  }
  
  public Iterator<Correlation> getCorrelations() {
    return _cs.iterator();
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_CORRELATION) {
      _cs.add(((BpelCorrelationState)pn).getCorrelation());
    } else {
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
    return BPEL_CORRELATIONS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCorrelationsState(se,pc);
    }
  }
}
