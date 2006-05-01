/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.CorrelationSet;
import org.apache.ode.bom.impl.nodes.CorrelationSetImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.utils.NSContext;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

class BpelCorrelationSetState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CorrelationSetImpl _s;

  BpelCorrelationSetState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _s = new CorrelationSetImpl();
    _s.setNamespaceContext(se.getNamespaceContext());
    _s.setLineNo(se.getLocation().getLineNumber());
    _s.setName(atts.getValue("name"));
    if (atts.hasAtt("properties")) {
      StringTokenizer st = new StringTokenizer(atts.getValue("properties"));
      ArrayList<QName> al = new ArrayList<QName>();
      NSContext nsc = se.getNamespaceContext();
      for (;st.hasMoreTokens();) {
        al.add(nsc.derefQName(st.nextToken()));
      }
      _s.setProperties(al.toArray(new QName[] {}));
    }
    
  }
  
  public CorrelationSet getCorrelationSet() {
    return _s;
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
    return BPEL_CORRELATIONSET;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCorrelationSetState(se,pc);
    }
  }
}
