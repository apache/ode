/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * BOM representation of a BPEL fault handler, i.e. a collection of catch blocks.
 */
public interface FaultHandler extends BpelObject {
  /**
   * Get the scope to which this fault handler belongs.
   *
   * @return scope owner scope
   */
  Scope getScope();

  /**
   * Gets the {@link Catch} blocks for this fault handler.
   *
   * @return array of {@link Catch} blocks
   */
  Catch[] getCatches();

  /**
   * Adds a {@link Catch} to the list of catch blocks.
   *
   * @param catchBlock catch block
   */
  void addCatch(Catch catchBlock);

}
