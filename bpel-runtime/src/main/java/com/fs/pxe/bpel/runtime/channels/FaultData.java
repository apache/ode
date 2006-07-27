/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

import com.fs.pxe.bpel.o.OBase;
import com.fs.pxe.bpel.o.OElementVarType;
import com.fs.pxe.bpel.o.OMessageVarType;
import com.fs.pxe.bpel.o.OVarType;
import com.fs.utils.SerializableElement;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.Serializable;


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

  private OBase _location;

	private final String _explanation;

  public FaultData(QName fault, OBase location, String explanation) {
    _faultName = fault;
    _location = location;
    _explanation = explanation;
  }

  public FaultData(QName fault, Element faultMsg, OVarType faultVarType, OBase location) {
    this(fault, location, null);
    assert faultMsg != null;
    assert faultVarType != null;
    assert faultVarType instanceof OMessageVarType || faultVarType instanceof OElementVarType;
    _faultMsg = new SerializableElement(faultMsg);
    _faultVarType = faultVarType;
  }

  /**
   * Return potential message associated with fault.
   * Null if no fault data.
   * @return fault message Element
   */
  public Element getFaultMessage() {
    return (_faultMsg == null)
           ? null
           : _faultMsg.getElement();
  }
  
  /**
   * The message type of the fault message data.  Null if no fault data.
   * @return fault type
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
  	return findLineNo(_location);
  }
  
  public String getExplanation() {
  	return _explanation;
  }

  public int getActivityId() {
    return _location.getId();
  }

  /**
   * Find the best line number for the given location.
   * @param location
   * @return line number
   */
  protected int findLineNo(OBase location) {
  	if (location == null)
  		return -1;
  	if (location.debugInfo == null)
  		return -1;
  	return location.debugInfo.startLine;
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
    sb.append(findLineNo(_location));
    return sb.toString();
  }

}
