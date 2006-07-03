/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils;

/**
 * An exception to encapsulate issues with system configuration. Examples
 * include the inability to find required services (e.g., XML parsing).
 */
public class SystemConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -2330515949287155695L;

	/**
	 * Construct a new instance with the specified message.
	 * 
	 * @param message
	 *            a descriptive message.
	 * @see RuntimeException#RuntimeException(java.lang.String)
	 */
	public SystemConfigurationException(String message) {
		super(message);
	}

	/**
	 * Construct a new instance with the specified message and a
	 * {@link Throwable} that triggered this exception.
	 * 
	 * @param message
	 *            a descriptive message
	 * @param cause
	 *            the cause
	 * @see RuntimeException#RuntimeException(java.lang.String,
	 *      java.lang.Throwable)
	 */
	public SystemConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Construct a new instance with the specified {@link Throwable} as the root
	 * cause.
	 * 
	 * @param cause
	 *            the cause
	 * @see RuntimeException#RuntimeException(java.lang.Throwable)
	 */
	public SystemConfigurationException(Throwable cause) {
		super(cause);
	}
}
