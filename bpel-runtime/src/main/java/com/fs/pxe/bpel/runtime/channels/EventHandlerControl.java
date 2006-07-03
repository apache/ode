/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

/**
 * Channel used to control processing of event handler activities.
 * @jacob.kind
 */
public interface EventHandlerControl {

  /**
   * Finish up active events but stop processing any more.
   */
  void stop();

}
