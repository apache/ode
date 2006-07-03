/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * <code>ErrorHandler</code> implementation that does nothing, i.e., ignores all
 * error events.
 * </p>
 * <p>
 * <em>Note:</em> This is not entirely appropriate, as
 * {@link #fatalError(SAXParseException)} handling should throw a
 * {@link org.xml.sax.SAXException}
 * </p>
 */
public class IgnoreAllErrorHandler implements ErrorHandler {

  /**
   * Create a new instance.
   */
  public IgnoreAllErrorHandler() {
    // do nothing.
  }
  
  /**
   * @see ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
  }

  /**
   * @see ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
  }

  /**
   * @see ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
  }
}
