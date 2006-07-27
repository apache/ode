/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daomem;

import org.apache.ode.sfwk.bapi.dao.MessageDAO;
import org.apache.ode.sfwk.bapi.dao.MessageExchangeDAO;
import org.apache.ode.utils.ArrayUtils;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Date;


/**
 * In-memory {@link org.apache.ode.sfwk.bapi.dao.MessageExchangeDAO} implementation.
 */
class MessageExchangeDaoImpl implements MessageExchangeDAO {
  private static final Log __log = LogFactory.getLog(MessageExchangeDaoImpl.class);

  private String _instanceId;
  private String _op;
  private QName _portType;
  private String _channelName;
  private Date _date;
  private int _state;
  private MessageDaoImpl _input;
  private MessageDaoImpl _output;
  private byte[] _correlationId;
  private boolean _pinned;
  private Node _sourceEndpoint;
  private Node _destinationEndpoint;

  public MessageExchangeDaoImpl(String instanceId, Node sourceEndpoint, Node destEndpoint, String op, QName portType, String channelName) {
    _instanceId = instanceId;
    _op = op;
    _portType = portType;
    _channelName = channelName;
    _date = new Date();
    _state = MessageExchangeDAO.STATE_START;
    _sourceEndpoint = sourceEndpoint;
    _destinationEndpoint = destEndpoint;
  }

  public boolean isPinned() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("isPinned", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _pinned;
  }

  public void setPinned(boolean pinned) {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("setPinned",new Object[] {"pinned", Boolean.valueOf(pinned)}));
    }

    _pinned = pinned;
  }

  public String getChannelName() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getChannelName", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _channelName;
  }

  public Date getCreateTime() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getCreateTime", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _date;
  }

  public Serializable getDHandle() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getDHandle", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return null;
  }

  public MessageDAO getInputMessage() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getInputMessage", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _input;
  }

  public String getInstanceId() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getInstanceId", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _instanceId;
  }

  public String getOperationName() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getOperationName", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _op;
  }

  public MessageDAO getOutputMessage() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getOutputMessage", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _output;
  }

  public QName getPortType() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getPortType", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _portType;
  }

  public void setState(int state) {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("setState",new Object[] {"state", state}));
    }

    _state = state;
  }

  public int getState() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getState", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _state;
  }

  public MessageDAO createMessage() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("createMessage", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return new MessageDaoImpl(this);
  }

  public void addInputMessage(MessageDAO msg) {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("addInputMessage",new Object[] {"msg",msg}));
    }

    if (_input != null) {
      throw new IllegalStateException("input not null");
    }

    _input = (MessageDaoImpl) msg;
  }

  public void addOutputMessage(MessageDAO msg) {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("addOutputMessage",new Object[] {"msg",msg}));
    }

    if (_output != null) {
      throw new IllegalStateException("output not null");
    }

    _output = (MessageDaoImpl)msg;
  }

  public byte[] getCorrelationId() {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("getCorrelationId", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return _correlationId;
  }

  public void setCorrelationId(byte[] correlationId) {
    if (__log.isDebugEnabled()) {
      __log.debug(ObjectPrinter.stringifyMethodEnter("setCorrelationId",new Object[] {"correlationId", correlationId}));
    }

    _correlationId = correlationId;
  }

  public Node getSourceEndpoint() {
    return _sourceEndpoint;
  }

  public void setSourceEndpoint(Node sourceEndpoint) {
    _sourceEndpoint = sourceEndpoint;
  }

  public Node getDestinationEndpoint() {
    return _destinationEndpoint;
  }

  public void setDestinationEndpoint(Node destinationEndpoint) {
    _destinationEndpoint = destinationEndpoint;
  }
}
