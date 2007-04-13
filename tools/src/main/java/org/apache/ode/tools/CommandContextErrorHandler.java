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
