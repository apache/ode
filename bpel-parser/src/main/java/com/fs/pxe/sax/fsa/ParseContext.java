/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa;

import com.fs.pxe.bom.api.BpelObject;
import com.fs.sax.evt.SaxEvent;

public interface ParseContext {
  
  public void setBaseUri(String uri);
  public String getBaseUri();
  public void parseError(ParseError pe) throws ParseException;
  public void parseError(short severity, SaxEvent se, String key, String msg)
    throws ParseException;
  public void parseError(short severity, BpelObject bo, String key, String msg)
    throws ParseException;
  public void parseError(short severity, String key, String msg)
    throws ParseException;
  
}
