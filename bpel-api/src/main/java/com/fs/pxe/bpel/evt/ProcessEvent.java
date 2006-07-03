/*
 * File: $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */
package com.fs.pxe.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Base class for process events.
 */
public abstract class ProcessEvent extends BpelEvent {

  private QName _processId;
  private QName _processname;

  public ProcessEvent() {
  }


  public void setProcessId(QName processId) {
    _processId = processId;
  }

  public QName getProcessId() {
    return _processId;
  }

  public void setProcessName(QName processName) {
    _processname = processName;
  }

  /**
   * Gets process name.
   * 
   * @return the process name
   */
  public QName getProcessName() {
    return _processname;
  }

}
