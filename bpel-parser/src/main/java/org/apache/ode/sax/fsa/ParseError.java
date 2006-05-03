/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.sax.fsa;


public interface ParseError {
  /** Fatal severity level. */
  public static final short FATAL = 3;

  /** Error severity level. */
  public static final short ERROR = 2;

  /** Warning severity level. */
  public static final short WARNING = 1;

  /** Get the location (URI or file name) of the error/warning. */
  public String getLocationURI();

  /** Get the line number of the error/warning. */
  public int getLine();

  /** Get the column number of the error/warning. */
  public int getColumn();

  /**
   * Get the severity of the error/warning.
   * @return on of {@link #FATAL}, {@link #ERROR}, or {@link #WARNING}
   */
  public short getSeverity();

  /**
   * Get the internationalized error/warning message.
   * @return internationalized error/warning message
   */
  public String getMessage();

  /**
   * Get the internationalization message key used to create the error message.
   * @return internationalization message key
   */
  public String getKey();
}
