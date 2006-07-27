/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Copy;
import org.apache.ode.bom.impl.nodes.CopyImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

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
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
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
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
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
