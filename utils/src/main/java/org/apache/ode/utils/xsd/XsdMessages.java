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

public class XsdMessages extends MessageBundle {

  /**
   * A key to indicate that a message is a warning.
   */
  public String msgXsdWarning() {
    return this.format("WARNING");
  }

  /**
   * A key to indicate that a message is an error.
   */
  public String msgXsdError() {
    return this.format("ERROR");
  }

  /**
   * A key to indicate that a message is fatal error.
   */
  public String msgXsdFatal() {
    return this.format("FATAL");
  }

  /**
   * Format a log message about an XSD-related warning or error.
   * 
   * @param type
   *          the type of occurrence (e.g., warning, error, fatal)
   * @param msg
   *          the warning message
   * @param systemId
   *          the System ID of the schema
   * @param line
   *          the line number where the event occurred, or -1
   * @param col
   *          the column number where the event occurred, or -1
   * @return the formatted message
   * 
   * {0}: [{2} @ L{3}:C{4}]: {1}
   */
  public String msgXsdMessage(String type, String msg, String systemId, int line, int col) {
    return this.format("{0}: [{2} @ L{3}:C{4}]: {1}", type, msg, systemId, line, col);
  }

  /**
   * Format a debug message about processing an XML Schema document.
   * 
   * @param systemId
   *          the System ID of the schema being processed
   * @return the formatted message
   * 
   * Processing schema with URI {0}.
   */
  public String msgProcessingSchema(String systemId) {
    return this.format("Processing schema with URI {0}.", systemId);
  }

  /**
   * Format a message about an exception that occurred while processing an XML
   * Schema.
   * 
   * @param message
   *          the detailed message about the exception
   * @param systemId
   *          the System ID of the schema
   * @param lineNumber
   *          the line number where the exception occurred, or -1
   * @param columnNumber
   *          the column number where the exception occurred, or -1
   * @return the formatted message
   * 
   * Unable to process XML Schema from {1} [@L{2}:C{3}]: {0}
   */
  public String msgXsdExceptionMessage(String message, String systemId, int lineNumber,
      int columnNumber) {
    return this.format("Unable to process XML Schema from {1} [@L{2}:C{3}]: {0}", message,
        systemId, lineNumber, columnNumber);
  }

  /** An unknown error occured processing schema at {0}" */
  public String msgXsdUnknownError(String systemId) {
    return this.format("An unknown error occured processing schema at {0}", systemId);
  }

}
