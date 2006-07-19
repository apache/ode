/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.iapi;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

/**
 * A representation of a communication (message-exchange) between the BPEL 
 * BPEL engine and an  external "partner".
 */
public interface MessageExchange {

  /**
   * Enumeration of message exchange patterns.
   */
  public enum MessageExchangePattern {
    REQUEST_ONLY,
    REQUEST_RESPONSE, 
    UNKNOWN
  }

  /**
   * Enumeration of the possible states for the message exchange.
   */
  public enum Status {
    /** New message exchange, has not been "invoked" */
    NEW,
    
    /** The request is being sent to the "server" */
    REQUEST,
    
    /** Waiting for an asynchronous response from the "server" */
    ASYNC,
    
    /** The one way request has been sent to the server. */
    ONE_WAY,
    
    /** Processing the response received from the "server". */
    RESPONSE,
    
    /** Processing the fault received from the "server". */
    FAULT,
    
    /** Processing a failure. */
    FAILURE,
    
    /** Message exchange completed succesfully. */
    COMPLETED_OK,
    
    /** Message exchange completed with a fault. */
    COMPLETED_FAULT,
    
    /** Message exchange completed with a failure. */
    COMPLETED_FAILURE,
  }
  
  /**
   * Enumeration of the types of failures. 
   */
  public enum FailureType {
    INVALID_ENDPOINT,
    UNKNOWN_ENDPOINT,
    UNKNOWN_OPERATION,
    COMMUNICATION_ERROR,
    FORMAT_ERROR,
    NO_RESPONSE,
    OTHER
  }
  
  /**
   * Get the message exchange identifier. This identifier should be globally 
   * unique as the BPEL engine may keep identifiers for extended periods of
   * time.
   * @return unique message exchange identifier
   */
  String getMessageExchangeId()
    throws BpelEngineException;
 
  /**
   * Get the name of the operation (WSDL 1.1) / message exchange (WSDL 1.2?).
   *
   * @return name of the operation (WSDL 1.1) /message exchange (WSDL 1.2?).
   */
  String getOperationName()
    throws BpelEngineException;
  

  /**
   * Get a reference to the end-point targeted by this message exchange.
   * @return end-point reference for this message exchange
   */
  EndpointReference getEndpointReference()
    throws BpelEngineException;

  void setEndpointReference(EndpointReference ref);

  /**
   * Get a reference to the end-point that originated this message exchange and
   * will be use for an eventual invocation later.
   * @return end-point reference for this message exchange
   */
  EndpointReference getCallbackEndpointReference()
    throws BpelEngineException;

  void setCallbackEndpointReference(EndpointReference ref);
  
  /**
   * Return the type of message-exchange that resulted form this invocation 
   * (request only/request-respone). If a 
   * {@link MessageExchangePattern#REQUEST_RESPONSE} message-exchange was 
   * created, then the caller should expect a response in the future. 
   * @return type of message exchange created by the invocation
   */
  MessageExchangePattern getMessageExchangePattern();
  
  /**
   * Create a message associated with this exchange.
   * @param msgType message type
   * @return a new {@link Message}
   */
  Message createMessage(QName msgType);

  boolean isTransactionPropagated()
    throws BpelEngineException;
  
  /**
   * Get the message exchange status.
   * @return
   */
  Status getStatus();
  
  /**
   * Get the request message.
   * @return request message
   */
  Message getRequest();
  
  /**
   * Get the response message.
   * @return response message (or null if not avaiable)
   */
  Message getResponse();
  
  /**
   * Get the fault type.
   * @return fault type, or <code>null</code> if not available/applicable.
   */
  String getFault();
  
  /**
   * Get the fault resposne message.
   * @return fault response, or <code>null</code> if not available/applicable.
   */
  Message getFaultResponse();
  
  /**
   * Get the operation description for this message exchange.
   * It is possible that the description cannot be resolved, for example if 
   * the EPR is unknown or if the operation does not exist. 
   * TODO: How to get rid of the WSDL4j dependency? 
   * @return WSDL operation description or <code>null</code> if not availble
   */ 
  Operation getOperation();
  
  /**
   * Get the port type description for this message exchange. 
   * It is possible that the description cannot be resolved, for example if 
   * the EPR is unknown or if the operation does not exist. 
   * TODO: How to get rid of the WSDL4j dependency? 
   * @return WSDL port type description or <code>null</code> if not available.
   */
  PortType getPortType();
  
  /**
   * Set a message exchange property. Message exchange properties are not 
   * interpreted by the engine--they exist to enable the integration layer
   * to persist information about the exchange.
   * @param key property key
   * @param value property value
   */
  void setProperty(String key, String value);
  
  /**
   * Get a message exchange property.
   * @param key property key
   * @return property value
   */
  String getProperty(String key);
  
 }
