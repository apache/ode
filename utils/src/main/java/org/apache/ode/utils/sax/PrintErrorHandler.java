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
package org.apache.ode.utils.sax;

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
