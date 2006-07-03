/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import javax.xml.namespace.QName;

/**
 *
 */
public class ProcessMessageExchangeEvent extends ProcessInstanceEvent {
  
  private static final long serialVersionUID = 1L;
  
  public static final short PROCESS_INPUT = 0;
  public static final short PROCESS_OUTPUT = 1;
  public static final short PROCESS_FAULT = 2;
  public static final short PARTNER_INPUT = 3;
  public static final short PARTNER_OUTPUT = 4;
  public static final short PARTNER_FAULT = 5;
  public static final short PARTNER_FAILURE = 6;
  
  
  private QName _portType;
  private String _operation;
  private String _mexId;
  private short _aspect;

  public ProcessMessageExchangeEvent() {}

  public ProcessMessageExchangeEvent(short aspect,
    QName processName,
    QName processId, Long processInstanceId) {
    super(processInstanceId);
    setProcessName(processName);
    setProcessId(processId);
    _aspect = aspect;
  }
  
  public short getAspect() {
    return _aspect;
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
  
  public void setAspect(short aspect) {
    _aspect = aspect;
  }

  public void setMexId(String mexId) {
    _mexId = mexId;
  }

  public void setOperation(String operation) {
    _operation = operation;
  }

  public void setPortType(QName portType) {
    _portType = portType;
  }}
