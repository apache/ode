/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;


/**
 * Message exchange event indicating that an input message has been received.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface InputRcvdMessageExchangeEvent extends ServerMessageExchangeEvent {
  /**
   * Get the input message that was received.
   *
   * @return input <code>Message</code> object
   */
  Message getInputMessage();
}
