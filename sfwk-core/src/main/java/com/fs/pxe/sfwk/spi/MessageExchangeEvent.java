/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;


/**
 * Base class for message-exchange events.
 * Message-exchange events communicate the progress of inter-service communication,
 * that is operations on WSDL operations.
 * In terms of message-exchange pattern state machines defined in the WSDL specification,
 * these events correspond to the state transitions.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface MessageExchangeEvent extends ServiceEvent {

  boolean isClientEvent();
  boolean isServerEvent();
  
  /**
   * Get the message exchange instance identifier.
   *
   * @return message exchange instance identifier
   */
  String getInstanceId();

  /**
   * Get the target port for this message-exchange event.
   * @return target port
   */
  ServicePort getPort();

  /**
   * Get the message exchange for the message exchange to which this
   * event pertains.
   *
   * @return {@link com.fs.pxe.sfwk.spi.MessageExchange} apropos this exchange
   */
  MessageExchange getMessageExchange();
}
