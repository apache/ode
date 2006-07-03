/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath20.runtime;

import com.fs.pxe.bpel.common.FaultException;

import net.sf.saxon.trans.XPathException;

public class FaultXPathException extends XPathException {
	
	public FaultXPathException(FaultException err) {
		super(err);
	}
	
}
