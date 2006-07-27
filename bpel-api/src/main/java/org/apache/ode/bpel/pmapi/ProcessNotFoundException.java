/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.pmapi;

/**
 * Exception thrown to indicate that the requested process could
 * not be found.
 */
public class ProcessNotFoundException extends InvalidRequestException {

  public ProcessNotFoundException(String msg) {
    super(msg);
  }

}
