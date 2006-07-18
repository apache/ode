/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
import org.w3c.dom.Node;

import javax.management.ObjectName;
import javax.xml.namespace.QName;


/**
 * The PXE container's representation of a PXE service. This interface is used
 * exclusively to expose a particular service context to the service provider
 * implementation. For example, it is passed to the
 * {@link ServiceProvider#deployService(ServiceConfig)} and
 * {@link ServiceProvider#activateService(ServiceContext)}
 * to indicate which service is to be deployed or activated. This interface
 * provides methods that expose the configuration of the service as well
 * as methods that can be used to participate in message exchanges on behalf
 * of that service; typically the latter are used from
 * {@link ServiceProvider#onServiceEvent(ServiceEvent)}, but technically they
 * may be used anywhere the {@link ServiceContext} object is valid (and the
 * object is valid <em>only</em> from {@link ServiceProvider} methods).   
 *
 * <p>
 * <em>NOTE: The PXE container framework implements this interface. </em>
 * </p>
 */
public interface ServiceContext extends ServiceConfig {



  /**
   * <p>
   * Create a {@link MessageExchange} with the other end of the channel.
   * The "other end" is a port on another PXE Service.
   * </p>
   *
   * <p>
   * Generally speaking a {@link MessageExchange} is created (i.e. initiated)
   * by the <em>client</em> of a service. This however, does not necessarilty
   * have to be the case: in WSDL 1.2 solicit-response type MEPs, it is the
   * server that initiates the conversation.
   * </p>
   *
   * @param sourceEndpoint source service endpoint of the message exchange
   * @param destinationEndpoint desination service endpoint of the message exchange
   * @param operationName portName of the operation for which this message
   *        exchange will be created
   *
   * @return a new <code>MessageExchange</code> instance in the "start" state.
   *
   * @throws NoSuchOperationException if the port does not support the
   *         operation
   */
  MessageExchange createMessageExchange(ServicePort servicePort,
                                        ServiceEndpoint sourceEndpoint,
                                        ServiceEndpoint destinationEndpoint,
                                        String operationName)
          throws MessageExchangeException;

  /**
   * <p>
   * Same as createMessageExchange(ServiceEndpoint,Operation).
   * </p>
   *
   * <p>Includes an optional correlation id for correlation responses
   *    to clients.  Common usage scenario would be the need to
   *  	set a correlation id on an outbound JMS message based on
   * 	  in incoming message with a JMS correlation id.
   * </p>
   *
   * @param sourceEndpoint source service endpoint of the message exchange
   * @param destinationEndpoint destination service endpoint of the message exchange
   * @param operationName portName of the operation for which this message
   *        exchange will be created
   * @param correlationId (optional)
   * @return a new <code>MessageExchange</code> instance in the "start" state.
   * @throws NoSuchOperationException
   * @throws MessageExchangeException
   */
  MessageExchange createMessageExchange(ServicePort servicePort,
                                        ServiceEndpoint sourceEndpoint,
                                        ServiceEndpoint destinationEndpoint,
                                        String operationName, String correlationId)
    throws MessageExchangeException;

  /**
   * <p>
   * Same as createMessageExchange(ServiceEndpoint,Operation).
   * </p>
   *
   * <p>Includes an optional correlation id for correlation responses
   *    to clients.  Common usage scenario would be the need to
   *  	set a correlation id on an outbound JMS message based on
   * 	  in incoming message with a JMS correlation id.
   * </p>
   *
   * @param sourceEndpoint service endpoint the message comes from
   * @param destinationEndpoint service endpoint the message exchange is targeted at
   * @param operationName portName of the operation for which this message
   *        exchange will be created
   * @param correlationIdBytes (optional)
   * @return a new <code>MessageExchange</code> instance in the "start" state
   * @throws NoSuchOperationException
   * @throws com.fs.pxe.sfwk.spi.MessageExchangeException
   */
  MessageExchange createMessageExchange(ServicePort servicePort,
                                        ServiceEndpoint sourceEndpoint,
                                        ServiceEndpoint destinationEndpoint,
                                        String operationName,
                                        byte[] correlationIdBytes)
          throws MessageExchangeException;


  /**
   * Creates a concrete implementation of the {@link ServiceEndpoint} interface based
   * on the provided XML data.
   * @param node
   * @return a {@link ServiceEndpoint} implementation
   */
  ServiceEndpoint createServiceEndpoint(Node node);

  ServiceEndpoint convert(Node sourceEndpoint, QName targetElmtType);

  /**
   * Checks if the provided 'myRole' endpoint is complete to participate in
   * a stateful interaction (including session id).
   * @param epr original endpoint
   * @return Node null if no update was necessary, updated endpoint otherwise
   */
  ServiceEndpoint checkMyEndpoint(Node epr);

  /**
   * Checks if an endpoint given by a partner is complete to participate in
   * a stateful interaction (including session id). Partners could choosse to
   * provide only a session id, without their endpoint url (that we should
   * already have from deployment). In this case we're merging the url we have
   * with the session provided to produce an adequate endpoint.
   * @param partnerEpr
   * @param originalEpr
   * @return
   */
  ServiceEndpoint checkPartnerEndpoint(Node partnerEpr, Node originalEpr);

  ObjectName createLocalObjectName(String name[]);
  
}
