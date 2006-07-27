/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

/**
 * Exception thrown by {@link BpelProcessBuilderFactory} to indicate configuration
 * error.
 */
public class BpelProcessBuilderFactoryException extends Exception {

  private static final long serialVersionUID = 1L;

	/** Constructor. */
  public BpelProcessBuilderFactoryException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
