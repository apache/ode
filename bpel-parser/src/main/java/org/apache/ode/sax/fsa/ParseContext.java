/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa;

import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.sax.evt.SaxEvent;

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
