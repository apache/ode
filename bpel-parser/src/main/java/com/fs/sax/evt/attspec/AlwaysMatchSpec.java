/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt.attspec;

import com.fs.sax.evt.XmlAttributes;

public class AlwaysMatchSpec implements XmlAttributeSpec {

  public static final AlwaysMatchSpec SINGLETON = new AlwaysMatchSpec();
  
  public boolean matches(XmlAttributes atts) {
    return true;
  }
}
