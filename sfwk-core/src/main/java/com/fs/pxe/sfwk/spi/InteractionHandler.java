/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

/**
 * Interface implemented by the service provider to handle interaction
 * "sessions".
 */
public interface InteractionHandler {

  /**
   * Close the session. Called to notify the {@link ServiceProvider}
   * that this handler can be destroyed or reclaimed.
   */
  public void close();
}
