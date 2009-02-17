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

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.jbi.msgmap.MessageTranslationException;
import org.w3c.dom.Element;

/**
 * Bridge JBI (consumer) to ODE (provider).
 */
public class OdeService extends ServiceBridge implements JbiMessageExchangeProcessor {

    private static final Log __log = LogFactory.getLog(OdeService.class);

    /** JBI-Generated Endpoint */
    private ServiceEndpoint _internal;

    /** External endpoint. */
    private ServiceEndpoint _external;

    private OdeContext _ode;

    private Element _serviceref;

    private Endpoint _endpoint;

    public OdeService(OdeContext odeContext, Endpoint endpoint) throws Exception {
        _ode = odeContext;
        _endpoint = endpoint;
    }

    /**
     * Do the JBI endpoint activation.
     * 
     * @throws JBIException
     */
    public void activate() throws JBIException {
        if (_serviceref == null) {
            ServiceEndpoint[] candidates = _ode.getContext().getExternalEndpointsForService(_endpoint.serviceName);
            if (candidates.length != 0) {
                _external = candidates[0];
            }
        }
        _internal = _ode.getContext().activateEndpoint(_endpoint.serviceName, _endpoint.portName);
        if (__log.isDebugEnabled()) {
            __log.debug("Activated endpoint " + _endpoint);
        }
        // TODO: Is there a race situation here?
    }

    /**
     * Deactivate endpoints in JBI.
     */
    public void deactivate() throws JBIException {
        _ode.getContext().deactivateEndpoint(_internal);
        __log.debug("Dectivated endpoint " + _endpoint);
    }

    public ServiceEndpoint getInternalServiceEndpoint() {
        return _internal;
    }

    public ServiceEndpoint getExternalServiceEndpoint() {
        return _external;
    }

    public void onJbiMessageExchange(javax.jbi.messaging.MessageExchange jbiMex) throws MessagingException {
        if (jbiMex.getRole() != javax.jbi.messaging.MessageExchange.Role.PROVIDER) {
            String errmsg = "Message exchange is not in PROVIDER role as expected: " + jbiMex.getExchangeId();
            __log.fatal(errmsg);
            throw new IllegalArgumentException(errmsg);
        }

        if (jbiMex.getStatus() != ExchangeStatus.ACTIVE) {
            // We can forget about the exchange.
            return;
        }

        if (jbiMex.getOperation() == null) {
            throw new IllegalArgumentException("Null operation in JBI message exchange id=" + jbiMex.getExchangeId() + " endpoint="
                    + _endpoint);
        }

        if (jbiMex.getPattern().equals(org.apache.ode.jbi.MessageExchangePattern.IN_ONLY)) {
            boolean success = false;
            Exception err = null;
            try {
                invokeOde(jbiMex, ((InOnly) jbiMex).getInMessage());
                success = true;
            } catch (Exception ex) {
                __log.error("Error invoking ODE.", ex);
                err = ex;
            } finally {
                if (!success) {
                    jbiMex.setStatus(ExchangeStatus.ERROR);
                    if (err != null && jbiMex.getError() == null)
                        jbiMex.setError(err);
                } else {
                    if (jbiMex.getStatus() == ExchangeStatus.ACTIVE)
                        jbiMex.setStatus(ExchangeStatus.DONE);
                }
                _ode.getChannel().send(jbiMex);
            }
        } else if (jbiMex.getPattern().equals(org.apache.ode.jbi.MessageExchangePattern.IN_OUT)) {
            boolean success = false;
            Exception err = null;
            try {
                invokeOde(jbiMex, ((InOut) jbiMex).getInMessage());
                success = true;
            } catch (Exception ex) {
                __log.error("Error invoking ODE.", ex);
                err = ex;
            } catch (Throwable t) {
                __log.error("Unexpected error invoking ODE.", t);
                err = new RuntimeException(t);
            } finally {
                // If we got an error that wasn't sent.
                if (jbiMex.getStatus() == ExchangeStatus.ACTIVE && !success) {
                    if (err != null && jbiMex.getError() == null) {
                        jbiMex.setError(err);
                    }
                    jbiMex.setStatus(ExchangeStatus.ERROR);
                    _ode.getChannel().send(jbiMex);
                }
            }
        } else {
            __log.error("JBI MessageExchange " + jbiMex.getExchangeId() + " is of an unsupported pattern " + jbiMex.getPattern());
            jbiMex.setStatus(ExchangeStatus.ERROR);
            jbiMex.setError(new Exception("Unknown message exchange pattern: " + jbiMex.getPattern()));
        }

    }

    /**
     * Forward a JBI input message to ODE.
     * 
     * @param jbiMex
     */
    private void invokeOde(javax.jbi.messaging.MessageExchange jbiMex, NormalizedMessage request) throws Exception {

        MyRoleMessageExchange odeMex;
        if (__log.isDebugEnabled()) {
            __log.debug("invokeOde() JBI exchangeId=" + jbiMex.getExchangeId() + " endpoint=" + _endpoint + " operation="
                    + jbiMex.getOperation());
        }

        odeMex = _ode._server.createMessageExchange(InvocationStyle.UNRELIABLE, _endpoint.serviceName, jbiMex.getOperation()
                .getLocalPart(), jbiMex.getExchangeId());

        if (odeMex.getOperation() == null) {
            __log.error("ODE MEX " + odeMex + " was unroutable.");
            sendError(jbiMex, new IllegalArgumentException("Unroutable invocation."));
            return;
        }

        copyMexProperties(odeMex, jbiMex);
        javax.wsdl.Message msgdef = odeMex.getOperation().getInput().getMessage();
        Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
        Mapper mapper = _ode.findMapper(request, odeMex.getOperation());
        if (mapper == null) {
            String errmsg = "Could not find a mapper for request message for JBI MEX " + jbiMex.getExchangeId() + "; ODE MEX "
                    + odeMex.getMessageExchangeId() + " is failed. ";
            __log.error(errmsg);
            throw new MessageTranslationException(errmsg);

        }
        odeMex.setProperty(Mapper.class.getName(), mapper.getClass().getName());
        mapper.toODE(odeRequest, request, msgdef);
        odeMex.setRequest(odeRequest);
        try {
            odeMex.invokeBlocking();

        } catch (Exception ex) {
            __log.error("ODE MEX " + odeMex + " resulted in an error.");
            sendError(jbiMex, ex);
            return;
        }

        switch (odeMex.getAckType()) {
        case FAULT:
            outResponseFault(odeMex, jbiMex);
            break;
        case RESPONSE:
            outResponse(odeMex, jbiMex);
            break;
        case FAILURE:
            outFailure(odeMex, jbiMex);
            break;
        default:
            __log.fatal("Unexpected AckType:" + odeMex.getAckType());
            sendError(jbiMex, new RuntimeException("Unexpected AckType:" + odeMex.getAckType()));
            
        }

    }

    private void outFailure(MyRoleMessageExchange odeMex, javax.jbi.messaging.MessageExchange jbiMex) {
        try {
            jbiMex.setError(new Exception("MEXFailure: " + odeMex.getFailureType()));
            jbiMex.setStatus(ExchangeStatus.ERROR);
            // TODO: get failure codes out of the message.
            _ode.getChannel().send(jbiMex);
        } catch (MessagingException ex) {
            __log.fatal("Error bridging ODE out response: ", ex);
        }
    }

    private void outResponse(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) {
        InOut inout = (InOut) jbiMex;

        try {
            NormalizedMessage nmsg = inout.createMessage();
            String mapperName = mex.getProperty(Mapper.class.getName());
            Mapper mapper = _ode.getMapper(mapperName);
            if (mapper == null) {
                String errmsg = "Message-mapper " + mapperName + " used in ODE MEX " + mex.getMessageExchangeId()
                        + " is no longer available.";
                __log.error(errmsg);
                throw new MessageTranslationException(errmsg);
            }

            mapper.toNMS(nmsg, mex.getResponse(), mex.getOperation().getOutput().getMessage(), null);

            inout.setOutMessage(nmsg);
            _ode.getChannel().send(inout);

        } catch (MessagingException ex) {
            __log.error("Error bridging ODE out response: ", ex);
            sendError(jbiMex, ex);
        } catch (MessageTranslationException e) {
            __log.error("Error translating ODE message " + mex.getResponse() + " to NMS format!", e);
            sendError(jbiMex, e);
        }
    }

    private void outResponseFault(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) {

        InOut inout = (InOut) jbiMex;

        try {
            Fault flt = inout.createFault();
            String mapperName = mex.getProperty(Mapper.class.getName());
            Mapper mapper = _ode.getMapper(mapperName);
            if (mapper == null) {
                String errmsg = "Message-mapper " + mapperName + " used in ODE MEX " + mex.getMessageExchangeId()
                        + " is no longer available.";
                __log.error(errmsg);
                throw new MessageTranslationException(errmsg);
            }

            QName fault = mex.getFault();
            javax.wsdl.Fault wsdlFault = mex.getOperation().getFault(fault.getLocalPart());
            if (wsdlFault == null) {
                sendError(jbiMex, new MessageTranslationException("Unmapped Fault : " + fault + ": " + mex.getFaultExplanation()));
            } else {
                mapper.toNMS(flt, mex.getFaultResponse(), wsdlFault.getMessage(), fault);
                inout.setFault(flt);
                _ode.getChannel().send(inout);
            }
        } catch (MessagingException e) {
            __log.error("Error bridging ODE fault response: ", e);
            sendError(jbiMex, e);
        } catch (MessageTranslationException mte) {
            __log.error("Error translating ODE fault message " + mex.getFaultResponse() + " to NMS format!", mte);
            sendError(jbiMex, mte);
        }
    }

    private void sendError(javax.jbi.messaging.MessageExchange jbiMex, Exception error) {
        try {
            jbiMex.setError(error);
            jbiMex.setStatus(ExchangeStatus.ERROR);
            _ode.getChannel().send(jbiMex);
        } catch (Exception e) {
            __log.error("Error sending ERROR status: ", e);
        }
    }

    public Endpoint getEndpoint() {
        return _endpoint;
    }

}
