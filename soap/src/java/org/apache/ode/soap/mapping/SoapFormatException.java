/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.soap.mapping;

/**
 * Indicates problem with a SOAP payload.
 */
public class SoapFormatException extends Exception {

  /** Constructor. */
	public SoapFormatException(String reason){
		super(reason);
	}
}
