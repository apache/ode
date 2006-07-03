/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Copy;
import com.fs.pxe.bom.impl.nodes.CopyImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

class BpelCopyState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CopyImpl _c;

  BpelCopyState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _c = new CopyImpl(se.getNamespaceContext());
    _c.setLineNo(se.getLocation().getLineNumber());
    if (atts.hasAtt("keepSrcElementName"))
      _c.setKeepSrcElement(checkYesNo(atts.getValue("keepSrcElementName")));
  }

  public Copy getCopy() {
    return _c;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_FROM:
      _c.setFrom(((BpelFromState)pn).getFrom());
      break;
    case BPEL_TO:
      _c.setTo(((BpelToState)pn).getTo());
      break;
    default:
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
    return BPEL_COPY;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCopyState(se,pc);
    }
  }
}
