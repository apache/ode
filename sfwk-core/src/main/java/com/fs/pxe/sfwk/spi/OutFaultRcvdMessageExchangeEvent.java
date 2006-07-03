/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Message exchange event indicating that an output fault message has been
 * received.
 */
public interface OutFaultRcvdMessageExchangeEvent extends ClientMessageExchangeEvent {
  /**
   * Get the name of the fault.
   *
   * @return the name of the fault
   */
  String getFaultName();

  /**
   * Get the output fault message that was received.
   *
   * @return output fault <code>Message</code> object
   */
  Message getOutFaultMessage();
}
