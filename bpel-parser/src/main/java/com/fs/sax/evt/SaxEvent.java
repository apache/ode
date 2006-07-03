/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.sax.evt;

import com.fs.utils.NSContext;

import org.xml.sax.Locator;

public abstract class SaxEvent {
  
  public static final short START_ELEMENT = 0;
  public static final short END_ELEMENT = 1;
  public static final short CHARACTERS = 2;
  
  private Locator _loc;
  private NSContext _nsc;
  
  public SaxEvent(Locator loc, NSContext nsc) {
    _loc = loc;
    _nsc = nsc;
  }
  
  public Locator getLocation() {
    return _loc;
  }
  
  public NSContext getNamespaceContext() {
    return _nsc;
  }
  
  public abstract short getType();
}
