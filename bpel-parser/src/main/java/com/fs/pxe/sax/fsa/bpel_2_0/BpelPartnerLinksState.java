/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.PartnerLink;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

import java.util.ArrayList;
import java.util.Iterator;

class BpelPartnerLinksState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<PartnerLink> _plinks;
   
  private BpelPartnerLinksState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _plinks = new ArrayList<PartnerLink>();
  }
  
  public Iterator<PartnerLink> getPartnerLinks() {
    return _plinks.iterator();
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_PARTNERLINK) {
      _plinks.add(((BpelPartnerLinkState)pn).getPartnerLink());
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
    return BPEL_PARTNERLINKS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelPartnerLinksState(se,pc);
    }
  }
}
