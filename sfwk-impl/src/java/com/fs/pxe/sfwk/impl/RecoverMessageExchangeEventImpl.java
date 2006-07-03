/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeEvent;
import com.fs.pxe.sfwk.spi.RecoverMessageExchangeEvent;

class RecoverMessageExchangeEventImpl extends MessageExchangeEventImpl implements RecoverMessageExchangeEvent {
  /** Constructor. */
  public RecoverMessageExchangeEventImpl(MessageExchange messageExchange) {
    super(messageExchange, MessageExchangeEvent.RECOVER);
  }
}
