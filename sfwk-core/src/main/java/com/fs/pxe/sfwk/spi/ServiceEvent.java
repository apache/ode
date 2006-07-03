/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * An event destined for a PXE Service hosted within a PXE Service Provider.
 * Instances of this class are created by the PXE container framework, and are consumed
 * by {@link ServiceProvider}s through the
 * {@link ServiceProvider#onServiceEvent(MessageExchangeEvent)} method.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface ServiceEvent {
  /** Event Type: Output fault received.*/
  short OUT_FAULT_EVENT = 1;
  /** Event Type: Output message received. */
  short OUT_RCVD_EVENT = 2;
  /** Event Type: Input fault received (WSDL 1.2, unsupported). */
  short IN_FAULT_EVENT = 3;
  /** Event Type: Input message received. */
  short IN_RCVD_EVENT = 4;
  /** Event Type: Scheduled Work Event. */
  short SCHEDULED_WORK_EVENT = 5;
  /** Event Type: Recovery notification. */
  short RECOVER = -100;
  /** Event Type: Message exchanged failure advisory received. */
  short FAILURE = -1;


  /**
   * Get the target service. Naturally, the returned {@link ServiceContext} will have
   * previously been started on the target {@link ServiceProvider} instance.
   *
   * @return target service.
   */
  ServiceContext getTargetService() ;

  /**
   * Get the event type.
   *
   * @return event type.
   */
  short getEventType();
}
