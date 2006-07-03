/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt.attspec;

import com.fs.sax.evt.XmlAttributes;

public interface XmlAttributeSpec {
  
  public boolean matches(XmlAttributes atts);

}

