/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.wsdl;

import javax.wsdl.WSDLException;


/**
 * An exception thrown when a BPEL <code>&lt;partnerLinkType&gt;</code> declaration
 * is invalid (from a schema perspective).
 */
public class InvalidBpelPartnerLinkTypeException extends WSDLException {
  private static final long serialVersionUID = 1L;
	/**
   * Construct a new instance with the specified explanatory message.
   * @param msg an explanatory message.
   * @see WSDLException#WSDLException(java.lang.String, java.lang.String)
   */
  public InvalidBpelPartnerLinkTypeException(String msg) {
    super(WSDLException.INVALID_WSDL, msg);
  }
  /**
   * Construct a new instance with the specified explanatory message and the
   * exception that triggered this exception.
   * @param msg an explanatory message
   * @param t the <code>Throwable</code> that triggered this exception.
   * @see WSDLException#WSDLException(java.lang.String, java.lang.String, java.lang.Throwable)
   */
  public InvalidBpelPartnerLinkTypeException(String msg,
      Throwable t) {
    super(WSDLException.INVALID_WSDL, msg, t);
  }
}
