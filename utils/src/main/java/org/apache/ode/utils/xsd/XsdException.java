/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils.xsd;

import org.apache.ode.utils.msg.MessageBundle;

/**
 * An exception class to encapsulate failures to load XML Schema documents.
 */
public class XsdException extends Exception {

  private static final long serialVersionUID = -358803351534482670L;

  private static final XsdMessages __msgs = MessageBundle.getMessages(XsdMessages.class);

  private String _message;

  private int _lineNumber;

  private int _columnNumber;

  private String _systemId;

  private XsdException _previous;

  /**
   * @param message
   *          the detail message from the impl
   * @param lineNumber
   *          the line number where the error occurred
   * @param columnNumber
   *          the column number where the error occurred
   * @param literalSystemId
   *          the (literal, i.e., passed-in) System ID
   */
  public XsdException(XsdException previous, String message, int lineNumber, int columnNumber, String literalSystemId) {
    super(__msgs.msgXsdExceptionMessage(message, literalSystemId, lineNumber, columnNumber));
    _message = message;
    _lineNumber = lineNumber;
    _columnNumber = columnNumber;
    _systemId = literalSystemId;
    _previous = previous;
  }

  public String getDetailMessage() {
    return _message;
  }

  public String getSystemId() {
    return _systemId;
  }

  public int getLineNumber() {
    return _lineNumber;
  }

  public int getColumnNumber() {
    return _columnNumber;
  }

  public XsdException getPrevious() {
    return _previous;
  }
}
