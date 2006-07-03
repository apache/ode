/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import java.io.Serializable;


/**
 * An object for long-term persistence of a reference to a {@link
 * MessageExchange}, capable of resolving the reference to a MesageExchange
 * during runtime.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface MessageExchangeRef extends Serializable {
  /**
   * Destroy (remove) the reference. 
   */
  public void destroy();

  /**
   * Resolves the reference to an actual message exchange, destroying it
   * in the process.
   *
   * @return referenced {@link MessageExchange} object
   */
  public MessageExchange resolve();

  /**
   * The unique id for the message exchange.
   * 
   * @return The message exchange id.
   */
  public String getInstanceId();
}
