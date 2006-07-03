/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel.hobj;

import com.fs.pxe.daohib.hobj.HObject;

/**
 * Hibernate table for representing the pre-computed keys for a message
 * targetted at the BPEL process with no matching instance at the time of
 * receipt (and createInstance is not possible).
 *
 * @hibernate.class
 *   table="BPEL_CORRELATOR_MESSAGE_CKEY"
 */
public class HCorrelatorMessageKey extends HObject {

  /** Correlation Key canonical string representation. */
  private String _keyCanonical;

  private HCorrelatorMessage _owner;

  /** Constructor. */
  public HCorrelatorMessageKey() {
    super();
  }

  /**
   * Canonical string representation of the correlation key.
   *
   * @hibernate.property
   *   column="CKEY"
   *   not-null="true"
   * @hibernate.column
   *   name="CKEY"
   *   index="IDX_BPEL_CORRELATOR_MESSAGE_CKEY"
   */
  public String getCanonical() {
    return _keyCanonical;
  }

  /** @see #getCanonical()  */
  public void setCanonical(String canonical) {
    _keyCanonical = canonical;
  }

  /**
   * The message with which this correlation key value is associated.
   * @hibernate.many-to-one
   *   column="CORRELATOR_MESSAGE_ID"
   */
  public HCorrelatorMessage getOwner() {
    return _owner;
  }

  public void setOwner(HCorrelatorMessage owner) {
    _owner = owner;
  }
}
