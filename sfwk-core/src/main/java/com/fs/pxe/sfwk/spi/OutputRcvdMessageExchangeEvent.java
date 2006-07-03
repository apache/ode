/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Message exchange event indicating that an output message has been received.
 */
public interface OutputRcvdMessageExchangeEvent extends ClientMessageExchangeEvent {
  /**
   * Get the output message that was received.
   *
   * @return output <code>Message</code> object
   */
  Message getOutputMessage();
}
