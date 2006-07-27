/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;

public class LogErrorListener implements ErrorListener {

  private Log _log;
  
  public LogErrorListener(Log log) {
    _log = log;
  }
  
  public void warning(TransformerException exception)
      throws TransformerException {
    _log.warn(exception.getMessageAndLocation(),exception);
  }

  public void error(TransformerException exception) throws TransformerException {
    _log.error(exception.getMessageAndLocation(),exception);
  }

  public void fatalError(TransformerException exception)
      throws TransformerException {
    _log.fatal(exception.getMessageAndLocation(),exception);
  }

}
