/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Message-exchange failure event: indicates that for some reason the message
 * exchange failed (i.e. the finite state machine for the message-exchange
 * pattern is in an error state).
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface MessageExchangeFailureEvent extends ClientMessageExchangeEvent {
  
}
