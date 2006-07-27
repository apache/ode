/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.jbi.msgmap;

/**
 * Indicates an error translating ODE message to JBI message or vice versa.
 */
public class MessageTranslationException extends Exception {
	public MessageTranslationException(String msg) {
		super(msg);
	}

	public MessageTranslationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
