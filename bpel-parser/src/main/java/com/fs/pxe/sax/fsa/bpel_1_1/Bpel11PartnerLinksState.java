/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.pxe.bom.api.PartnerLink;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

class Bpel11PartnerLinksState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private List<PartnerLink> _plinks = new ArrayList<PartnerLink>();
   
  private Bpel11PartnerLinksState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
  }
  
  public Iterator<PartnerLink> getPartnerLinks() {
    return _plinks.iterator();
  }
  
  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL11_PARTNERLINK) {
      _plinks.add(((Bpel11PartnerLinkState)pn).getPartnerLink());
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
    return BPEL11_PARTNERLINKS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11PartnerLinksState(se,pc);
    }
  }
}
