/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.Message;
import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.OutFaultRcvdMessageExchangeEvent;


final class OutFaultRcvdMessageExchangeEventImpl
  extends ClientMessageExchangeEventImpl implements OutFaultRcvdMessageExchangeEvent {
  private Message _outFaultMessage;
  private String _faultName;

  /**
   * Constructor.
   * @param messageExchange the <code>MessageExchange</code> that has received
   * an output fault message
   * @param outFaultMessage
   * @param faultName
   */
  public OutFaultRcvdMessageExchangeEventImpl(MessageExchange messageExchange,
                                          Message outFaultMessage,
                                          String faultName) {
    super(messageExchange, OUT_FAULT_EVENT);
    _outFaultMessage = outFaultMessage;
    _faultName = faultName;
  }

  public String getFaultName() {
    return _faultName;
  }

  public Message getOutFaultMessage() {
    return _outFaultMessage;
  }
}
