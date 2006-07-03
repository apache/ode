/*
 * File:      $Id: PxeToJbiBridge.java 492 2006-01-02 16:12:09Z holger $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.jbi;

import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange.FailureType;
import com.fs.pxe.jbi.msgmap.Mapper;
import com.fs.pxe.jbi.msgmap.MessageTranslationException;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.messaging.*;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bridge between PXE (consumers) and JBI (providers). An single object of this
 * type handles all communications initiated by PXE that is destined for other
 * JBI providers.
 */
class PxeConsumer implements JbiMessageExchangeProcessor {
  private static final Log __log = LogFactory.getLog(PxeConsumer.class);

  private PxeContext _pxe;

  private Map<String, String> _outstandingExchanges = new ConcurrentHashMap<String, String>();

  PxeConsumer(PxeContext pxe) {
    _pxe = pxe;
  }

  /**
   * This is where we handle invocation where the PXE BPEL engine is the
   * <em>client</em> and some other JBI service is the <em>provider</em>.
   */
  public void invokePartner(PartnerRoleMessageExchange pxeMex)
      throws ContextException {
    // Cast the EndpointReference to a JbiEndpointReference. This is the
    // only type it can be (since we control the creation of these things).
    JbiEndpointReference targetEndpoint = (JbiEndpointReference) pxeMex
        .getEndpointReference();

    if (targetEndpoint == null) {
      String errmsg = "No endpoint for mex: " + pxeMex;
      __log.error(errmsg);
      pxeMex.replyWithFailure(FailureType.INVALID_ENDPOINT, errmsg, null);
      return;
    }

    ServiceEndpoint se = targetEndpoint.getServiceEndpoint();

    boolean isTwoWay = pxeMex.getMessageExchangePattern() == com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;

    QName opname = new QName(se.getServiceName().getNamespaceURI(), pxeMex
        .getOperation().getName());

    MessageExchangeFactory mexf = _pxe.getChannel().createExchangeFactory(se);
    MessageExchange jbiMex;
    try {
      jbiMex = mexf.createExchange(isTwoWay ? MessageExchangePattern.IN_OUT
          : MessageExchangePattern.IN_ONLY);
      jbiMex.setEndpoint(se);
      jbiMex.setService(se.getServiceName());
      jbiMex.setOperation(opname);
    } catch (MessagingException e) {
      String errmsg = "Unable to create JBI message exchange for PXE message exchange "
          + pxeMex;
      __log.error(errmsg, e);
      pxeMex.replyWithFailure(FailureType.COMMUNICATION_ERROR, errmsg, null);
      return;
    }

    Mapper mapper = _pxe.getDefaultMapper();
    pxeMex.setProperty(Mapper.class.getName(),mapper.getClass().getName());
    try {
      if (!isTwoWay) {
        InOnly inonly = ((InOnly) jbiMex);
        NormalizedMessage nmsg = inonly.createMessage();
        mapper.toNMS(nmsg,pxeMex.getRequest(), pxeMex.getOperation().getInput().getMessage());
        inonly.setInMessage(nmsg);
        _pxe.getChannel().send(inonly);
      } else {
        InOut inout = (InOut) jbiMex;
        NormalizedMessage nmsg = inout.createMessage();
        mapper.toNMS(nmsg,pxeMex.getRequest(), pxeMex.getOperation().getInput().getMessage());
        inout.setInMessage(nmsg);
        _pxe.getChannel().send(inout);
        _outstandingExchanges.put(inout.getExchangeId(), pxeMex
            .getMessageExchangeId());
      }
    } catch (MessagingException me) {
      String errmsg = "Error sending message to JBI for PXE mex " + pxeMex;
      __log.error(errmsg, me);
      pxeMex.replyWithFailure(FailureType.COMMUNICATION_ERROR, errmsg, null);
    } catch (MessageTranslationException e) {
      String errmsg = "Error converting PXE message to JBI format for mex "
          + pxeMex;
      __log.error(errmsg, e);
      pxeMex.replyWithFailure(FailureType.FORMAT_ERROR, errmsg, null);
    }

  }

  public void onJbiMessageExchange(MessageExchange jbiMex)
      throws MessagingException {
    if (jbiMex.getPattern().equals(MessageExchangePattern.IN_ONLY)) {
      // Ignore these, they're one way.
    } else if (jbiMex.getPattern().equals(MessageExchangePattern.IN_OUT)) {
      if (jbiMex.getStatus() == ExchangeStatus.ACTIVE) {
        outResponse((InOut) jbiMex);
        jbiMex.setStatus(ExchangeStatus.DONE);
        _pxe.getChannel().send(jbiMex);
      } else if (jbiMex.getStatus() == ExchangeStatus.ERROR)
        outFailure((InOut) jbiMex);
      else
        __log.warn("Unexpected state for JBI message exchange: "
            + jbiMex.getExchangeId());
    } else {
      __log.fatal("JBI MessageExchange " + jbiMex.getExchangeId()
          + " is of an unsupported pattern " + jbiMex.getPattern());
    }

  }

  private void outFailure(final InOut jbiMex) {
    final String mexref = _outstandingExchanges.remove(jbiMex.getExchangeId());
    if (mexref == null) {
      __log.warn("Received a response for unkown JBI message exchange "
          + jbiMex.getExchangeId());
      return;
    }

    try {
      _pxe._scheduler.execTransaction(new Callable<Boolean>() {
        public Boolean call() throws Exception {
          PartnerRoleMessageExchange pmex = (PartnerRoleMessageExchange) _pxe._server
              .getEngine().getMessageExchange(mexref);
          pmex.replyWithFailure(FailureType.OTHER, "Error: "
              + jbiMex.getError(), null);
          return null;
        }
      });
    } catch (Exception ex) {
      __log.error("error delivering failure: " ,ex);
    }

  }

  private void outResponse(final InOut jbiMex) {
    final String mexref = _outstandingExchanges.remove(jbiMex.getExchangeId());
    if (mexref == null) {
      __log.warn("Received a response for unkown JBI message exchange "
          + jbiMex.getExchangeId());
      return;
    }

    try {
      _pxe._scheduler.execTransaction(new Callable<Boolean>() {
        public Boolean call() throws Exception {
          PartnerRoleMessageExchange pmex = (PartnerRoleMessageExchange) _pxe._server
              .getEngine().getMessageExchange(mexref);

          String mapperName = pmex.getProperty(Mapper.class.getName());
          Mapper mapper = mapperName == null ? _pxe.getDefaultMapper() : _pxe.getMapper(mapperName);
          if (mapper == null) {
            String errmsg = "Mapper not found.";
            __log.error(errmsg);
            pmex.replyWithFailure(FailureType.FORMAT_ERROR, errmsg, null);
          } else {
            try {
              Fault jbiFlt = jbiMex.getFault();
              if (jbiFlt != null) {

                // TODO: How are we supposed to figure out the fault type exactly?
                throw new AssertionError("todo");
              } else {
                Message response = pmex.createMessage(pmex.getOperation().getOutput().getMessage().getQName());
                mapper.toPXE(response,jbiMex.getOutMessage(),pmex.getOperation().getOutput().getMessage());
                pmex.reply(response);
              }
            } catch (MessageTranslationException mte) {
              __log.error("Error translating message.", mte);
              pmex.replyWithFailure(FailureType.FORMAT_ERROR, mte.getMessage(), null);
            }
          }
          return null;
        }
      });
    } catch (Exception ex) {
      __log.error("error delivering RESPONSE: " ,ex);
      
    }
  }
}
