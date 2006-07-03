/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;


/**
 * Message exchange msgs indicating that an input fault message has been
 * received.
 * <p>
 * <em>NOTE: Input faults are a WSDL 1.2 feature and are currently not supported.</em>
 * </p>
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface InFaultRcvdMessageExchangeEvent extends ServerMessageExchangeEvent {
  /**
   * Get the input fault message that was received.
   *
   * @return input fault <code>Message</code> object
   */
  Message getInFaultMessage();
}
