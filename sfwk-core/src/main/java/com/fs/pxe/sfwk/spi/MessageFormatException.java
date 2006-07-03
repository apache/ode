/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Thrown if an action violates format requirements for a
 * {@link com.fs.pxe.sfwk.spi.Message}, normally with additional explanatory
 * information or a wrapped exception.
 */
public class MessageFormatException extends MessageExchangeException {
  /**
   * @see Exception#Exception(java.lang.String)
   */
  public MessageFormatException(String msg) {
    super(msg);
  }

  /**
   * @see Exception#Exception(java.lang.String, java.lang.Throwable)
   */
  public MessageFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @see Exception#Exception(java.lang.Throwable)
   */
  public MessageFormatException(Throwable cause) {
    super(cause);
  }
}
