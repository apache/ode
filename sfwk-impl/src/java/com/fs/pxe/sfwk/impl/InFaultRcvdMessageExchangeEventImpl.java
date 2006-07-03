/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.InFaultRcvdMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.Message;
import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeEvent;

final class InFaultRcvdMessageExchangeEventImpl
  extends ServerMessageExchangeEventImpl implements InFaultRcvdMessageExchangeEvent {
  private Message _infaultMessage;

  /**
   * Constructor.
   * 
   * @param messageExchange the <code>MessageExchange</code> which has
   * received the input fault message
   * @param infaultMessage the input fault message
   */
  public InFaultRcvdMessageExchangeEventImpl(MessageExchange messageExchange,
                                         Message infaultMessage) {
    super(messageExchange, MessageExchangeEvent.IN_FAULT_EVENT);
    _infaultMessage = infaultMessage;
  }

  public Message getInFaultMessage() {
    return _infaultMessage;
  }
}
