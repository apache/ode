/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;

public class XmlAttributes {
  
  private HashMap<QName,String> _hm;
  
  public XmlAttributes(Attributes atts) {
    _hm = new HashMap<QName,String>();
    for (int i=0; i < atts.getLength(); ++i) {
      _hm.put(new QName(atts.getURI(i),atts.getLocalName(i)),atts.getValue(i));
    }
  }
  
  public String getValue(QName qn) {
    return _hm.get(qn);
  }
  
  public boolean hasAtt(QName qn) {
    return _hm.get(qn) != null;
  }
  
  public boolean hasAtt(String s) {
    return _hm.get(new QName(s)) != null;
  }
  
  public String getValue(String s) {
    return getValue(new QName(s));
  }
  
  public int getCount() {
    return _hm.size();
  }
  
  public Iterator<QName> getQNames() {
    return _hm.keySet().iterator();
  }
  
  public String toString() {
    if (_hm.size() == 0) {
      return "<<none>>";
    }
    StringBuffer sb = new StringBuffer();
    boolean flag = true;
    for (Iterator<QName> it = getQNames(); it.hasNext(); ) {
      if (!flag) {
        sb.append(' ');
      }
      sb.append( it.next());
      flag = false;
    }
    return sb.toString();
  }
}
