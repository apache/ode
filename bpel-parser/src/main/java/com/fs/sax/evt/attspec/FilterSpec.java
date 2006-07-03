/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt.attspec;

import com.fs.sax.evt.XmlAttributes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

public class FilterSpec implements XmlAttributeSpec {

  private Set<QName> _required;
  private Set<QName> _optional;
  
  public FilterSpec(String[] required, String[] optional) {
    _required = new HashSet<QName>();
    for (int i=0; i < required.length; ++i) {
      _required.add(new QName(required[i]));
    }
    _optional = new HashSet<QName>();
    for (int i=0; i < optional.length; ++i) {
      _optional.add(new QName(optional[i]));
    }
  }
  
  public FilterSpec(QName[] required, QName[] optional) {
    _required = new HashSet<QName>();
    for (int i=0; i < required.length; ++i) {
      _required.add(required[i]);
    }
    _optional = new HashSet<QName>();
    for (int i=0; i < optional.length; ++i) {
      _optional.add(optional[i]);
    }
  }
  
  /**
   * @see com.fs.sax.evt.attspec.XmlAttributeSpec#matches(com.fs.sax.evt.XmlAttributes)
   */
  public boolean matches(XmlAttributes atts) {
    // check for all required.
    for (Iterator<QName> it = _required.iterator(); it.hasNext();) {
      if (atts.getValue(it.next()) == null) {
        // TODO: return a message.
        return false;
      }
    }
    for (Iterator<QName> it = atts.getQNames(); it.hasNext();) {
      QName qn = it.next();
      if (!_required.contains(qn) && !_optional.contains(qn) && qn.getNamespaceURI().equals("")) {
        // TODO: return a message.
        return false;
      }
    }
    return true;
  }
}
