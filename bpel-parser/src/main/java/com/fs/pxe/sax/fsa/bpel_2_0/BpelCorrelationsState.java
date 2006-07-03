/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

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
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_CORRELATION) {
      _cs.add(((BpelCorrelationState)pn).getCorrelation());
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
    return BPEL_CORRELATIONS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCorrelationsState(se,pc);
    }
  }
}
