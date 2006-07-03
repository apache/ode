/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import com.fs.pxe.bom.api.CorrelationSet;
import com.fs.pxe.bom.impl.nodes.CorrelationSetImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.utils.NSContext;

class Bpel11CorrelationSetState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CorrelationSetImpl _s;
  
  Bpel11CorrelationSetState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();

    _s = new CorrelationSetImpl();
    _s.setNamespaceContext(se.getNamespaceContext());
    _s.setLineNo(se.getLocation().getLineNumber());
    _s.setName(atts.getValue("name"));

    if (atts.hasAtt("properties")) {
      StringTokenizer st = new StringTokenizer(atts.getValue("properties"));
      List<QName> al = new ArrayList<QName>();
      NSContext nsc = se.getNamespaceContext();
      for (;st.hasMoreTokens();) {
        al.add(nsc.derefQName(st.nextToken()));
      }
      _s.setProperties(al.toArray(new QName[]{}));
    }
    
  }
  
  public CorrelationSet getCorrelationSet() {
    return _s;
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
    return BPEL11_CORRELATIONSET;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se,ParseContext pc) throws ParseException {
      return new Bpel11CorrelationSetState(se,pc);
    }
  }
}
