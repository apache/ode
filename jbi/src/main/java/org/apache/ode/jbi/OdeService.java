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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.Scheduler.Synchronizer;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.jbi.msgmap.MessageTranslationException;
import org.w3c.dom.Element;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridge JBI (consumer) to ODE (provider).
 */
public class OdeService extends ServiceBridge implements JbiMessageExchangeProcessor {

    private static final Log __log = LogFactory.getLog(OdeService.class);

    /** utility for tracking outstanding JBI message exchanges. */
    private final JbiMexTracker _jbiMexTracker = new JbiMexTracker();

    /** JBI-Generated Endpoint */
    private ServiceEndpoint _internal;

    /** External endpoint. */
    private ServiceEndpoint _external;

    private OdeContext _ode;

    private Element _serviceref;

    private Endpoint _endpoint;

    private int count;

    public OdeService(OdeContext odeContext, Endpoint endpoint) throws Exception {
        _ode = odeContext;
        _endpoint = endpoint;
    }

    public int getCount() {
        return count;
    }
    /**
     * Do the JBI endpoint activation.
     *
     * @throws JBIException
     */
    public void activate() throws JBIException {
        count++;
        if(count != 1)
            return;
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
        count--;
        if(count != 0)
            return;
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
            if (__log.isDebugEnabled()) {
                __log.debug("Consuming MEX tracker " + jbiMex.getExchangeId());
            }
            _jbiMexTracker.consume(jbiMex.getExchangeId());
            return;
        }

        if (jbiMex.getOperation() == null) {
            throw new IllegalArgumentException("Null operation in JBI message exchange id=" + jbiMex.getExchangeId()
                                                + " endpoint=" + _endpoint);
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
                    if (err != null && jbiMex.getError() == null)  {
                        jbiMex.setError(err);
                    }
                    jbiMex.setStatus(ExchangeStatus.ERROR);
                    _ode.getChannel().send(jbiMex);
                }
            }
        } else {
            __log.error("JBI MessageExchange " + jbiMex.getExchangeId() + " is of an unsupported pattern "
                    + jbiMex.getPattern());
            jbiMex.setStatus(ExchangeStatus.ERROR);
            jbiMex.setError(new Exception("Unknown message exchange pattern: " + jbiMex.getPattern()));
        }

    }

    /**
     * Called from
     * {@link MessageExchangeContextImpl#onAsyncReply(MyRoleMessageExchange)}
     *
     * @param mex
     *            message exchange
     */
    public void onResponse(MyRoleMessageExchange mex) {
        final String clientId = mex.getClientId();
        final String mexId = mex.getMessageExchangeId();
        if (__log.isDebugEnabled()) {
            __log.debug("Processing MEX tracker mexId: " + mexId + " clientId: " + clientId);
        }
        final javax.jbi.messaging.MessageExchange jbiMex = _jbiMexTracker.peek(clientId);
        if (jbiMex == null) {
            __log.warn("Ignoring unknown async reply. mexId: " + mexId + " clientId: " + clientId);
            return;
        }

        try {
        switch (mex.getStatus()) {
        case FAULT:
            outResponseFault(mex, jbiMex);
            break;
        case RESPONSE:
            outResponse(mex, jbiMex);
            break;
        case FAILURE:
            outFailure(mex, jbiMex);
            break;
        default:
            __log.warn("Received ODE message exchange in unexpected state: " + mex.getStatus() + " mexId: " + mexId + " clientId: " + clientId);
        }

        mex.release(mex.getStatus() == MessageExchange.Status.RESPONSE);
        _ode._scheduler.registerSynchronizer(new Synchronizer() {
            public void afterCompletion(boolean success) {
                if (success) {
                    //Deliver reply to external world only if ODE scheduler's job has completed successfully
                    try {
                        _ode.getChannel().send(jbiMex);
                        if (__log.isDebugEnabled()) {
                            __log.debug("Consuming MEX tracker mexId: " + mexId + " clientId: " + clientId);
                        }
                    _jbiMexTracker.consume(clientId);
                    } catch (MessagingException e) {
                        __log.error("Error delivering response from ODE to JBI mexId: " + mexId + " clientId: " + clientId, e);
                    }
                }
            }

            public void beforeCompletion() {
            }
        });
        } catch (MessagingException e) {
            __log.error("Error processing response from ODE to JBI mexId: " + mexId + " clientId: " + clientId, e);
        }
    }

    /**
     * Forward a JBI input message to ODE.
     *
     * @param jbiMex
     */
    private void invokeOde(javax.jbi.messaging.MessageExchange jbiMex, NormalizedMessage request) throws Exception {

        // If this has already been tracked, we will not invoke!
        if (_jbiMexTracker.track(jbiMex)) {
            if (__log.isDebugEnabled()) {
                __log.debug("Skipping JBI MEX " + jbiMex.getExchangeId() + ", already received!");
            }
            return;
        }

        _ode.getTransactionManager().begin();

        boolean success = false;
        MyRoleMessageExchange odeMex = null;
        try {
            if (__log.isDebugEnabled()) {
                __log.debug("invokeOde() JBI exchangeId=" + jbiMex.getExchangeId() + " endpoint=" + _endpoint
                        + " operation=" + jbiMex.getOperation());
            }
            odeMex = _ode._server.getEngine().createMessageExchange(jbiMex.getExchangeId(), _endpoint.serviceName,
                    jbiMex.getOperation().getLocalPart());

            if (odeMex.getOperation() != null) {
                copyMexProperties(odeMex, jbiMex);
                javax.wsdl.Message msgdef = odeMex.getOperation().getInput().getMessage();
                Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
                Mapper mapper = _ode.findMapper(request, odeMex.getOperation());
                if (mapper == null) {
                    String errmsg = "Could not find a mapper for request message for JBI MEX " + jbiMex.getExchangeId()
                            + "; ODE MEX " + odeMex.getMessageExchangeId() + " is failed. ";
                    __log.error(errmsg);
                    throw new MessageTranslationException(errmsg);

                }
                odeMex.setProperty(Mapper.class.getName(), mapper.getClass().getName());
                mapper.toODE(odeRequest, request, msgdef);
                odeMex.invoke(odeRequest);

                // Handle the response if it is immediately available.
                if (odeMex.getStatus() != Status.ASYNC) {
                    if (__log.isDebugEnabled()) {
                        __log.debug("ODE MEX " + odeMex + " completed SYNCHRONOUSLY.");
                    }
                    onResponse(odeMex);
                    _jbiMexTracker.consume(jbiMex.getExchangeId());
                } else {
                    if (__log.isDebugEnabled()) {
                        __log.debug("ODE MEX " + odeMex + " completed ASYNCHRONOUSLY.");
                    }
                }
            } else {
                __log.error("ODE MEX " + odeMex + " was unroutable.");
                setError(jbiMex, new IllegalArgumentException("Unroutable invocation."));
            }

            success = true;
            // For one-way invocation we do not need to maintain the association
            if (jbiMex.getPattern().equals(org.apache.ode.jbi.MessageExchangePattern.IN_ONLY)) {
                if (__log.isDebugEnabled()) {
                    __log.debug("Consuming non Req/Res MEX tracker " + jbiMex.getExchangeId() + " with pattern " + jbiMex.getPattern());
                }
                _jbiMexTracker.consume(jbiMex.getExchangeId());
            }

        } finally {
            if (success) {
                if (__log.isDebugEnabled()) {
                    __log.debug("Commiting ODE MEX " + odeMex);
                }
                _ode.getTransactionManager().commit();
            } else {
                if (__log.isDebugEnabled()) {
                    __log.debug("Rolling back ODE MEX " + odeMex);
                }
                _jbiMexTracker.consume(jbiMex.getExchangeId());
                _ode.getTransactionManager().rollback();

            }
        }

    }

    private void outFailure(MyRoleMessageExchange odeMex, javax.jbi.messaging.MessageExchange jbiMex) throws MessagingException {
        jbiMex.setError(new Exception("MEXFailure"));
        jbiMex.setStatus(ExchangeStatus.ERROR);
        // TODO: get failure codes out of the message.
    }

    private void outResponse(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) throws MessagingException {
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
        } catch (MessageTranslationException e) {
            __log.error("Error translating ODE message " + mex.getResponse() + " to NMS format!", e);
            setError(jbiMex, e);
        }
    }

    private void outResponseFault(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) throws MessagingException {
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
               setError(jbiMex, new MessageTranslationException("Unmapped Fault : " + fault + ": " + mex.getFaultExplanation()));
            } else {
                mapper.toNMS(flt, mex.getFaultResponse(), wsdlFault.getMessage(), fault);
                inout.setFault(flt);
            }
        } catch (MessageTranslationException mte) {
            __log.error("Error translating ODE fault message " + mex.getFaultResponse() + " to NMS format!", mte);
            setError(jbiMex, mte);
        }
    }

    private void setError(javax.jbi.messaging.MessageExchange jbiMex, Exception error) throws MessagingException {
        jbiMex.setError(error);
        jbiMex.setStatus(ExchangeStatus.ERROR);
    }

    public Endpoint getEndpoint() {
        return _endpoint;
    }

    /**
     * Class for tracking outstanding message exchanges from JBI.
     */
    private static class JbiMexTracker {
        /**
         * Outstanding JBI-initiated exchanges: mapping for JBI MEX ID to JBI
         * MEX
         */
        private Map<String, javax.jbi.messaging.MessageExchange> _outstandingJbiExchanges = new HashMap<String, javax.jbi.messaging.MessageExchange>();

        synchronized boolean track(javax.jbi.messaging.MessageExchange jbiMex) {
            boolean found = _outstandingJbiExchanges.containsKey(jbiMex.getExchangeId());
            _outstandingJbiExchanges.put(jbiMex.getExchangeId(), jbiMex);
            return found;
        }

        synchronized javax.jbi.messaging.MessageExchange peek(String clientId) {
            return _outstandingJbiExchanges.get(clientId);
        }

        synchronized javax.jbi.messaging.MessageExchange consume(String clientId) {
            return _outstandingJbiExchanges.remove(clientId);
        }
    }
}
