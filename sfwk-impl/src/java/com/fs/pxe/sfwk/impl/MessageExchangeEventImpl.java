/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.ObjectPrinter;

abstract class MessageExchangeEventImpl extends ServiceEventImpl implements MessageExchangeEvent {

  private MessageExchange _messageExchange;

  /** Event type. */
  private short _type;

  ServicePort _targetPort;

  /**
   * Constructor.
   * @param messageExchange apropos {@link com.fs.pxe.sfwk.spi.MessageExchange}
   * @param type event type
   *
   * @throws IllegalArgumentException DOCUMENTME
   */
  protected MessageExchangeEventImpl(MessageExchange messageExchange, short type) {
    super(type);
    if (messageExchange == null) {
      throw new IllegalArgumentException("messageExchange cannot be null!");
    }

    _messageExchange = messageExchange;
    _type = type;
  }

  public ServicePort getPort() {
    return _targetPort;
  }

  public short getEventType() {
    return _type;
  }

  public String getInstanceId() {
    return _messageExchange.getInstanceId();
  }

  public MessageExchange getMessageExchange() {
    return _messageExchange;
  }

  public boolean isClientEvent() {
    return this instanceof ClientMessageExchangeEvent;
  }

  public boolean isServerEvent() {
    return this instanceof ServerMessageExchangeEvent;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "eventType", Integer.valueOf(_eventType),
      "mex", _messageExchange,
      "targetService", _targetService,
      "type", Integer.valueOf(_type)
    });
  }
}
