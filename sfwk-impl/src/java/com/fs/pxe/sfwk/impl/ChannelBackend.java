/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.bapi.dao.DomainStateConnection;
import com.fs.pxe.sfwk.bapi.dao.MessageDAO;
import com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO;
import com.fs.pxe.sfwk.bapi.dao.SystemDAO;
import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.uuid.UUIDGen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;


/**
 * Framework implementation of a communication channel. A channel is a link
 * between two server (export) and client (import) ports. Each channel
 * supports communication in both directions (from server to client and from
 * client to server).
 */
class ChannelBackend {
  private static Log __log = LogFactory.getLog(ChannelBackend.class);

  private PortType _portType;
  private String _channelName;
  private UUIDGen _uuidGen = new UUIDGen();
  private ServiceBackend _server;
  private ServiceBackend _client;
  private ServicePort _clientPort;
  private ServicePort _serverPort;
  private SystemBackend _system;
  private DomainNodeImpl _domain;

  ChannelBackend(SystemBackend system,String channelName, PortType portType) {
    _system = system;
    _domain = _system.getDomainNode();
    _channelName = channelName;
    _portType = portType;
  }

  boolean isBound() {
    return (_server != null) && (_client != null);
  }

  public String getName() {
    return _channelName;
  }

  public QName getPortTypeName() {
    return _portType.getQName();
  }

  void processMessageExchangeTask(MessageExchangeTask mexTask)
    throws DomainTaskProcessingException {
    assert mexTask.getInstanceId() != null;
    assert mexTask.getOperation() != null;

    ServiceBackend destination = (mexTask.getDirection() == MessageExchangeTask.DIR_TO_SERVER)
                                 ? getServer()
                                 : getClient();

    Operation operationDescription = getPortType().getOperation(mexTask.getOperation(), null, null);

    DomainStateConnection dconn = _domain.getDomainStoreConnection();
    SystemDAO systemDAO = dconn.findSystem(_system.getSystemUUID().toString());

    String msgExchangeId = mexTask.getInstanceId();
    MessageExchangeDAO mex = systemDAO.getMessageExchange(msgExchangeId);

    if (mex == null) {
      String msg = "Unable to locate message exchange, id='" + msgExchangeId + "'";
      __log.error(msg);
      throw new DomainTaskProcessingException(DomainTaskProcessingException.ACTION_COMMIT_AND_CONSUME, msg);
    }

    ServiceEndpoint destinationEndpoint = null;
    if (mex.getDestinationEndpoint() != null)
      destinationEndpoint = _server.getService().createServiceEndpoint(mex.getDestinationEndpoint());
    ServiceEndpoint sourceEndpoint = null;
    if (mex.getSourceEndpoint() != null)
      sourceEndpoint = _server.getService().createServiceEndpoint(mex.getSourceEndpoint());
    MessageExchangeImpl msgExchange = new MessageExchangeImpl(this, sourceEndpoint,
            destinationEndpoint, mex, operationDescription);

    MessageExchangeEventImpl event;
    MessageImpl msg;
    Message msgDef;
    MessageDAO msgDAO;

    switch (mexTask.getMessageType()) {
      case MessageExchangeTask.MSGTYPE_INPUT:
        msgDef = operationDescription.getInput().getMessage();
        mex.setState(MessageExchangeDAO.STATE_SERVER_RCVD_INPUTMSG);
        msgDAO = mex.getInputMessage();
        msg = new MessageImpl(msgDef, msgDAO);
        event = new InputRcvdMessageExchangeEventImpl(msgExchange,msg);
        break;

      case MessageExchangeTask.MSGTYPE_INFAULT:
        // Input faults are not supported.
        throw new UnsupportedOperationException("Input faults not supported; system corrupt.");

      case MessageExchangeTask.MSGTYPE_OUTPUT:
        msgDef = operationDescription.getOutput().getMessage();
        mex.setState(MessageExchangeDAO.STATE_CLIENT_RCVD_OUTFAULTMSG);
        msgDAO = mex.getOutputMessage();
        msg = new MessageImpl(msgDef, msgDAO);
        event = new OutputRcvdMessageExchangeEventImpl(msgExchange, msg);
        break;

      case MessageExchangeTask.MSGTYPE_OUTFAULT:
        msgDef = operationDescription.getFault(mexTask.getFaultType()).getMessage();
        mex.setState(MessageExchangeDAO.STATE_CLIENT_RCVD_OUTFAULTMSG);
        msgDAO = mex.getOutputMessage();
        msg = new MessageImpl(msgDef, msgDAO);
        assert operationDescription.getFault(mexTask.getFaultType()) != null;
        event = new OutFaultRcvdMessageExchangeEventImpl(msgExchange, msg, mexTask.getFaultType());
        break;

      case MessageExchangeTask.MSGTYPE_FAILURE:
        event = new MessageExchangeFailureEventImpl(msgExchange);
        mex.setState(MessageExchangeDAO.STATE_FAILED);
        break;

      case MessageExchangeTask.MSGTYPE_RECOVER:
        event = new RecoverMessageExchangeEventImpl(msgExchange);
        break;

      default:
        throw new IllegalArgumentException("unknown message exchange event type: "
                + mexTask.getMessageType());
    }

    if (mexTask.getDirection() == MessageExchangeTask.DIR_TO_SERVER) {
      event._targetPort = _serverPort;
    }
    else {
      event._targetPort = _clientPort;
    }

    destination.onServiceEvent(event);

    // Garbage collect the message exchange.
    switch (mex.getState()) {
      case MessageExchangeDAO.STATE_CLIENT_RCVD_OUTFAULTMSG:
      case MessageExchangeDAO.STATE_CLIENT_RCVD_OUTPUTMSG:
        if (!mex.isPinned()) {
          systemDAO.removeMessageExchange(mex.getInstanceId());
        }
        break;
      case MessageExchangeDAO.STATE_SERVER_RCVD_INPUTMSG:
        if (operationDescription.getOutput() == null && !mex.isPinned()) {
          systemDAO.removeMessageExchange(mex.getInstanceId());
        }
        break;
    }
  }

  void setClient(ServiceBackend client, ServicePort clientPort) {
    if (!clientPort.getPortType().getQName().equals(_portType.getQName())) {
      throw new IllegalArgumentException("PortType mismatch.");
    }

    _client = client;
    _clientPort = clientPort;
  }

  ServiceBackend getClient() {
    return _client;
  }

  ServicePort getClientPort() {
    return _clientPort;
  }

  PortType getPortType() {
    return _portType;
  }

  void setServer(ServiceBackend server, ServicePort serverPort) {
    if (!serverPort.getPortType().getQName().equals(_portType.getQName()))
      throw new IllegalArgumentException("PortType mismatch.");

    _server = server;
    _serverPort = serverPort;
  }

  ServiceBackend getServer() {
    return _server;
  }

  ServicePort getServerPort() {
    return _serverPort;
  }

  SystemUUID getSystemUUID() {
    return _system.getSystemUUID();
  }

  MessageExchange createMessageExchange(ServiceEndpoint sourceEpr, ServiceEndpoint destEpr,
                                          String operationName, byte[] correlationId)
          throws NoSuchOperationException, MessageExchangeException {
    Operation op = _portType.getOperation(operationName, null, null);

    if (op == null) {
      throw new NoSuchOperationException(_portType.getQName(), operationName);
    }

    DomainStateConnection dconn = _domain.getDomainStoreConnection();
    String instanceId = _uuidGen.nextUUID();

    SystemDAO systemDAO = dconn.findSystem(_system.getSystemUUID().toString());
    MessageExchangeDAO meDao = systemDAO.newMessageExchange(instanceId, sourceEpr == null ? null : sourceEpr.toXML(),
            destEpr == null ? null : destEpr.toXML(), op.getName(), _portType.getQName(), _channelName);
    meDao.setCorrelationId(correlationId);

    return new MessageExchangeImpl(this, sourceEpr, destEpr, meDao, op);
  }

  void sendInputMsg(MessageExchangeImpl messageExchange, MessageImpl inputMessage)
      throws MessageExchangeException, MessageFormatException {
    assert messageExchange._channelBackend == this;
    assert _server != null;

    inputMessage.checkValid();

    MessageExchangeTask mexEvent = new MessageExchangeTask(MessageExchangeTask.DIR_TO_SERVER, _system.getSystemUUID(), _system.getDomainNode().getDomainId());
    mexEvent.setMsgType(MessageExchangeTask.MSGTYPE_INPUT);
    send(mexEvent, messageExchange, inputMessage);

    //sendMngmtEvent(ChannelManagementEvent.IN_MSG, inputMessage);
  }

  void sendOutfaultMsg(MessageExchangeImpl messageExchange,
  										 String faultType,
                       MessageImpl outputFaultMessage)
                throws MessageExchangeException, MessageFormatException {
    assert messageExchange._channelBackend == this;
    assert _server != null;

    outputFaultMessage.checkValid();

    MessageExchangeTask mexEvent = new MessageExchangeTask(MessageExchangeTask.DIR_TO_CLIENT,_system.getSystemUUID(), _system.getDomainNode().getDomainId());
    
    mexEvent.setFaultType(faultType);

    mexEvent.setMsgType(MessageExchangeTask.MSGTYPE_OUTFAULT);
    send(mexEvent, messageExchange, outputFaultMessage);

    //sendMngmtEvent(ChannelManagementEvent.OUT_FAULT, outputFaultMessage);
  }
  
  MessageExchange resolveExchange(String instanceId) {
  	DomainStateConnection dconn = _domain.getDomainStoreConnection();
    SystemDAO system = dconn.findSystem(_system.getSystemUUID().toString());
    MessageExchangeDAO exchangeDAO = system.getMessageExchange(instanceId);
    String op = exchangeDAO.getOperationName();
    Operation opDesc = getPortType().getOperation(op, null, null);

    ServiceEndpoint destinationEndpoint = null;
    if (exchangeDAO.getDestinationEndpoint() != null)
      destinationEndpoint = _server.getService().createServiceEndpoint(exchangeDAO.getDestinationEndpoint());
    ServiceEndpoint sourceEndpoint = null;
    if (exchangeDAO.getSourceEndpoint() != null)
      sourceEndpoint = _server.getService().createServiceEndpoint(exchangeDAO.getSourceEndpoint());
    return new MessageExchangeImpl(this, sourceEndpoint, destinationEndpoint, exchangeDAO, opDesc);
  }

  void sendOutputMsg(MessageExchangeImpl messageExchange, MessageImpl outputMessage)
      throws MessageExchangeException, MessageFormatException {
    assert messageExchange._channelBackend == this;
    assert _server != null;

    outputMessage.checkValid();

    MessageExchangeTask mexEvent = new MessageExchangeTask(MessageExchangeTask.DIR_TO_CLIENT, _system.getSystemUUID(), _system.getDomainNode().getDomainId());
    mexEvent.setMsgType(MessageExchangeTask.MSGTYPE_OUTPUT);
    send(mexEvent, messageExchange, outputMessage);

    // send management event
    // sendMngmtEvent(ChannelManagementEvent.OUT_MSG, outputMessage);
  }

  void sendFailure(MessageExchangeImpl messageExchange, String description)
      throws MessageExchangeException {
    assert messageExchange._channelBackend == this;
    assert _server != null;

    if (__log.isDebugEnabled()) {
      __log.debug("sendFailure(" + messageExchange + ", " + description + ")");
    }

    MessageExchangeTask task = new MessageExchangeTask(MessageExchangeTask.DIR_TO_CLIENT, _system.getSystemUUID(), _system.getDomainNode().getDomainId());
    task.setMsgType(MessageExchangeTask.MSGTYPE_FAILURE);
    send(task, messageExchange, null);
  }


  private void send(MessageExchangeTask msgExchangeTask, MessageExchange messageExchange, MessageImpl msg) {
    msgExchangeTask.setChannelName(_channelName);
    msgExchangeTask.setInstanceId(messageExchange.getInstanceId());
    msgExchangeTask.setOperation(messageExchange.getName());

    DomainStateConnection dconn = _domain.getDomainStoreConnection();

    SystemDAO system = dconn.findSystem(_system.getSystemUUID().toString());
    MessageExchangeDAO exchangeDAO = system.getMessageExchange(messageExchange.getInstanceId());

    switch (msgExchangeTask.getMessageType()) {
      case MessageExchangeTask.MSGTYPE_INPUT:
        exchangeDAO.setState(MessageExchangeDAO.STATE_CLIENT_SENT_INPUTMSG);
        exchangeDAO.addInputMessage(msg._dao);
        break;

      case MessageExchangeTask.MSGTYPE_OUTPUT:
        exchangeDAO.setState(MessageExchangeDAO.STATE_CLIENT_RCVD_OUTPUTMSG);
        exchangeDAO.addOutputMessage(msg._dao);
        break;

      case MessageExchangeTask.MSGTYPE_OUTFAULT:
        exchangeDAO.setState(MessageExchangeDAO.STATE_CLIENT_RCVD_OUTFAULTMSG);
        exchangeDAO.addOutputMessage(msg._dao);
        break;

      case MessageExchangeTask.MSGTYPE_FAILURE:
        exchangeDAO.setState(MessageExchangeDAO.STATE_CLIENT_RCVD_OUTFAULTMSG);
        // TODO: Add failure description.
        break;

      default:
        throw new IllegalStateException("unknown message type: " + msgExchangeTask.getMessageType());
    }

    // Pass it on to the system (we don't directly send this puppy)
    _system.schedule(msgExchangeTask);
  }
}
