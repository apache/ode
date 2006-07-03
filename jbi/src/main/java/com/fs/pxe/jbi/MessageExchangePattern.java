/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.jbi;

import java.net.URI;

/**
 * Constant exchange patter defs.
 */
public abstract class MessageExchangePattern {

	public static final URI IN_OUT;
	public static final URI IN_ONLY;

	static {
		try {
			IN_OUT = new URI("http://www.w3.org/2004/08/wsdl/in-out");
			IN_ONLY = new URI("http://www.w3.org/2004/08/wsdl/in-only");
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private MessageExchangePattern() {
	}

}
