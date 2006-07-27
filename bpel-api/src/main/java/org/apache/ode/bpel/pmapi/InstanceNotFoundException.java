/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.pmapi;

/**
 * Exception indicating that the requested instance identifier could not be found.
 */
public class InstanceNotFoundException extends InvalidRequestException {

  public InstanceNotFoundException(String msg) {
    super(msg);
  }

}
