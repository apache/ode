/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk.hobj;

import com.fs.pxe.daohib.hobj.HLargeData;

import java.util.Date;

/**
 * Hibernate-managed table for keeping track of message exchanges.
 *
 * @hibernate.class
 *  table="PXE_MESSAGE_EXCHANGE"
 *  dynamic-update="true"
 */
public class HSfwkMessageExchange {
	
  private HLargeData _correlationId;
  private String _channelName;
  private String _operationName;
  private String _instanceId;
  private int _state;
  private Date _insertTime;
  private HSystem _system;
  private String _portType;
  private HLargeData _sourceEndpoint;
  private HLargeData _destEndpoint;
  private boolean _sourceEndpointSimpleType;
  private boolean _destinationEndpointSimpleType;

  private HSfwkMessage _input;
  private HSfwkMessage _output;
  private boolean _clientPinned;

  /**
	 * 
	 */
	public HSfwkMessageExchange() {
		super();
	}
  
  /**
   * @hibernate.property
   *    column="PORT_TYPE"
   */
	public String getPortType() {
		return _portType;
	}

	public void setPortType(String portType) {
		_portType = portType;
	}
  /**
   * @hibernate.many-to-one
   * @hibernate.column name="SYSTEM_ID" index="IDX_MESSAGE_EXCHANGE_SYSTEM"
   *    
   */
  public HSystem getSystem() {
    return _system;
  }
  
  public void setSystem(HSystem system) {
    _system = system;
  }
  
  /**
   * @hibernate.property
   *    column="CHANNEL_NAME"
   */
	public String getChannelName() {
		return _channelName;
	}

	public void setChannelName(String channelName) {
		_channelName = channelName;
	}

  /**
   * @hibernate.many-to-one column="LDATA_ID" cascade="delete"
   */
	public HLargeData getCorrelationId() {
		return _correlationId;
	}

	public void setCorrelationId(HLargeData correlationId) {
		_correlationId = correlationId;
	}
  /**
   * @hibernate.many-to-one column="LDATA_SEPR_ID" cascade="delete"
   */
  public HLargeData getSourceEndpoint() {
    return _sourceEndpoint;
  }

  public void setSourceEndpoint(HLargeData endpoint) {
    _sourceEndpoint = endpoint;
  }

  /**
   * @hibernate.many-to-one column="LDATA_DEPR_ID" cascade="delete"
   */
  public HLargeData getDestinationEndpoint() {
    return _destEndpoint;
  }

  public void setDestinationEndpoint(HLargeData endpoint) {
    _destEndpoint = endpoint;
  }

  /**
   * @hibernate.property column="SENDPOINT_SIMPLE_TYPE"
   */
  public boolean isSourceEndpointSimpleType() {
    return _sourceEndpointSimpleType;
  }

  public void setSourceEndpointSimpleType(boolean endpointSimpleType) {
    _sourceEndpointSimpleType = endpointSimpleType;
  }

  /**
   * @hibernate.property column="DENDPOINT_SIMPLE_TYPE"
   */
  public boolean isDestinationEndpointSimpleType() {
    return _destinationEndpointSimpleType;
  }

  public void setDestinationEndpointSimpleType(boolean endpointSimpleType) {
    _destinationEndpointSimpleType = endpointSimpleType;
  }

  /**
   * @hibernate.many-to-one
   *    column="INPUT_MESSAGE_ID"
   *    cascade="delete"
   */
	public HSfwkMessage getInputMessage() {
		return _input;
	}

	public void setInputMessage(HSfwkMessage inputMessage) {
		_input = inputMessage;
	}
  
  /**
   * @hibernate.many-to-one
   *    column="OUTPUT_MESSAGE_ID"
   *    cascade="delete"
   */
  public HSfwkMessage getOutputMessage() {
    return _output;
  }

  public void setOutputMessage(HSfwkMessage outputMessage) {
    _output = outputMessage;
  }
  
  /**
   * @hibernate.property
   *    column="INSERT_DT"
   */
	public Date getInsertTime() {
		return _insertTime;
	}
	public void setInsertTime(Date insertTime) {
		_insertTime = insertTime;
	}
  
  /**
   * @hibernate.id
   *    column="ID"
   *    unique="true"
   *    generator-class="assigned"
   */
	public String getInstanceId() {
		return _instanceId;
	}
	public void setInstanceId(String instanceId) {
		_instanceId = instanceId;
	}
  
  /**
   * @hibernate.property
   *    column="OP_NAME"
   */
	public String getOperationName() {
		return _operationName;
	}

	public void setOperationName(String operationName) {
		_operationName = operationName;
	}

  /**
   * @hibernate.property
   *    column="STATE"
   */
	public int getState() {
		return _state;
	}

	public void setState(int state) {
		_state = state;
	}

  /**
   * @hibernate.property
   *    column="PINNED"
   */
  public boolean getPinned() {
    return _clientPinned;
  }


  public void setPinned(boolean pinned) {
    _clientPinned = pinned;
  }

}
