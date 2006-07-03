/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import com.fs.pxe.sfwk.core.PxeSystemException;

// TODO: Better documentation

/**
 * A MessageExchangeException is thrown by a {@link com.fs.pxe.sfwk.spi.MessageExchange}
 * instance to provide additional descriptive information about a wrapped exception or
 * other failure.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public class MessageExchangeException extends PxeSystemException {
  /**
   * @see PxeSystemException#JloException(String)
   */
  public MessageExchangeException(String msg) {
    super(msg);
  }

  public MessageExchangeException(Throwable cause) {
    super(cause);
  }
  /**
   * @see PxeSystemException#JloException(String, Throwable)
   */
  public MessageExchangeException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
