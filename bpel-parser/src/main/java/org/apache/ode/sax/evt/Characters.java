/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.evt;

import org.apache.ode.utils.NSContext;

import org.xml.sax.Locator;

public class Characters extends SaxEvent {
  
  private String _content;
  
  public Characters(String content, Locator loc, NSContext nsc) {
    super(loc,nsc);
    _content = content;
  }
  
  public String getContent() {
    return _content;
  }
  
  public short getType() {
    return CHARACTERS;
  }
  
  public String toString() {
    return _content;
  }
}
