/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeFailureEvent;

class MessageExchangeFailureEventImpl extends MessageExchangeEventImpl implements MessageExchangeFailureEvent {

  public MessageExchangeFailureEventImpl(MessageExchange mex) {
    super(mex, FAILURE);
  }
  
}
