/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.wsdl;

import javax.wsdl.WSDLException;


/**
 * An exception thrown in response to an invalid BPEL <code>&lt;property&gt;</code>
 * declaration.
 */
public class InvalidBpelPropertyException extends WSDLException {
  private static final long serialVersionUID = 1L;
	/**
   * Construct a new instance with the specified explanatory message.
   * @param msg an explanatory message.
   * @see WSDLException#WSDLException(java.lang.String, java.lang.String)
   */
  public InvalidBpelPropertyException(String msg) {
    super(WSDLException.INVALID_WSDL, msg);
  }
  /**
   * Construct a new instance with the specified explanatory message and the
   * exception that triggered this exception.
   * @param msg an explanatory message
   * @param t the <code>Throwable</code> that triggered this exception.
   * @see WSDLException#WSDLException(java.lang.String, java.lang.String, java.lang.Throwable)
   */
  public InvalidBpelPropertyException(String msg,
      Throwable t) {
    super(WSDLException.INVALID_WSDL, msg, t);
  }
}
