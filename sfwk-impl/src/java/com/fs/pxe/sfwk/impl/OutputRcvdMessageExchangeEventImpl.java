/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.Message;
import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.OutputRcvdMessageExchangeEvent;

final class OutputRcvdMessageExchangeEventImpl
  extends ClientMessageExchangeEventImpl implements OutputRcvdMessageExchangeEvent {
  private Message _outputMessage;

  /**
   * Constructor.
   * @param messageExchange the <code>MessageExchange</code> which has received 
   * the output message
   * @param	outputMessage the message
   */
  public OutputRcvdMessageExchangeEventImpl(MessageExchange messageExchange,
                                        Message outputMessage) {
    super(messageExchange, OUT_RCVD_EVENT);
    _outputMessage = outputMessage;
  }

  public Message getOutputMessage() {
    return _outputMessage;
  }
}
