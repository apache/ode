/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import javax.xml.namespace.QName;

public class CorrelationEvent extends ProcessEvent {
  private static final long serialVersionUID = 1L;

  private QName _portType;
  private String _operation;
  private String _mexId;

  public CorrelationEvent() {
    super();
  }

  public CorrelationEvent(QName portType, String operation, String mexId) {
    _portType = portType;
    _operation = operation;
    _mexId = mexId;
  }

  /** Message exchange port type*/
	public QName getPortType() {
    return _portType;
  }

	/** Message exchange operation */
	public String getOperation() {
    return _operation;
  }

  /** Message exchange id */
	public String getMessageExchangeId() {
    return _mexId;
  }

  public void setMexId(String mexId) {
    _mexId = mexId;
  }

  public void setOperation(String operation) {
    _operation = operation;
  }

  public void setPortType(QName portType) {
    _portType = portType;
  }
}
