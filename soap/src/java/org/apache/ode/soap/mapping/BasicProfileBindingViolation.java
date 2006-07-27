/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.soap.mapping;


/**
 * An exception that encapsulates a WS-I Basic Profile violation.
 */
public class BasicProfileBindingViolation extends SoapBindingException {
  private String _violation;

  /**
   * Constructor.
   */
  public BasicProfileBindingViolation(String code, String locType, String loc, String msg) {
    super("Basic Profile Violation #" + code, locType, loc, code + ":" + msg);
    _violation = code;
  }


  /**
   * Get the BP-I violation identifier (e.g. "R2718").
   * @return violation identifier
   */
  public String getViolation() {
    return _violation;
  }
}