/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime.channels;

/**
 * Response channel for pick requests.
 * @jacob.kind 
 * @jacob.parent TimerResponseChannel
 */
public interface PickResponse extends TimerResponse{

  public void onRequestRcvd(int selectorIdx, String mexId);

}
