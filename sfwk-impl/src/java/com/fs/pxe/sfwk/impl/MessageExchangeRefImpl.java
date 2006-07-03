/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeRef;

import java.io.Serializable;


/**
 * Implementation of the {@link MessageExchangeRef}.
 */
class MessageExchangeRefImpl implements MessageExchangeRef, Serializable {
  
	private static final long serialVersionUID = -888L;
	private String _channel;
  private SystemUUID _system;
  private String _instanceId;

  MessageExchangeRefImpl(MessageExchangeImpl me) {
    _channel = me.getChannelName();
    _system = me.getSystemUUID();
    _instanceId = me.getInstanceId();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchangeRef#destroy()
   */
  public void destroy() {
    resolve();
  }

  public String getInstanceId() {
    return _instanceId;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchangeRef#resolve()
   */
  public MessageExchange resolve() {
    ChannelBackend channel = DomainNodeImpl.getActiveDomain()
                                       .findChannelBackend(_system, _channel);
    return channel.resolveExchange(_instanceId);
  }

  public String toString() {
    return "{MexRef " + _instanceId + "}"; 
  }
}
