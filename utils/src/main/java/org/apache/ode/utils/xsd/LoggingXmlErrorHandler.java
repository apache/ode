/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils.xsd;

import org.apache.ode.utils.msg.MessageBundle;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;

import org.apache.commons.logging.Log;

/**
 * Implementation of {@link LoggingXmlErrorHandler} that outputs messages to a
 * log.
 */
public class LoggingXmlErrorHandler implements XMLErrorHandler {

  private Log _log;

  private static final XsdMessages __msgs = MessageBundle.getMessages(XsdMessages.class);

  private XMLParseException _ex;

  /**
   * Create a new instance that will output to the specified {@link Log}
   * instance.
   * 
   * @param log
   *          the target log, which much be non-<code>null</code>
   */
  public LoggingXmlErrorHandler(Log log) {
    assert log != null;
    _log = log;
  }

  public XMLParseException getLastError() {
    return _ex;
  }

  public void warning(String domain, String key, XMLParseException ex) throws XNIException {
    _log.warn(__msgs.msgXsdMessage(__msgs.msgXsdWarning(), ex.getMessage(), ex
        .getLiteralSystemId(), ex.getColumnNumber(), ex.getLineNumber()), ex);
  }

  public void error(String domain, String key, XMLParseException ex) throws XNIException {
    _log.error(__msgs.msgXsdMessage(__msgs.msgXsdError(), ex.getMessage(), ex
        .getLiteralSystemId(), ex.getColumnNumber(), ex.getLineNumber()), ex);
    _ex = ex;
    throw ex;
  }

  public void fatalError(String domain, String key, XMLParseException ex) throws XNIException {
    _log.error(__msgs.msgXsdMessage(__msgs.msgXsdFatal(), ex.getMessage(), ex
        .getLiteralSystemId(), ex.getColumnNumber(), ex.getLineNumber()), ex);
    _ex = ex;
    throw ex;
  }
}
