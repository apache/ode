/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.LinkSource;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

import java.util.ArrayList;
import java.util.Iterator;

class BpelLinkSourcesState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<LinkSource> _sources = new ArrayList<LinkSource>();

  BpelLinkSourcesState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_SOURCE:
      _sources.add(((BpelLinkSourceState)pn).getSource());
      break;
    default:
      super.handleChildCompleted(pn);
    }
  }
  
  
  public Iterator<LinkSource> getSources() {
    return _sources.iterator();
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
    return BPEL_SOURCES;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLinkSourcesState(se,pc);
    }
  }
}
