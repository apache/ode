/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.impl.nodes.InvokeActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

import java.util.Iterator;

class BpelInvokeActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelInvokeActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    InvokeActivityImpl iai = new InvokeActivityImpl();
    XmlAttributes atts = se.getAttributes();
    iai.setPortType(se.getNamespaceContext().derefQName(atts.getValue("portType")));
    iai.setPartnerLink(atts.getValue("partnerLink"));
    iai.setOperation(atts.getValue("operation"));
    iai.setInputVar(atts.getValue("inputVariable"));
    iai.setOutputVar(atts.getValue("outputVariable"));
    return iai;
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    InvokeActivityImpl iai = (InvokeActivityImpl) getActivity();
    switch (pn.getType()) {
    case BaseBpelState.BPEL_CORRELATIONS:
      for (Iterator<Correlation> it = ((BpelCorrelationsState)pn).getCorrelations();it.hasNext();) {
        iai.addCorrelation(it.next());
      }
      break;
    case BaseBpelState.BPEL_CATCH:
      iai.getFaultHandler().addCatch(((BpelCatchState)pn).getCatch());
      break;
    case BaseBpelState.BPEL_CATCHALL:
      iai.getFaultHandler().addCatch(((BpelCatchAllState)pn).getCatch());
      break;
    case BaseBpelState.BPEL_COMPENSATIONHANDLER:
      iai.setCompensationHandler(((BpelCompensationHandlerState)pn).getCompensationHandler());
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
    return BPEL_INVOKE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelInvokeActivityState(se,pc);
    }
  }
}
