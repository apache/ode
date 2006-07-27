/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.soap.mapping;

/**
 * Indicates a problem with the SOAP WSDL binding.
 */
public class SoapBindingException extends Exception {

  /** Internationalized, human-readable messsage. */
  private String _msg;
  private String _loc;
  private String _elType;

  /**
   * Constructor.
   * @param reason internal error message (printed in stack trace)
   * @param locType element type where error occured
   * @param loc name of element where error occured
   * @param ilmsg internationalized message
   */
  public SoapBindingException(String reason, String locType, String loc, String ilmsg) {
    super(reason);
    _elType =locType;
    _loc = loc;
    _msg = ilmsg;
  }

  public String getLocalizedMessage() {
    StringBuffer buf = new StringBuffer();
    buf.append(_elType);
    buf.append(" \"");
    buf.append(_loc);
    buf.append("\" : ");

    if (_msg == null)
      buf.append(getMessage());
    else
      buf.append(_msg);

    return buf.toString();
  }

  public String getLocationType() {
    return _elType;
  }

  public String getLocation() {
    return _loc;
  }
}

