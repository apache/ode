/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi.dao;


import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Date;


/**
 * Data access object for a message exchange.
 */
public interface MessageExchangeDAO  {
  public static final int STATE_START = 0x0000;

  /** Inbound msg response sent. */
  public static final int STATE_CLIENT_SENT_INPUTMSG = 0x0011;

  /** Inbound msg received */
  public static final int STATE_SERVER_RCVD_INPUTMSG = 0x0022;

  /** Outbound msg sent outbound. */
  public static final int STATE_SERVER_SENT_OUTPUTMSG = 0x0023;

  /** Outbound fault msg sent (not possible) */
  public static final int STATE_SERVER_SENT_OUTFAULTMSG = 0x0024;

  /** Outbound msg response received. */
  public static final int STATE_CLIENT_RCVD_OUTPUTMSG = 0x00013;

  /** Outbound fault msg received */
  public static final int STATE_CLIENT_RCVD_OUTFAULTMSG = 0x0014;

  public static final int STATE_DONE = 0x00ff;

  public static final int STATE_FAILED = 0xffff;

  /**
   * Name of channel for which this message exchange manages.
   *
   * @return channel name.
   */
   String getChannelName();

  /**
   * Creation time of the message exchange
   *
   * @return create time
   */
   Date getCreateTime();

  /**
   * Get the input message.
   *
   * @return input message DAO
   */
   MessageDAO getInputMessage();

  /**
   * Instance id of the message exchange.
   *
   * @return message exchange id.
   */
   String getInstanceId();

  /**
   * Get the operation name of this message exchange.
   *
   * @return operation name.
   */
   String getOperationName();

  /**
   * Get the endpoint this message exchange is targeted at.
   * @return endpoint representation as a DOM Node
   */
  Node getDestinationEndpoint();

  /**
   * Set the endpoint this message exchange is targeted at.
   * @param endpoint representation as a DOM Node
   */
  void setDestinationEndpoint(Node endpoint);

  /**
   * Get the endpoint this message exchange has been issued from.
   * @return endpoint representation as a DOM Node
   */
  Node getSourceEndpoint();

  /**
   * Set the endpoint this message exchange has been issued from.
   * @param endpoint representation as a DOM Node
   */
  void setSourceEndpoint(Node endpoint);

  /**
   * Get output message (could be fault message)
   *
   * @return output message DAO
   */
   MessageDAO getOutputMessage();

  /**
   * The qualified name of the WSDL port type.
   *
   * @return port type name
   */
  QName getPortType();

  /**
   * Set state of last message sent/received.
   * 
   * @param state state to be set
   */
   void setState(int state);

  /**
   * Get state of last message sent/received.
   *
   * @return the state
   */
   int getState();

  /**
   * Create a new message associated with this message-exchange
   * @return new {@link MessageDAO}
   */
  MessageDAO createMessage();
  
  /**
   * Creates an input message DAO.
   */
   void addInputMessage(MessageDAO msg);

  /**
   * Creates an output message DAO.
   */
  void addOutputMessage(MessageDAO msg);
  
  /**
   * Get the correlation identifier.
   * @return correlation identifier
   */
  byte[] getCorrelationId();

  /**
   * Set the correlation identifier.
   * @param correlationId identifier
   */
  void setCorrelationId(byte[] correlationId);

  void setPinned(boolean pinned);

  boolean isPinned();

}
