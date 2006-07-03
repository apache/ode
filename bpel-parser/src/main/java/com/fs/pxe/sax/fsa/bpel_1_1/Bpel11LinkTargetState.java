/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.LinkTarget;
import com.fs.pxe.bom.impl.nodes.LinkTargetImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

class Bpel11LinkTargetState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private LinkTargetImpl _t;
  
  
  Bpel11LinkTargetState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _t = new LinkTargetImpl();
    _t.setNamespaceContext(se.getNamespaceContext());
    _t.setLineNo(se.getLocation().getLineNumber());
    _t.setLinkName(atts.getValue("linkName"));
  }
  
  public LinkTarget getTarget() {
    return _t;
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
    return BPEL11_TARGET;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11LinkTargetState(se,pc);
    }
  }
}
