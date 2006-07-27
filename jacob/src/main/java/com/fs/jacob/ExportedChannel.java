/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob;

public interface ExportedChannel extends Channel {
  /**
   * Destroy the exported channel reference. It is improtant to destroy the
   * references of exported channels, otherwise the JACOB system will not be
   * able to detect that they are no longer being used.
   */
  public void destroyReference();
}
