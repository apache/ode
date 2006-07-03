/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt.attspec;

import com.fs.sax.evt.XmlAttributes;

import javax.xml.namespace.QName;

public class HasAttsSpec implements XmlAttributeSpec {
  
  private QName[] _names;
  
  public HasAttsSpec(QName[] names) {
    _names = names;
  }
  
  public boolean matches(XmlAttributes xatts) {
    boolean match = true;
    for (int i=0; i < _names.length && match; ++i) {
      match &= xatts.getValue(_names[i]) != null;
    }
    return match;
  }
}
