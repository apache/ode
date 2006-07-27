/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.evt.attspec;

import org.apache.ode.sax.evt.XmlAttributes;

public class AndSpec implements XmlAttributeSpec {
  
  XmlAttributeSpec _lhs;
  XmlAttributeSpec _rhs;
  
  public AndSpec(XmlAttributeSpec lhs, XmlAttributeSpec rhs) {
    _lhs = lhs;
    _rhs = rhs;
  }
  
  public boolean matches(XmlAttributes xatts) {
    return _lhs.matches(xatts) && _rhs.matches(xatts);
  }
}
