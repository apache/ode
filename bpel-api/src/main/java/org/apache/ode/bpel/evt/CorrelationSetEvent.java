/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.evt;

/**
 * Correlation set event.
 */
public abstract class CorrelationSetEvent extends ScopeEvent {
  public String _correlationSetName;

  public CorrelationSetEvent() {
    super();
  }

  public CorrelationSetEvent(String csetName) {
    super();
    _correlationSetName = csetName;
  }

  public String getCorrelationSetName() {
    return _correlationSetName;
  }

  public void setCorrelationSetName(String correlationSetName) {
    _correlationSetName = correlationSetName;
  }

  public TYPE getType() {
    return TYPE.dataHandling;
  }
  
}
