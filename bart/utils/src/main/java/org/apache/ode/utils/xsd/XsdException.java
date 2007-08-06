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
