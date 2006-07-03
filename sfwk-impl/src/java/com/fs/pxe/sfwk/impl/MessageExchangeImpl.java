/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.bapi.dao.MessageDAO;
import com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO;
import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.ObjectPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import java.io.UnsupportedEncodingException;


/**
 * Framework implementation of the <code>MessageExchange</code> interface.
 */
class MessageExchangeImpl implements MessageExchange {

	private final static Log __log = LogFactory.getLog(MessageExchangeImpl.class);

	ChannelBackend _channelBackend;
  private String _instanceId;
  private Operation _operationDescription;
  private Message _lastInput;
  private Message _lastOutput;
  private MessageExchangeDAO _dao;
  private ServiceEndpoint _sourceEndpoint;
  private ServiceEndpoint _destinationEndpoint;

  MessageExchangeImpl(ChannelBackend channel, ServiceEndpoint sourceEndpoint, ServiceEndpoint destEndpoint,
                      MessageExchangeDAO meDAO, Operation operationDescription) {
    _dao = meDAO;
    _instanceId = _dao.getInstanceId();
    _channelBackend = channel;
    _operationDescription = operationDescription;
    _sourceEndpoint = sourceEndpoint;
    _destinationEndpoint = destEndpoint;
  }

  SystemUUID getSystemUUID() {
    return _channelBackend.getSystemUUID();
  }

  String getChannelName() {
    return _channelBackend.getName();
  }

  public PortType getPortType() {
    return _channelBackend.getPortType();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#getInstanceId()
   */
  public String getInstanceId() {
    return _instanceId;
 }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#getCorrelationId()
   */
  public String getCorrelationId(){
    byte[] correlationId = _dao.getCorrelationId();
    try {
      return correlationId == null
				? null
				: new String(correlationId,"UTF-8");
    } catch (UnsupportedEncodingException e) {
      // Inexcusable.
      throw new Error("Platform Error: UTF-8 Not Supported!", e);
    }
  }



  public byte[] getCorrelationIdBytes() {
    return _dao.getCorrelationId();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#getName()
   */
  public String getName() {
    return _dao.getOperationName();
  }

  public Operation getOperation() {
    return _operationDescription;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#getReference()
   */
  public MessageExchangeRef getReference() {
    pin();
    return new MessageExchangeRefImpl(this);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#getSourceServiceEndpoint()
   */
  public ServiceEndpoint getSourceServiceEndpoint() {
    return _sourceEndpoint;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#getDestinationServiceEndpoint()
   */
  public ServiceEndpoint getDestinationServiceEndpoint() {
    return _destinationEndpoint;
  }

  /**
   * Note: This does not support multiple input message (WSDL 1.2).
   *
   * @see com.fs.pxe.sfwk.spi.MessageExchange#createInputMessage()
   */
  public Message createInputMessage()
                             throws IllegalStateException {
    return new MessageImpl(_operationDescription.getInput().getMessage(), _dao.createMessage());
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#createOutfaultMessage(java.lang.String)
   */
  public Message createOutfaultMessage(String fault)
                                throws IllegalStateException {
    Fault falt = _operationDescription.getFault(fault);
    if(fault == null)
      throw new IllegalArgumentException("No such fault defined in WSDL: '" + fault + "'");
    return new MessageImpl(falt.getMessage(), _dao.createMessage());
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#createOutputMessage()
   */
  public Message createOutputMessage()
                              throws IllegalStateException {
    // TODO: handle case of multiple output messages (WSDL 1.2)
    return new MessageImpl(_operationDescription.getOutput().getMessage(), _dao.createMessage());
  }

  public void dontKnow() {
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#input(com.fs.pxe.sfwk.spi.Message)
   */
  public void input(Message inputMessage)
             throws MessageExchangeException, MessageFormatException {
  	if(__log.isTraceEnabled())
  		__log.trace(ObjectPrinter.stringifyMethodEnter("input", new Object[] {"inputMessage",inputMessage}));

    if (_dao.getState() != MessageExchangeDAO.STATE_START)
      throw new MessageExchangeException("Invalid message-exchange state:  input message can only be sent from START state!");
    _channelBackend.sendInputMsg(this, (MessageImpl)inputMessage);
    _dao.setState(MessageExchangeDAO.STATE_CLIENT_SENT_INPUTMSG);
  }

  public void failure(String description) throws MessageExchangeException  {
    _channelBackend.sendFailure(this,description);
    _dao.setState(MessageExchangeDAO.STATE_FAILED);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#outfault(java.lang.String, com.fs.pxe.sfwk.spi.Message)
   */
  public void outfault(String faultType, Message outputFaultMessage)
                throws MessageExchangeException, MessageFormatException {

  	if(__log.isTraceEnabled())
  		__log.trace(ObjectPrinter.stringifyMethodEnter("outfault", new Object[] {
        "faultType", faultType,
        "outputFaultMessage", outputFaultMessage
      }));

    if (_dao.getState() != MessageExchangeDAO.STATE_SERVER_RCVD_INPUTMSG)
      throw new MessageExchangeException("Invalid message-exchange state: state must be SERVER_RCVD_INPUTMSG to send output fault messages!");

    _channelBackend.sendOutfaultMsg(this, faultType, (MessageImpl)outputFaultMessage);
    _dao.setState(MessageExchangeDAO.STATE_SERVER_SENT_OUTFAULTMSG);
    _dao.setPinned(false);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#output(com.fs.pxe.sfwk.spi.Message)
   */
  public void output(Message outputMessage)
              throws MessageExchangeException, MessageFormatException {

    if(__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("output", new Object[] {
        "outputMessage", outputMessage
      }));

    if (_dao.getState() != MessageExchangeDAO.STATE_SERVER_RCVD_INPUTMSG)
      throw new MessageExchangeException("Invalid message-exchange state: state must be SERVER_RCVD_INPUTMSG to send output messages!");

    _channelBackend.sendOutputMsg(this, (MessageImpl)outputMessage);
    _dao.setState(MessageExchangeDAO.STATE_SERVER_SENT_OUTPUTMSG);
    _dao.setPinned(false);
  }

  public void pin() {
    _dao.setPinned(true);
  }

  public void release() {
    _dao.setPinned(false);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#lastOutput()
   */
  public Message lastFault(String fault)
                    throws MessageExchangeException {
    if (_lastOutput != null) {
      return _lastOutput;
    }

    return getLast(false, fault);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#lastInput()
   */
  public Message lastInput()
                    throws MessageExchangeException {
    if (_lastInput != null) {
      return _lastInput;
    }

    return getLast(true, null);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.MessageExchange#lastOutput()
   */
  public Message lastOutput()
                     throws MessageExchangeException {
    if (_lastOutput != null) {
      return _lastOutput;
    }

    return getLast(false, null);
  }

  public ServicePort getClientPort() {
    return _channelBackend.getClientPort();
  }

  public ServicePort getServerPort() {
    return _channelBackend.getServerPort();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "MsgExch[id=" + _instanceId + ", channel=" + _channelBackend.getName()
           + ", op=" + getName() + "]";
  }

  void setLastInput(Message input) {
    _lastInput = input;
  }

  void setLastOutput(Message output) {
    _lastOutput = output;
  }

  private Message getLast(boolean input, String fault) {

    javax.wsdl.Message msgDef = (input)
                     ? _operationDescription.getInput()
                                           .getMessage()
                     : ((fault != null)
                        ? _operationDescription.getFault(fault)
                                              .getMessage()
                        : _operationDescription.getOutput()
                                              .getMessage());
    MessageDAO msgDAO = (input)
                        ? _dao.getInputMessage()
                        : _dao.getOutputMessage();

    if (msgDAO == null)
      return null;

    return new MessageImpl(msgDef,msgDAO);
  }

}
