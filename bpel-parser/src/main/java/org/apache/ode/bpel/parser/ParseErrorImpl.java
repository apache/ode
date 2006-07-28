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