/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import java.io.Serializable;
import java.util.Date;

/**
 * The {@link ServiceEvent} used to indicate that a unit of work
 * previously scheduled should now be executed.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface ScheduledWorkEvent extends ServiceEvent {

  /**
   * Get the time for which this event is scheduled (should be close to now).
   *
   * @return scheduled time for event
   */
  public Date getDueDate();

  /**
   * Get the time when this event was orignially scheduled.
   *
   * @return time when the event was scheduled
   */
  public Date getCreateDate();

  /**
   * Get the work payload--an arbitrary serializable object.
   *
   * @return work payload
   */
  public Serializable getPayload();

  /**
   * Get a human-readable description of the work-event.
   * @return
   */
  public String getDescription();

}
