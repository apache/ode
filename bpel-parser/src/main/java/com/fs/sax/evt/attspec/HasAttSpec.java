/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt.attspec;

import com.fs.sax.evt.XmlAttributes;

import javax.xml.namespace.QName;

public class HasAttSpec implements XmlAttributeSpec {
  
  private QName _name;
  
  public HasAttSpec(QName name) {
    _name = name;
  }
  
  public boolean matches(XmlAttributes xatts) {
    return xatts.getValue(_name) != null;
  }
}
