/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.utils.ObjectPrinter;


/**
 * Domain message representing message-exchange events. The implementation of
 * this interface should be a wire-transportable representation of a message
 * exchange event.
 */
class MessageExchangeTask extends SystemTask {

	/** Message is an input (request). */
  public static final short MSGTYPE_INPUT = 0x11;

  /** Message is an input fault (unsupported). */
  public static final short MSGTYPE_INFAULT = 0x12;

  /** Message is an output (reply). */
  public static final short MSGTYPE_OUTPUT = 0x21;

  /** Message is an output fault (fault). */
  public static final short MSGTYPE_OUTFAULT = 0x22;

  /** Message is a failure message. */
  public static final short MSGTYPE_FAILURE = 0xff;

  /** Message instructing a recovery. */
  public static final short MSGTYPE_RECOVER = 0xd0;

  /** The message is travelling to the "server". */
  public static final short DIR_TO_SERVER = 0x10;

  /** The message is travelling to the "client". */
  public static final short DIR_TO_CLIENT = 0x20;

	private String _op;
	private short _messageType;
  private short _direction;
	private String _instanceId;
	private String _faultType;
  private String _channelName;

  /**
   * Constructor.
	 * @param systemUUID
	 * @param domain
	 */
	public MessageExchangeTask(short direction, SystemUUID systemUUID, String domain) {
		super(systemUUID, domain);
    _direction = direction;
	}


  /**
   * Get the direction of the message exchange event: either {@link
   * MessageExchangeMsg#DIR_TO_SERVER}  for messages travelling from
   * the client to server or {@link MessageExchangeTask#DIR_TO_CLIENT}
   * for messages travelling from server to client. The direction of the
   * event is inferred from the type of event; input and infault events are
   * client-to-server messages while output and outfault events are
   * server-to-client messages.
   *
   * @return direction of the message.
   */
  public short getDirection(){
  	return _direction;
  }
  

  /**
   * Setter: fault type.
   *
   * @param faultType name (type) of fault.
   */
  public void setFaultType(String faultType){
  	_faultType = faultType;
  }

  /**
   * Getter: fault type
   *
   * @return name (type) of faultS
   */
  public String getFaultType(){
  	return _faultType;
  }

  /**
   * Setter: message exchange instance identifier.
   */
  public void setInstanceId(String instanceId){
  	_instanceId = instanceId;
  }

  /**
   * Setter: instance ID
   *
   * @return instance ID
   */
  public String getInstanceId(){
  	return _instanceId;
  }

  /**
   * Getter: message type.
   *
   * @return message type
   */
  public short getMessageType(){
  	return _messageType;
  }

  /**
   * Setter: message type.
   * @param msgType message type
   */
  public void setMsgType(short msgType){
  	_messageType = msgType;
  }

  /**
   * Setter: operation name.
   * @param operation operation name
   */
  public void setOperation(String operation){
  	_op = operation;
  }

  /**
   * Getter: operation name.
   *
   * @return operation name
   */
  public String getOperation(){
  	return _op;
  }


  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "instanceId",_instanceId,
      "operation",_op
    });
  }

  public void setChannelName(String channelName) {
    _channelName = channelName;
  }

  public String getChannelName() {
    return _channelName;
  }
}
