/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Event used to inform the service provider of a in-progress message exchange
 * in the event of system recovery.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface RecoverMessageExchangeEvent extends MessageExchangeEvent {
}
