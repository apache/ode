/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.evt.attspec;

import org.apache.ode.sax.evt.XmlAttributes;

public interface XmlAttributeSpec {
  
  public boolean matches(XmlAttributes atts);

}

