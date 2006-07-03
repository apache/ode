/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.provider;

import java.util.HashMap;

import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.bpel.iapi.BpelServer;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange.FailureType;
import com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern;
import com.fs.pxe.bpel.pmapi.BpelManagementFacade;
import com.fs.pxe.bpel.runtime.InvalidProcessException;
import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.pxe.sfwk.spi.InputRcvdMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.MessageExchange;
import com.fs.pxe.sfwk.spi.MessageExchangeEvent;
import com.fs.pxe.sfwk.spi.OutFaultRcvdMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.OutputRcvdMessageExchangeEvent;
import com.fs.pxe.sfwk.spi.ServiceContext;
import com.fs.pxe.sfwk.spi.ServiceEvent;
import com.fs.pxe.sfwk.spi.ServicePort;
import com.fs.pxe.sfwk.spi.ServiceProviderException;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.SerializableUtils;
import com.fs.utils.msg.MessageBundle;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import sun.misc.BASE64Encoder;

/**
 * Representation of an <em>active</em> BPEL service.
 */
class BpelService {
  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(BpelService.class);

  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  private ServiceContext _serviceContext;

  private boolean _closed;

  private ProcessMBeanImpl _mbean;

  private MBeanServer _mbeanServer;

  private BpelServiceProvider _sp;

  private BpelServer _server;

  private final HashMap<QName, ServicePort> _partners = new HashMap<QName, ServicePort>();

  private final BASE64Encoder encoder = new BASE64Encoder();

  private QName _processId;

  BpelService(BpelServiceProvider bpelServiceProvider, BpelServer server,
      QName processId, ServiceContext service,
      BpelDAOConnectionFactory bpelDAOConnectionFactory,
      TransactionManager txm, MBeanServer mbeanServer) {

    _server = server;
    _processId = processId;
    _sp = bpelServiceProvider;
    _serviceContext = service;
    _mbeanServer = mbeanServer;
    buildPartnerLinkMaps();
    if (mbeanServer != null)
      try {
        _mbean = new ProcessMBeanImpl(this);
      } catch (Exception ex) {
        __log.error(ex);
      }
  }

  BpelManagementFacade getManagementFacade() {
    return _sp.getBpelManagementFacade();
  }

  void start() {
    _server.activate(_processId, false);
    if (_mbean != null)
      _mbean.register(_mbeanServer);
  }

  void stop() throws ServiceProviderException {
    assert !_closed;

    _server.deactivate(_processId, false);
    if (_mbean != null)
      _mbean.unregister();

    _closed = true;
  }

  ServiceContext getService() {
    return _serviceContext;
  }

  void onServiceEvent(ServiceEvent serviceEvent)
      throws ServiceProviderException {
    if (serviceEvent instanceof MessageExchangeEvent) {
      onMessageExchangeEvent((MessageExchangeEvent) serviceEvent);
    } else {
      String wrnmsg = "Ingoring unrecognized ServiceEvent: " + serviceEvent;
      __log.warn(wrnmsg);
    }
  }

  private void onMessageExchangeEvent(MessageExchangeEvent messageExchangeEvent)
      throws ServiceProviderException {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("onMessageExchangeEvent",
          new Object[] { "messageExchangeEvent", messageExchangeEvent }));

    try {
      if (messageExchangeEvent.isServerEvent()) {
        // Are we the server for this exchange?
        handleServerRoleEvent(messageExchangeEvent);
      } else if (messageExchangeEvent.isClientEvent()) {
        // Are we the client for this exchange?
        handleClientRoleEvent(messageExchangeEvent);
      }
    } catch (InvalidProcessException e) {
      if (e.getCauseCode() == InvalidProcessException.RETIRED_CAUSE_CODE) {
        throw new ServiceProviderException(e.getMessage(), e);
      }
      throw e;
    } catch (Exception e) {
      String errmsg = __msgs.msgProtocolBindingError();
      __log.error(errmsg, e);
      throw new ServiceProviderException(errmsg, e);
    }
  }

  /**
   * Build a mapping between ports and partnerLink link roles.
   */
  private void buildPartnerLinkMaps() {
    ServicePort[] imports = _serviceContext.getImports();
    for (ServicePort imp : imports) {
      _partners.put(imp.getPortType().getQName(), imp);
    }
  }

  /**
   * Handle msgs relating to exchanges where this service is in the
   * <em>client</em> role.
   * 
   * @param messageExchangeEvent
   *          msgs
   * 
   * @throws AssertionError
   *           DOCUMENTME
   */
  private void handleClientRoleEvent(MessageExchangeEvent messageExchangeEvent) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("handleClientRoleEvent",
          new Object[] { "messageExchangeEvent", messageExchangeEvent }));

    switch (messageExchangeEvent.getEventType()) {
    case MessageExchangeEvent.FAILURE:
    case MessageExchangeEvent.OUT_FAULT_EVENT:
    case MessageExchangeEvent.OUT_RCVD_EVENT:
      handleAsyncClientEvent(messageExchangeEvent);
      break;
    case MessageExchangeEvent.IN_RCVD_EVENT:
    case MessageExchangeEvent.IN_FAULT_EVENT:
      // This is impossible, framework would have to be broken.
      throw new AssertionError("input message received in client role");
    case MessageExchangeEvent.RECOVER:
      // Ignore recovery events: we store info about our message exchanges
      // in the database.
      break;
    default:
      __log.debug("unknown client-role msgs: " + messageExchangeEvent);
      break;
    }
  }

  private void handleAsyncClientEvent(MessageExchangeEvent messageExchangeEvent) {
    PartnerRoleMessageExchange bpelMex = (PartnerRoleMessageExchange) _server
        .getEngine().getMessageExchange(
            messageExchangeEvent.getMessageExchange().getCorrelationId());
    if (bpelMex == null) {
      __log.error("Ignoring event, no valid message exchange: "
          + messageExchangeEvent);
      return;
    }

    if (bpelMex.getStatus() != com.fs.pxe.bpel.iapi.MessageExchange.Status.ASYNC) {
      __log.error("Ignoring event, message exchange not in ASYNC state:  "
          + messageExchangeEvent);
      return;
    }

    switch (messageExchangeEvent.getEventType()) {
    case MessageExchangeEvent.FAILURE:
      __log.warn(__msgs.msgMessageExchangeFailure(messageExchangeEvent
          .getInstanceId()));
      bpelMex.replyWithFailure(FailureType.COMMUNICATION_ERROR, "", null);
      break;
    case MessageExchangeEvent.OUT_FAULT_EVENT:
      String fault = ((OutFaultRcvdMessageExchangeEvent) messageExchangeEvent)
          .getFaultName();

      com.fs.pxe.bpel.iapi.Message faultResponse = bpelMex
          .createMessage(((OutFaultRcvdMessageExchangeEvent) messageExchangeEvent)
              .getOutFaultMessage().getDescription().getQName());

      faultResponse
          .setMessage(((OutFaultRcvdMessageExchangeEvent) messageExchangeEvent)
              .getOutFaultMessage().getMessage());
      bpelMex.replyWithFault(fault, faultResponse);
      break;
    case MessageExchangeEvent.OUT_RCVD_EVENT:
      com.fs.pxe.bpel.iapi.Message response = bpelMex
          .createMessage(((OutputRcvdMessageExchangeEvent) messageExchangeEvent)
              .getOutputMessage().getDescription().getQName());
      response
          .setMessage(((OutputRcvdMessageExchangeEvent) messageExchangeEvent)
              .getOutputMessage().getMessage());
      bpelMex.reply(response);
      break;

    }
  }

  /**
   * Handle msgs relating to exchanges where this service is in the
   * <em>server</em> role.
   * 
   * @param sfwkEvent
   *          msgs
   */
  private void handleServerRoleEvent(MessageExchangeEvent sfwkEvent) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("handleServerRoleEvent",
          new Object[] { "messageExchangeEvent", sfwkEvent }));

    switch (sfwkEvent.getEventType()) {
    case MessageExchangeEvent.IN_RCVD_EVENT:

      ServiceEndpoint se = sfwkEvent.getMessageExchange()
          .getDestinationServiceEndpoint();
      String sname = sfwkEvent.getTargetService().getSystemName() + "."
          + sfwkEvent.getTargetService().getServiceName() + "."
          + sfwkEvent.getPort().getPortName();
      EndpointReference epr;
      if (se != null) {
        // TODO: fix the following hack.
        epr = _sp._eprContext.resolveEndpointReference((Element) se.toXML());
      } else {
        // TODO: standard messages
        __log.warn("Missing EPR for message exchange "
            + sfwkEvent.getInstanceId());
        epr = null;
      }
      MyRoleMessageExchange mex = _server.getEngine().createMessageExchange(
          sfwkEvent.getInstanceId(), new QName(null, sname), epr,
          sfwkEvent.getMessageExchange().getOperation().getName());
      mex.setProperty("mexref", encoder.encode(SerializableUtils
          .toBytes(sfwkEvent.getMessageExchange().getReference())));

      Message request = mex
          .createMessage(((InputRcvdMessageExchangeEvent) sfwkEvent)
              .getInputMessage().getDescription().getQName());
      request.setMessage(((InputRcvdMessageExchangeEvent) sfwkEvent)
          .getInputMessage().getMessage());
      mex.invoke(request);
      _sp.handleBpelResponse(sfwkEvent.getMessageExchange(), mex);
      break;

    case MessageExchangeEvent.IN_FAULT_EVENT:
    case MessageExchangeEvent.OUT_FAULT_EVENT:
    case MessageExchangeEvent.OUT_RCVD_EVENT:
      // This is impossible, framework would have to be broken
      throw new AssertionError("framework broken?");
    case MessageExchangeEvent.FAILURE:
      __log.debug("server-side failure received: " + sfwkEvent);
      break;
    default:
      // Do not know about the msgs type, log it.
      __log.debug("unknown server-role msgs: " + sfwkEvent);
      break;
    }
  }

  void invokePartner(PartnerRoleMessageExchange mex) {
    ServicePort sport = _partners.get(mex.getPortType().getQName());
    if (sport == null) {
      // TODO: log message.
      mex.replyWithFailure(FailureType.OTHER, "No route.", null);
      return;
    }

    ServiceEndpoint sepr = null;
    if (mex.getEndpointReference() != null)
      sepr = _serviceContext.createServiceEndpoint(mex.getEndpointReference()
          .toXML().getDocumentElement());

    MessageExchange smex = _serviceContext.createMessageExchange(sport, null,
        sepr, mex.getOperationName(), mex.getMessageExchangeId());
    com.fs.pxe.sfwk.spi.Message request = smex.createInputMessage();
    request.setMessage(mex.getRequest().getMessage());
    smex.input(request);
    if (mex.getMessageExchangePattern() == MessageExchangePattern.REQUEST_ONLY)
      mex.replyOneWayOk();
    else
      mex.replyAsync();

  }

}
