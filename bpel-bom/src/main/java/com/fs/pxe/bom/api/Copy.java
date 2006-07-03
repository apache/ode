/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Assignment copy entry. Each copy entry consists of a
 * "left hand side" (L-value) and a "right hand side (R-value).
 * The value on the right hand side is copied to the location
 * referenced in the left hand side.
 */
public interface Copy extends BpelObject {

  /**
   * Get the L-value.
   *
   * @return the L-value.
   */
  To getTo();

  /**
   * Set the L-value.
   *
   * @param to the L-value
   */
  void setTo(To to);

  /**
   * Get the R-value.
   *
   * @return the R-value.
   */
  From getFrom();

  /**
   * Set the R-value.
   *
   * @param from the R-value
   */
  void setFrom(From from);

  boolean isKeepSrcElement();

  void setKeepSrcElement(boolean keepSrcElement);

}
