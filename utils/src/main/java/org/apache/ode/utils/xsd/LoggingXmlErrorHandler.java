/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.utils.xsd;

import org.apache.commons.logging.Log;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;

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
