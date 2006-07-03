/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils.sax;

import com.fs.utils.msg.CommonMessages;
import com.fs.utils.msg.MessageBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An <code>ErrorHandler</code> implementation that dumps all of the error
 * messages onto a logging channel at the debug level.
 */
public class LoggingErrorHandler implements ErrorHandler {

  private static final Log __log = LogFactory.getLog(LoggingErrorHandler.class);
  private Log _l;

  private static final String WARNING = MessageBundle.getMessages(CommonMessages.class)
      .strWarning().toUpperCase();
  private static final String ERROR = MessageBundle.getMessages(CommonMessages.class)
      .strError().toUpperCase();
  private static final String FATAL = MessageBundle.getMessages(CommonMessages.class)
      .strFatal().toUpperCase();

  /**
   * Construct a new instance that dumps messages onto the default logging
   * channel defined by this class.
   */
  public LoggingErrorHandler() {
    _l = __log;
  }

  /**
   * Construct a new instance that dumps messages onto a specific logging
   * channel.
   * 
   * @param log
   *          the <code>Log</code> on which to dump messages.
   */
  public LoggingErrorHandler(Log log) {
    _l = log;
  }

  /**
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
    if (_l.isDebugEnabled()) {
      _l.debug(formatMessage(WARNING, exception));
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
    if (_l.isDebugEnabled()) {
      _l.debug(formatMessage(ERROR, exception));
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    if (_l.isDebugEnabled()) {
      _l.debug(formatMessage(FATAL, exception));
    }
  }

  private String formatMessage(String level, SAXParseException spe) {
    StringBuffer sb = new StringBuffer(64);

    if (spe.getSystemId() != null) {
      sb.append(spe.getSystemId());
    }

    sb.append(':');
    sb.append(spe.getLineNumber());
    sb.append(':');
    sb.append(spe.getColumnNumber());
    sb.append(':');
    sb.append(level);
    sb.append(':');
    sb.append(spe.getMessage());

    return sb.toString();
  }

}
