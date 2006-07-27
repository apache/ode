/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath20.runtime;

import org.apache.ode.bpel.common.FaultException;

import net.sf.saxon.trans.XPathException;

public class FaultXPathException extends XPathException {
	
	public FaultXPathException(FaultException err) {
		super(err);
	}
	
}
