/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Signals completion of the process.
 * 
 */
public class ProcessCompletionEvent extends ProcessInstanceEvent {
  private static final long serialVersionUID = 1L;

  private QName _fault;

  public ProcessCompletionEvent() {
    super();
  }

  public ProcessCompletionEvent(QName faultName) {
    super();
    _fault = faultName;
  }

  /**
   * if the process finished with a fault, this will return the fault name,
   * otherwise this will be <code>null</code>
   * 
   * @return
   */
  public QName getFault() {
    return _fault;
  }

  public void setFault(QName fault) {
    _fault = fault;
  }

}
