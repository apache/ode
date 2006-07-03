/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.ServerMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.ServicePort;

/**
 * Base class for events that occur on the "server-side"  of a message exchange.
 */
abstract class ServerMessageExchangeEventImpl extends MessageExchangeEventImpl
       implements ServerMessageExchangeEvent
 {
  protected ServerMessageExchangeEventImpl(MessageExchange messageExchange,
                                       short type) {
    super(messageExchange, type);
  }

  public ServicePort getPort() {
    return getMessageExchange().getServerPort();
  }

}
