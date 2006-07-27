/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

import org.apache.ode.sax.fsa.ParseError;

/**
 * Implementation of the {@link ParseError} interface.
 */
class ParseErrorImpl implements ParseError {
  private String _locationURI;
  private int _lineNo;
  private int _column;
  private String _message;
  private String _key;
  private short _severity;

  ParseErrorImpl(short severity, String locationURI, int lineno, int column, String key, String msg) {
    _severity = severity;
    _locationURI = locationURI;
    _lineNo = lineno;
    _column = column;
    _key = key;
    _message = msg;
  }

    
  public String getLocationURI() {
    return _locationURI;
  }

  public int getLine() {
    return _lineNo;
  }

  public int getColumn() {
    return _column;
  }

  public short getSeverity() {
    return _severity;
  }

  public String getMessage() {
    return _message;
  }

  public String getKey() {
    return _key;
  }

  /**
   * Emacs-like error output.
   *
   * @return
   */
  public String toString() {
    StringBuffer buf = new StringBuffer(_locationURI);
    buf.append(':');
    buf.append(_lineNo);
    buf.append(':');
    buf.append(_column);
    buf.append(": ");
    buf.append(_message);
    return buf.toString();
  }
}