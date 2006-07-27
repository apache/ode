/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Event indicating that an activity completed with a fault.
 */
public class ScopeFaultEvent
  extends ScopeEvent {
  
	private static final long serialVersionUID = 1L;
	private QName _faultType;
  private int _faultLineNo = -1;
  private String _explanation;

  public ScopeFaultEvent() {
    super();
  }

	public int getFaultLineNo() {
		return _faultLineNo;
	}
	public void setFaultLineNo(int faultLineNo) {
		_faultLineNo = faultLineNo;
	}
  public ScopeFaultEvent(QName faultType, int lineNo, String explanation) {
    _faultType = faultType;
    _faultLineNo = lineNo;
    _explanation = explanation;
  }


  /**
	 * Get the fault type.
	 * @return the fault type
	 */
	public QName getFaultType() {
    return _faultType;
  }

  public void setFaultType(QName faultType) {
    _faultType = faultType;
  }

	public String getExplanation() {
		return _explanation;
	}

	public void setExplanation(String explanation) {
		_explanation = explanation;
	}
}
