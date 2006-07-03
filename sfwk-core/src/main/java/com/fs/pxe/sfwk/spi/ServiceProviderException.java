/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Exception thrown by a Service Provider implementation to indicate (to
 * the PXE container) that a problem has occured.
 *
 * <em>NOTE: This exception is thrown by the Service Provider implementation,
 *           never from the PXE container. </em>
 */
public class ServiceProviderException extends Exception {
  /**
   * Constructor with no message.
   */
  public ServiceProviderException() {
    super();
  }

  /**
   * Constructor with an exception message.
   *
   * @param message the exception message
   */
  public ServiceProviderException(String message) {
    super(message);
  }

  /**
   * Constructor that includes both a message and cause.
   *
   * @param message the exception message
   * @param cause throwable that has caused this exception
   */
  public ServiceProviderException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor that includes a cause.
   *
   * @param cause throwable that has caused this exception
   */
  public ServiceProviderException(Throwable cause) {
    super(cause);
  }
}
