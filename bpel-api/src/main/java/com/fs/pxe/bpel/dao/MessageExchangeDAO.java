/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.dao;

import java.util.Date;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Data access object for a message exchange.
 */
public interface MessageExchangeDAO {

  public static final char DIR_BPEL_INVOKES_PARTNERROLE = 'P';

  public static final char DIR_PARTNER_INVOKES_MYROLE = 'M';

  /**
   * Instance id of the message exchange.
   * 
   * @return message exchange id.
   */
  String getMessageExchangeId();

  /**
   * Get output message (could be fault message)
   * 
   * @return output message DAO
   */
  MessageDAO getResponse();

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
  MessageDAO getRequest();

  /**
   * Get the operation name of this message exchange.
   * 
   * @return operation name.
   */
  String getOperation();

  /**
   * The qualified name of the WSDL port type.
   * 
   * @return port type name
   */
  QName getPortType();

  /**
   * Set the port type.
   * 
   * @param porttype
   *          port type
   */
  void setPortType(QName porttype);

  /**
   * Set state of last message sent/received.
   * 
   * @param string
   *          state to be set
   */
  void setStatus(String status);

  /**
   * Get state of last message sent/received.
   * 
   * @return the state
   */
  String getStatus();

  /**
   * Create a new message associated with this message-exchange
   * 
   * @param type
   *          message type
   * @return new {@link MessageDAO}
   */
  MessageDAO createMessage(QName type);

  /**
   * Creates an input message DAO.
   */
  void setRequest(MessageDAO msg);

  /**
   * Creates an output message DAO.
   */
  void setResponse(MessageDAO msg);

  /**
   * Get the model id for the partner link to which this message exchange
   * relates.
   * 
   * @return
   */
  int getPartnerLinkModelId();

  /**
   * Set the model id for the partner link to which this message exchange
   * relates
   * 
   * @param modelId
   */
  void setPartnerLinkModelId(int modelId);

  /**
   * Get the correlation identifier/client id
   * 
   * @return correlation identifier
   */
  String getCorrelationId();

  /**
   * Set the correlation identifier/client id
   * 
   * @param correlationId
   *          identifier
   */
  void setCorrelationId(String correlationId);

  void setPattern(String string);

  void setOperation(String opname);

  void setEPR(Element epr);

  Element getEPR();

  String getPattern();

  /**
   * Get the response channel.
   * 
   * @return response channel.
   */
  String getChannel();

  /**
   * Set the response channel.
   * 
   * @param string
   *          response channel
   */
  void setChannel(String string);

  boolean getPropagateTransactionFlag();

  String getFault();

  void setFault(String faultType);

  void setCorrelationStatus(String cstatus);

  String getCorrelationStatus();

  /**
   * Get the process associate with this message exchange. The process should
   * always be available for partnerRole message exchanges. However, for myRole
   * message exchanges, it is possible that no process is associated with the
   * message exchange (i.e. if the EPR routing fails).
   * 
   * @return process associated with the message exchange
   */
  ProcessDAO getProcess();

  void setProcess(ProcessDAO process);

  void setInstance(ProcessInstanceDAO dao);

  ProcessInstanceDAO getInstance();

  /**
   * Get the direction of the message exchange.
   * 
   * @return
   */
  char getDirection();

  /**
   * Get the "callee"--the id of the process being invoked in a myRole
   * exchange.
   * @return
   */
  QName getCallee();

  /**
   * Set the "callee"--the id of the process being invoked in a myRole 
   * exchange.
   * @param callee
   */
  void setCallee(QName callee);

  String getProperty(String key);

  void setProperty(String key, String value);


}
