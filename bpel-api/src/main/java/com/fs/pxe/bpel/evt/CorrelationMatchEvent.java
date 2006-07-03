/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import javax.xml.namespace.QName;

import com.fs.pxe.bpel.common.CorrelationKey;

/**
 * Correlation matched a process instance on inbound message.
 */
public class CorrelationMatchEvent extends ProcessMessageExchangeEvent {
  private static final long serialVersionUID = 1L;
  private CorrelationKey _correlationKey;

  public CorrelationMatchEvent(
    QName processName, QName processId, Long processInstanceId, CorrelationKey correlationKey) {
    super(PROCESS_INPUT, processName,processId,processInstanceId);
    _correlationKey = correlationKey;
  }

	public CorrelationKey getCorrelationKey() {
    return _correlationKey;
  }

  public void setCorrelationKey(CorrelationKey correlationKey) {
    _correlationKey = correlationKey;
  }
  
}
