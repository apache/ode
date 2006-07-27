/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.evt.BpelEvent;

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
