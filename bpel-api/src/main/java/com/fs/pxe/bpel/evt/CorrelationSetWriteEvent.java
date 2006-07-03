/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import com.fs.pxe.bpel.common.CorrelationKey;

/**
 * Correlation was set event.
 * 
 */
public class CorrelationSetWriteEvent extends CorrelationSetEvent {
  private static final long serialVersionUID = 1L;
  private CorrelationKey _key;

  public CorrelationSetWriteEvent(String csetName, CorrelationKey key) {
    super(csetName);
    _key = key;
  }

  /**
   * Correlation key.
   * 
   * @return Returns the key.
   */
  public CorrelationKey getKey() {
    return _key;
  }

  public void setKey(CorrelationKey key) {
    _key = key;
  }

}
