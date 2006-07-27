/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.evt;

import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

public class StartElement extends SaxEvent {
  
  private QName _name;
  private XmlAttributes _atts;
  
  public StartElement(QName name, XmlAttributes atts, Locator loc, NSContext nsc) {
    super(loc,nsc);
    _name = name;
    _atts = atts;
  }
  
  public QName getName() {
    return _name;
  }
  
  public XmlAttributes getAttributes() {
    return _atts;
  }
  
  public short getType() {
    return START_ELEMENT;
  }
  
  public String toString() {
    return "<" + _name.toString() + ">";
  }
}
