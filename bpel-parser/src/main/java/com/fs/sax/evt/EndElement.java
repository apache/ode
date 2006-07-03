/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt;

import com.fs.utils.NSContext;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

public class EndElement extends SaxEvent {
  
  private QName _name;
  
  public EndElement(QName name, Locator loc, NSContext nsc) {
    super(loc,nsc);
    _name = name;
  }
  
  public QName getName() {
    return _name;
  }
  
  public short getType() {
    return END_ELEMENT;
  }
  
  public String toString() {
    return "</" + _name.toString() + ">";
  }
}
