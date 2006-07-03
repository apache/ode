/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Link;
import com.fs.pxe.bom.impl.nodes.LinkImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

class BpelLinkState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkImpl _link;
  
  BpelLinkState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _link = new LinkImpl();
    _link.setNamespaceContext(se.getNamespaceContext());
    _link.setLineNo(se.getLocation().getLineNumber());
    _link.setLinkName(atts.getValue("name"));
  }
  
  public Link getLink() {
    return _link;
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
    return BPEL_LINK;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLinkState(se,pc);
    }
  }
}
