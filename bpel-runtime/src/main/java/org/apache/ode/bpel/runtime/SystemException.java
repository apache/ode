/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

/**
 * Run-time exception indicating an infrastructure problem.
 */
class SystemException extends RuntimeException {
  private static final long serialVersionUID = 3194250610255026706L;

  SystemException(Throwable cause) {
    super(cause);
  }

}
