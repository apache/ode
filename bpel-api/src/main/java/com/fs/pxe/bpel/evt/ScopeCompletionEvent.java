/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Activity completion event.
 */
public class ScopeCompletionEvent extends ScopeEvent {
  private static final long serialVersionUID = 1L;

  private boolean _success;
  private QName _fault;
  
  public ScopeCompletionEvent(boolean success, QName fault) {
    _success = success;
    _fault = fault;
  }

	/**
	 * @param fault The fault to set.
	 */
	public void setFault(QName fault) {
		_fault = fault;
	}

	/**
	 * @return Returns the fault.
	 */
	public QName getFault() {
		return _fault;
	}
  
	public boolean isSuccess() {
		return _success;
	}
	public void setSuccess(boolean success) {
		_success = success;
	}
}
