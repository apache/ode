/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

/**
 * Exception thrown to indicate an error in the BOM parser.
 */
public class BpelParseException extends Exception {

  private static final long serialVersionUID = 1L;

	/** Constructor. */
  public BpelParseException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
