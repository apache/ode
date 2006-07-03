/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa;


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
