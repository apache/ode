/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime.channels;

/**
 * Channel for timer notification. 
 * @jacob.kind
 */
public interface TimerResponse {
	/** timer event has occurred */
	public void onTimeout();

  /** timer was cancelled. */
  public void onCancel();

}
