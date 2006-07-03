/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.fs.pxe.bpel.o.OElementVarType;
import com.fs.pxe.bpel.o.OMessageVarType;
import com.fs.pxe.bpel.o.OVarType;
import com.fs.utils.SerializableElement;


/**
 * Information about a BPEL fault.
 */
public class FaultData implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Name of the fault. */
  private QName _faultName;

  /** MessageType of the fault. */
  private OVarType _faultVarType;

  private SerializableElement _faultMsg;

  private int _lineNo;

	private final String _explanation;

  public FaultData(QName fault, int lineNo, String explanation) {
    _faultName = fault;
    _lineNo = lineNo;
    _explanation = explanation;
  }

  public FaultData(QName fault, Element faultMsg, OVarType faultVarType, int lineNo) {
    this(fault, lineNo, null);
    assert faultMsg != null;
    assert faultVarType != null;
    assert faultVarType instanceof OMessageVarType || faultVarType instanceof OElementVarType;
    _faultMsg = new SerializableElement(faultMsg);
    _faultVarType = faultVarType;
  }

  /**
   * Return potential message associated with fault.
   * Null if no fault data.
   *
   * @return
   */
  public Element getFaultMessage() {
    return (_faultMsg == null)
           ? null
           : _faultMsg.getElement();
  }
  
  /**
   * The message type of the fault message data.  Null if no fault data.
   * @return
   */
  public OVarType getFaultType(){
  	return _faultVarType;
  }

  /**
   * Get the fault name.
   *
   * @return qualified fault name.
   */
  public QName getFaultName() {
    return _faultName;
  }
  
  public int getFaultLineNo(){
  	return _lineNo;
  }
  
  public String getExplanation() {
  	return _explanation;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString(){
    StringBuilder sb = new StringBuilder("FaultData: [faultName=");
    sb.append(_faultName);
    sb.append(", faulType=");
    sb.append(_faultVarType);
    if (_explanation != null) {
    	sb.append(" (");
    	sb.append(_explanation);
    	sb.append(")");
    }
    
    sb.append("] @");
    sb.append(_lineNo);
    return sb.toString();
  }

}
