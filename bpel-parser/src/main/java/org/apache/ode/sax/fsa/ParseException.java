/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa;

import org.xml.sax.SAXException;

public class ParseException extends SAXException {
  
  private static final long serialVersionUID = 1L;
	private ParseError _pe;
  
  public ParseException(ParseError pe ){
    super("");
    _pe = pe;
  }
  
  public ParseError getParseError() {
    return _pe;
  }
  
  public String getMessage() {
    return _pe.getMessage();
  }
}
