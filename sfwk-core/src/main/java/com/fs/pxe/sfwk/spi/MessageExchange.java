/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import com.fs.pxe.sfwk.core.ServiceEndpoint;

import javax.wsdl.Operation;
import javax.wsdl.PortType;


/**
 * An interface to a PXE message exchange. Message exchange objects are used
 * by PXE services for inter-communication.  A {@link MessageExchange} object
 * corresponds to a WSDL 2.0 <em>message exchange</em> or a WSDL 1.1
 * <em>operation</em>. Typically, a  <em>client</em> service creates a {@link
 * MessageExchange} and uses it to send an <em>input</em> message; upon
 * receipt of the input message the <em>server</em> uses the same {@link
 * MessageExchange} to send an <em>output</em> message back to the client.
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface MessageExchange {

  /**
   * Get the port-type of this message-exchange.
   * @return {@link PortType} of the associated channel.
   */
  PortType getPortType();
  
  /**
   * Message exchange may have an <b>optional</b> correlation id, useful for
   * maintaining a protocol adapter specific correlation key for asynchronous request/response
   * messaging (e.g. JMS).
   * @return the correlation id
   */
  String getCorrelationId();

  /**
   * Get the correlation id in byte form.
   * @return correlation id bytes.
   */
  byte[] getCorrelationIdBytes();

  /**
   * Get a unique identifier for the instance of the MEP state machine from
   * which this message originated. This identifier is used in MEPs such as
   * in-out and request-response to correlate the response message with the
   * original request. The returned id need be unique only to the protocol
   * adapater that originated the message.
   *
   * @return byte array defining a unique identifier for the message exchange
   *         instance
   */
  String getInstanceId();

  /**
   * Get the name of the operation (WSDL 1.1) / message exchange (WSDL 1.2?).
   *
   * @return name of the operation (WSDL 1.1) /message exchange (WSDL 1.2?).
   */
  String getName();

  /**
   * Get hte {@link Operation} for this message exchange.
   * @return a WSDL {@link Operation} description
   */
  Operation getOperation();

  /**
   * Get a persisted (counted) {@link MessageExchangeRef} to this exchange
   * object. The reference can be used in another transaction to recreate
   * this exchange object.
   *
   * @return the reference
   */
  MessageExchangeRef getReference();

  /**
   * Get the {@link com.fs.pxe.sfwk.core.ServiceEndpoint} (i.e. reference to some web service) that
   * originated this exchange (operation).
   * @return the service endpoint origin of the exchange
   */
  ServiceEndpoint getSourceServiceEndpoint();

  /**
   * Get the {@link com.fs.pxe.sfwk.core.ServiceEndpoint} (i.e. reference to some web service) on
   * which this exchange (operation) is being performed.
   * @return the service endpoint the exchange is targeted at
   */
  ServiceEndpoint getDestinationServiceEndpoint();

  // TODO: Better documentation for this method.

  /**
   * Create an input {@link Message}.
   *
   * @return a new input message
   *
   * @throws IllegalStateException if creating this type of message is
   *         inappropriate at the present point.
   * @throws MessageExchangeException
   */
  Message createInputMessage()
                             throws MessageExchangeException;

  // TODO: Better documentation for this method.

  /**
   * Creates an output fault {@link Message}.
   *
   * @return a new output fault message
   *
   * @throws IllegalStateException if creating this type of message is
   *         inappropriate at the present point.
   * @throws com.fs.pxe.sfwk.spi.MessageExchangeException
   */
  Message createOutfaultMessage(String fault)
                                throws MessageExchangeException;

  // TODO: Better documentation for this method.

  /**
   * Creates an output message.
   *
   * @return a new output message
   *
   * @throws MessageExchangeException
   * @throws IllegalStateException if creating this type of message is
   *         inappropriate at the present point.
   */
  Message createOutputMessage()
                              throws IllegalStateException, 
                                     MessageExchangeException;

  /**
   * Indicate to the framework that this message exchange is unknown to the
   * recipient.
   */
  void dontKnow();

  /**
   * Deliver an input {@link Message}. Input messages are delivered from the
   * client to the server, so the current context must be that of {@link
   * Context#CLIENT} for this method to be applicable.
   *
   * @param inputMessage the input message
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void input(Message inputMessage)
             throws IllegalStateException, MessageExchangeException, MessageFormatException;

  // TODO: Better documentation for this method.

  /**
   * Retreive last fault message by key.
   *
   * @return the relevant message or <code>null</code> if there was none.
   */
  Message lastFault(String fault)
                    throws MessageExchangeException;

  /**
   * Retreive last input message.
   *
   * @return the relevant message or <code>null</code> if there was none.
   */
  Message lastInput()
                    throws MessageExchangeException;

  /**
   * Retreive last output message.
   *
   * @return the relevant message or <code>null</code> if there was none.
   */
  Message lastOutput()
                     throws MessageExchangeException;

  /**
   * Deliver an output fault message.
   *
   * @param faultType fault type
   * @param outputFaultMessage the input message
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void outfault(String faultType, Message outputFaultMessage)
                throws IllegalStateException, MessageExchangeException, MessageFormatException;

  /**
   * Deliver an output {@link Message}. Output messages are delivered from the
   * server to the client, so the current context must be that of {@link
   * Context#SERVER} for this method to be applicable.
   *
   * @param outputMessage the input message
   *
   * @throws IllegalStateException if delivering this type of message is
   *         inappropriate at the present point.
   */
  void output(Message outputMessage)
              throws IllegalStateException, MessageExchangeException, MessageFormatException;


  /**
   * Indicate that the message exchange has failed. Invoking this method will
   * cause a failure advisory to be sent back to the other party.
   * @param description description of failure
   */
  void failure(String description) throws MessageExchangeException;

  /**
   * Pin this message exchange to the store; prevents garbage collection in circumstances
   * where the service provider will need to defer work on the message exchange
   * until a later time.
   */
  void pin();

  /**
   * Release a previously "pinned" message exchange.
   * @see #pin()
   */
  void release();

  ServicePort getServerPort();

  ServicePort getClientPort();
}
