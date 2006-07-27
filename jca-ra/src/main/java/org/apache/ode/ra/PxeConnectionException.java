/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

import javax.resource.ResourceException;

/**
 * Exception thrown in case of ODE communication failures.
 */
public class OdeConnectionException extends ResourceException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * @param message the message
   */
  OdeConnectionException(String message) {
    super(message);
  }
  
  /**
   * Constructor.
   * @param message the message
   * @param cause a <code>Throwable</code> cause for this exception
   */
  OdeConnectionException(String message, Throwable cause) {
    super(message);
    initCause(cause);
  }

  /**
   * Constructor.
   * @param cause a <code>Throwable</code> cause for this exception
   */
  OdeConnectionException(Throwable cause) {
    super(cause.getMessage());
    initCause(cause);
  }
}
