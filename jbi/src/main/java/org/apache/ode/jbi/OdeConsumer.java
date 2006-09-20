/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jbi;

import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.jbi.msgmap.MessageTranslationException;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.messaging.*;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bridge between ODE (consumers) and JBI (providers). An single object of this
 * type handles all communications initiated by ODE that is destined for other
 * JBI providers.
 */
class OdeConsumer extends ServiceBridge implements JbiMessageExchangeProcessor {
  private static final Log __log = LogFactory.getLog(OdeConsumer.class);

  private OdeContext _ode;

  private Map<String, String> _outstandingExchanges = new ConcurrentHashMap<String, String>();

  OdeConsumer(OdeContext ode) {
    _ode = ode;
  }

  /**
   * This is where we handle invocation where the ODE BPEL engine is the
   * <em>client</em> and some other JBI service is the <em>provider</em>.
   */
  public void invokePartner(PartnerRoleMessageExchange odeMex)
      throws ContextException {
    // Cast the EndpointReference to a JbiEndpointReference. This is the
    // only type it can be (since we control the creation of these things).
    JbiEndpointReference targetEndpoint = (JbiEndpointReference) odeMex
        .getEndpointReference();

    if (targetEndpoint == null) {
      String errmsg = "No endpoint for mex: " + odeMex;
      __log.error(errmsg);
      odeMex.replyWithFailure(FailureType.INVALID_ENDPOINT, errmsg, null);
      return;
    }

    ServiceEndpoint se = targetEndpoint.getServiceEndpoint();

    boolean isTwoWay = odeMex.getMessageExchangePattern() == org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;

    QName opname = new QName(se.getServiceName().getNamespaceURI(), odeMex
        .getOperation().getName());

    MessageExchangeFactory mexf = _ode.getChannel().createExchangeFactory(se);
    MessageExchange jbiMex;
    try {
      jbiMex = mexf.createExchange(isTwoWay ? MessageExchangePattern.IN_OUT
          : MessageExchangePattern.IN_ONLY);
      jbiMex.setEndpoint(se);
      jbiMex.setService(se.getServiceName());
      jbiMex.setOperation(opname);
    } catch (MessagingException e) {
      String errmsg = "Unable to create JBI message exchange for ODE message exchange "
          + odeMex;
      __log.error(errmsg, e);
      odeMex.replyWithFailure(FailureType.COMMUNICATION_ERROR, errmsg, null);
      return;
    }

    Mapper mapper = _ode.getDefaultMapper();
    odeMex.setProperty(Mapper.class.getName(),mapper.getClass().getName());
    try {
      if (!isTwoWay) {
        InOnly inonly = ((InOnly) jbiMex);
        NormalizedMessage nmsg = inonly.createMessage();
        mapper.toNMS(nmsg,odeMex.getRequest(), odeMex.getOperation().getInput().getMessage());
        inonly.setInMessage(nmsg);
        _ode.getChannel().send(inonly);
        odeMex.replyOneWayOk();
      } else {
        InOut inout = (InOut) jbiMex;
        NormalizedMessage nmsg = inout.createMessage();
        mapper.toNMS(nmsg,odeMex.getRequest(), odeMex.getOperation().getInput().getMessage());
        inout.setInMessage(nmsg);
        _ode.getChannel().send(inout);
        _outstandingExchanges.put(inout.getExchangeId(), odeMex
            .getMessageExchangeId());
        odeMex.replyAsync();
      }
    } catch (MessagingException me) {
      String errmsg = "Error sending message to JBI for ODE mex " + odeMex;
      __log.error(errmsg, me);
      odeMex.replyWithFailure(FailureType.COMMUNICATION_ERROR, errmsg, null);
    } catch (MessageTranslationException e) {
      String errmsg = "Error converting ODE message to JBI format for mex "
          + odeMex;
      __log.error(errmsg, e);
      odeMex.replyWithFailure(FailureType.FORMAT_ERROR, errmsg, null);
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
        _ode.getChannel().send(jbiMex);
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
      _ode._scheduler.execTransaction(new Callable<Boolean>() {
        public Boolean call() throws Exception {
          PartnerRoleMessageExchange pmex = (PartnerRoleMessageExchange) _ode._server
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
      _ode._scheduler.execTransaction(new Callable<Boolean>() {
        public Boolean call() throws Exception {
          PartnerRoleMessageExchange pmex = (PartnerRoleMessageExchange) _ode._server
              .getEngine().getMessageExchange(mexref);

          String mapperName = pmex.getProperty(Mapper.class.getName());
          Mapper mapper = mapperName == null ? _ode.getDefaultMapper() : _ode.getMapper(mapperName);
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
                mapper.toODE(response,jbiMex.getOutMessage(),pmex.getOperation().getOutput().getMessage());
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
