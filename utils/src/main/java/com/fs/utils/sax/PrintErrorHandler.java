/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.sax;

import java.io.PrintStream;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PrintErrorHandler implements ErrorHandler {

  private ErrorHandler _child;
  private PrintStream _out;
  
  public PrintErrorHandler(ErrorHandler eh,PrintStream ps) {
    _child = eh;
    _out = ps;
  }
  
  public PrintErrorHandler(PrintStream pw) {
    this(new IgnoreAllErrorHandler(),pw);
  }

  /**
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
    _out.println(formatMessage(exception));
    _child.warning(exception);
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
    _out.println(formatMessage(exception));
    _child.error(exception);
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    _out.println(formatMessage(exception));
    _child.fatalError(exception);
  }
  
  private String formatMessage(SAXParseException spe) {
    StringBuffer sb = new StringBuffer();
    sb.append('[');
    sb.append(spe.getSystemId());
    sb.append(':');
    sb.append(spe.getLineNumber());
    sb.append(':');
    sb.append(spe.getColumnNumber());
    sb.append("] ");
    sb.append(spe.getMessage());
    return sb.toString();
  }
}
