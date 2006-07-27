/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Signals expression evaluation triggered a fault.
 */
public class ExpressionEvaluationFailedEvent extends ExpressionEvaluationEvent {
  private static final long serialVersionUID = 1L;

  private QName _fault;

  /** fault qname */
	public QName getFault() {
    return _fault;
  }

  public void setFault(QName fault) {
    _fault = fault;
  }

}
