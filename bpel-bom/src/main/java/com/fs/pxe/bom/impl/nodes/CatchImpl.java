/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Catch;
import com.fs.utils.NSContext;

import javax.xml.namespace.QName;


/**
 * Normalized representation of a BPEL fault catch block (i.e. the
 * <code>catch</code> and <code>catchAll</code> constructs). A catch block
 * consists of an activity that is executed when the block is activated, a
 * fault name that determines which faults the catch block can handle, and a
 * variable which is used to hold fault data. The fault name and fault
 * variable are optional, the lack of a fault name indicates a catch-all
 * block, and the lack of a variable means that the fault data will not be
 * accessible. Note that a catch block is itself a scope; this is to provide
 * a context in which to place the fault variable.
 */
public class CatchImpl extends ScopeImpl implements Catch {

  private static final long serialVersionUID = -1L;

  private QName _faultName;
  private String _faultVariable;
  private QName _faultVariableMessageType;
  private QName _faultVariableElementType;
  private ActivityImpl _activity;

  /**
   * Constructor.
   */
  public CatchImpl(NSContext nsContext) {
    super(nsContext);
  }

  public CatchImpl() {
    super();
  }

  public String getType() {
    return "catch";
  }

  public QName getFaultVariableMessageType() {
    return _faultVariableMessageType;
  }

  public void setFaultVariableMessageType(QName faultVariableType) {
    _faultVariableMessageType = faultVariableType;
  }

  public Activity getActivity() {
    return _activity;
  }

  public void setActvity(Activity activity) {
    _activity = (ActivityImpl) activity;
  }

  public void setActvity(ActivityImpl activity) {
    _activity = activity;
  }

  public void setFaultName(QName name) {
    _faultName = name;
  }

  public QName getFaultName() {
    return _faultName;
  }

  public String getFaultVariable() {
    return _faultVariable;
  }

  public void setFaultVariable(String faultVariable) {
    _faultVariable = faultVariable;
  }

	/**
	 * @see com.fs.pxe.bom.api.Catch#getFaultVariableElementType()
	 */
	public QName getFaultVariableElementType() {
		return _faultVariableElementType;
	}

	/**
	 * @see com.fs.pxe.bom.api.Catch#setFaultVariableElementType(javax.xml.namespace.QName)
	 */
	public void setFaultVariableElementType(QName faultVariableType) {
		_faultVariableElementType = faultVariableType;
	}


}
