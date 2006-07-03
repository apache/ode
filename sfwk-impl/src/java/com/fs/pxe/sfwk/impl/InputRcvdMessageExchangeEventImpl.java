/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.InputRcvdMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.Message;
import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeEvent;


final class InputRcvdMessageExchangeEventImpl
  extends ServerMessageExchangeEventImpl implements InputRcvdMessageExchangeEvent {
  private Message _inputMessage;

  /**
   * Constructor.
   * @param messageExchange the <code>MessageExchange</code> that has received an
   * input message
   * @param inputMessage the input message received
   */
  public InputRcvdMessageExchangeEventImpl(MessageExchange messageExchange,
                                       Message inputMessage) {
    super(messageExchange, MessageExchangeEvent.IN_RCVD_EVENT);
    _inputMessage = inputMessage;
  }

  public Message getInputMessage() {
    return _inputMessage;
  }
}
