/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FailOnErrorErrorHandler implements ErrorHandler {
  
  private ErrorHandler _child;
  
  public FailOnErrorErrorHandler() {
    this(new IgnoreAllErrorHandler());
  }
  
  public FailOnErrorErrorHandler(ErrorHandler eh) {
    _child = eh;
  }
  
  /**
   * @see ErrorHandler#error(org.xml.sax.SAXParseException)
   */  
  public void error(SAXParseException exception) throws SAXException {
    throw exception;
  }
  /**
   * @see ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */  
  public void fatalError(SAXParseException exception) throws SAXException {
    throw exception;
  }
  /**
   * @see ErrorHandler#warning(org.xml.sax.SAXParseException)
   */  
  public void warning(SAXParseException exception) throws SAXException {
    _child.warning(exception);
  }
}
