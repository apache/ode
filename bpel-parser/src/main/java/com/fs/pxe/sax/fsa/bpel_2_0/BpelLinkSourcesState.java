/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.LinkSource;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
