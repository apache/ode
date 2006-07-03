/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.evt.BpelEvent;

/**
 * Interface implemented by listeners of BPEL events.
 */
public interface BpelEventListener {

  /**
   * Event handling method.
   * @param event {@link BpelEvent}
   */
  void onEvent(BpelEvent event);
}
