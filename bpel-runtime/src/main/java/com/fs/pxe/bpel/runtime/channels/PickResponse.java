/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

import org.w3c.dom.Element;

/**
 * Response channel for pick requests.
 * @jacob.kind 
 * @jacob.parent TimerResponseChannel
 */
public interface PickResponse extends TimerResponse{

  public void onRequestRcvd(int selectorIdx, String mexId);

}
