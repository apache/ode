/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.ClientMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.MessageExchange;

abstract class ClientMessageExchangeEventImpl extends MessageExchangeEventImpl
  implements ClientMessageExchangeEvent {

  protected ClientMessageExchangeEventImpl(MessageExchange messageExchange, short type) {
    super(messageExchange, type);
  }

}
