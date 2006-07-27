/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CommandContextErrorHandler implements ErrorHandler {

  private CommandContext _cc;
  
  private boolean _err;
  /**
   * 
   */
  public CommandContextErrorHandler(CommandContext cc) {
    _cc = cc;
    _err = false;
  }

  public boolean hadError() {
    return _err;
  }
  
  /* (non-Javadoc)
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
    _cc.warn(formatMessage(exception));
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
    _err = true;
    _cc.errln(formatMessage(exception));    
  }

  /* (non-Javadoc)
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    _err = true;
    _cc.error(formatMessage(exception),exception);
    throw exception;
  }
  
  private String formatMessage(SAXParseException e) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    if (e.getSystemId() == null) {
      sb.append("<<null>>");
    } else {
      sb.append(e.getSystemId());
    }
    sb.append(" ");
    sb.append(e.getLineNumber());
    sb.append(':');
    sb.append(e.getColumnNumber());
    sb.append("] ");
    sb.append(e.getMessage());
    return sb.toString();
  }
  
}
