/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;


/**
 * Base class for msgs that occur on the "client side" of a message
 * exchange (e.g. an {@link OutputRcvdMessageExchangeEvent}).
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface ClientMessageExchangeEvent extends MessageExchangeEvent {
}
