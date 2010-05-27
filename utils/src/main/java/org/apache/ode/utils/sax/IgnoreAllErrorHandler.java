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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 * <code>ErrorHandler</code> implementation that does nothing, i.e., ignores all
 * error events.
 * </p>
 * <p>
 * <em>Note:</em> This is not entirely appropriate, as
 * {@link #fatalError(SAXParseException)} handling should throw a
 * {@link org.xml.sax.SAXException}
 * </p>
 */
public class IgnoreAllErrorHandler implements ErrorHandler {

  /**
   * Create a new instance.
   */
  public IgnoreAllErrorHandler() {
    // do nothing.
  }

  /**
   * @see ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
  }

  /**
   * @see ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
  }

  /**
   * @see ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
  }
}
