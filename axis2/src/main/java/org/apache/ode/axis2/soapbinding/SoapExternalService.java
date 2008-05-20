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

package org.apache.ode.axis2.soapbinding;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.ExternalService;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.il.OMUtils;
import org.apache.ode.il.epr.EndpointFactory;
import org.apache.ode.il.epr.MutableEndpoint;
import org.apache.ode.il.epr.WSAEndpoint;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.fs.FileWatchDog;
import org.apache.ode.utils.uuid.UUID;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;

/**
 * Acts as a service not provided by ODE. Used mainly for invocation as a way to maintain the WSDL decription of used services.
 *
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class SoapExternalService implements ExternalService, PartnerRoleChannel {

    private static final Log __log = LogFactory.getLog(ExternalService.class);
    private static final int EXPIRE_SERVICE_CLIENT = 30000;
    private static ThreadLocal<CachedServiceClient> _cachedClients = new ThreadLocal<CachedServiceClient>();

    private Definition _definition;
    private QName _serviceName;
    private String _portName;
    private AxisConfiguration _axisConfig;
    private SoapMessageConverter _converter;
    private ProcessConf _pconf;

    public SoapExternalService(Definition definition, QName serviceName, String portName,
                               AxisConfiguration axisConfig, ProcessConf pconf) throws AxisFault {
        _definition = definition;
        _serviceName = serviceName;
        _portName = portName;
        _axisConfig = axisConfig;
        _converter = new SoapMessageConverter(definition, serviceName, portName);
        _pconf = pconf;
    }

    public void invoke(final PartnerRoleMessageExchange odeMex) {
        boolean isTwoWay = odeMex.getMessageExchangePattern() == org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
        try {
            // Override options are passed to the axis MessageContext so we can
            // retrieve them in our session out handler.
            MessageContext mctx = new MessageContext();
            writeHeader(mctx, odeMex);

            _converter.createSoapRequest(mctx, odeMex.getRequest(), odeMex.getOperation());

            SOAPEnvelope soapEnv = mctx.getEnvelope();
            EndpointReference axisEPR = new EndpointReference(((MutableEndpoint) odeMex.getEndpointReference()).getUrl());
            if (__log.isDebugEnabled()) {
                __log.debug("Axis2 sending message to " + axisEPR.getAddress() + " using MEX " + odeMex);
                __log.debug("Message: " + soapEnv);
            }

            Options options = new Options();
            options.setAction(mctx.getSoapAction());
            options.setTo(axisEPR);
            options.setTimeOutInMilliSeconds(60000);
            options.setExceptionToBeThrownOnSOAPFault(false);

            CachedServiceClient cached = getCachedServiceClient();

            final OperationClient operationClient = cached._client.createClient(isTwoWay ? ServiceClient.ANON_OUT_IN_OP
                    : ServiceClient.ANON_OUT_ONLY_OP);
            operationClient.setOptions(options);
            operationClient.addMessageContext(mctx);

            if (isTwoWay) {
                final Operation operation = odeMex.getOperation();

                try {
                    operationClient.execute(true);
                    MessageContext response = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    MessageContext flt = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_FAULT_VALUE);
                    if (response != null && __log.isDebugEnabled())
                        __log.debug("Service response:\n" + response.getEnvelope().toString());

                    if (flt != null) reply(odeMex, operation, flt, true);
                    else reply(odeMex, operation, response, response.isFault());
                } catch (Throwable t) {
                    String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
                    __log.error(errmsg, t);
                    replyWithFailure(odeMex, MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
                }
            } else /* one-way case */{
                operationClient.execute(false);
                odeMex.replyOneWayOk();
            }
        } catch (AxisFault axisFault) {
            String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
            __log.error(errmsg, axisFault);
            odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
        }
    }

    private CachedServiceClient getCachedServiceClient() throws AxisFault {
        CachedServiceClient cached = _cachedClients.get();
        if (cached == null) {
            cached = new CachedServiceClient(new File(_pconf.getBaseURI().resolve(_serviceName.getLocalPart() + ".axis2")), EXPIRE_SERVICE_CLIENT);
            _cachedClients.set(cached);
        }
        try {
            // call manually the check procedure
            // we dont want a dedicated thread for that
            cached.checkAndConfigure();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
        return cached;
    }

    /**
     * Extracts the action to be used for the given operation.  It first checks to see
     * if a value is specified using WS-Addressing in the portType, it then falls back onto
     * getting it from the SOAP Binding.
     * @param operation the name of the operation to get the Action for
     * @return The action value for the specified operation
     */
    private String getAction(String operation) {
        String action = _converter.getWSAInputAction(operation);
        if (action == null || "".equals(action)) action = _converter.getSoapAction(operation);
        return action;
    }

    /**
     * Extracts endpoint information from ODE message exchange to stuff them into Axis MessageContext.
     */
    private void writeHeader(MessageContext ctxt, PartnerRoleMessageExchange odeMex) {
        Options options = ctxt.getOptions();
        WSAEndpoint targetEPR = EndpointFactory.convertToWSA((MutableEndpoint) odeMex.getEndpointReference());
        WSAEndpoint myRoleEPR = EndpointFactory.convertToWSA((MutableEndpoint) odeMex.getMyRoleEndpointReference());

        String partnerSessionId = odeMex.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
        String myRoleSessionId = odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);

        if (partnerSessionId != null) {
            if (__log.isDebugEnabled()) {
                __log.debug("Partner session identifier found for WSA endpoint: " + partnerSessionId);
            }
            targetEPR.setSessionId(partnerSessionId);
        }
        options.setProperty("targetSessionEndpoint", targetEPR);

        if (myRoleEPR != null) {
            if (myRoleSessionId != null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("MyRole session identifier found for myrole (callback) WSA endpoint: " + myRoleSessionId);
                }
                myRoleEPR.setSessionId(myRoleSessionId);
            }
            options.setProperty("callbackSessionEndpoint", odeMex.getMyRoleEndpointReference());
        } else {
            __log.debug("My-Role EPR not specified, SEP will not be used.");
        }

        String action = getAction(odeMex.getOperationName());
        ctxt.setSoapAction(action);

        if (MessageExchange.MessageExchangePattern.REQUEST_RESPONSE == odeMex.getMessageExchangePattern()) {
            EndpointReference annonEpr =
                    new EndpointReference(Namespaces.WS_ADDRESSING_ANON_URI);
            ctxt.setReplyTo(annonEpr);
            ctxt.setMessageID("uuid:" + new UUID().toString());
        }
    }

    public org.apache.ode.bpel.iapi.EndpointReference getInitialEndpointReference() {
        Element eprElmt = ODEService.genEPRfromWSDL(_definition, _serviceName, _portName);
        if (eprElmt == null)
            throw new IllegalArgumentException("Service " + _serviceName + " and port " + _portName
                    + "couldn't be found in provided WSDL document!");
        return EndpointFactory.convertToWSA(ODEService.createServiceRef(eprElmt));
    }

    public void close() {
        // nothing
    }

    public String getPortName() {
        return _portName;
    }

    public QName getServiceName() {
        return _serviceName;
    }

    private void replyWithFailure(final PartnerRoleMessageExchange odeMex, final FailureType error, final String errmsg, final Element details) {
        try {
            odeMex.replyWithFailure(error, errmsg, details);
        } catch (Exception e) {
            String emsg = "Error executing replyWithFailure; reply will be lost.";
            __log.error(emsg, e);

        }

    }

    private void reply(final PartnerRoleMessageExchange odeMex, final Operation operation, final MessageContext reply, final boolean fault) {
        try {
            if (__log.isDebugEnabled()) __log.debug("Received response for MEX " + odeMex);
            if (fault) {
                Document odeMsg = DOMUtils.newDocument();
                Element odeMsgEl = odeMsg.createElementNS(null, "message");
                odeMsg.appendChild(odeMsgEl);
                QName faultType = _converter.parseSoapFault(odeMsgEl, reply.getEnvelope(), operation);
                if (__log.isDebugEnabled()) __log.debug("Reply is a fault, found type: " + faultType);

                if (faultType != null) {
                    if (__log.isWarnEnabled())
                        __log.warn("Fault response: faultType=" + faultType + "\n" + DOMUtils.domToString(odeMsgEl));
                    QName nonNullFT = new QName(Namespaces.ODE_EXTENSION_NS, "unknownFault");
                    Fault f = odeMex.getOperation().getFault(faultType.getLocalPart());
                    if (f != null && f.getMessage().getQName() != null) nonNullFT = f.getMessage().getQName();
                    else __log.debug("Fault " + faultType + " isn't referenced in the service definition, unknown fault.");

                    Message response = odeMex.createMessage(nonNullFT);
                    response.setMessage(odeMsgEl);

                    odeMex.replyWithFault(faultType, response);
                } else {
                    if (__log.isWarnEnabled())
                        __log.warn("Fault response: faultType=(unkown)\n" + reply.getEnvelope().toString());
                    odeMex.replyWithFailure(FailureType.OTHER, reply.getEnvelope().getBody().getFault().getText(),
                            OMUtils.toDOM(reply.getEnvelope().getBody()));
                }
            } else {
                Message response = odeMex.createMessage(odeMex.getOperation().getOutput().getMessage().getQName());
                _converter.parseSoapResponse(response, reply.getEnvelope(), operation);
                if (__log.isInfoEnabled()) __log.info("Response:\n" + (response.getMessage() != null ?
                        DOMUtils.domToString(response.getMessage()) : "empty"));
                odeMex.reply(response);
            }
        } catch (Exception ex) {
            String errmsg = "Unable to process response: " + ex.getMessage();
            __log.error(errmsg, ex);
            odeMex.replyWithFailure(FailureType.OTHER, errmsg, null);
        }

    }


    /**
     * This class wraps a {@link org.apache.axis2.client.ServiceClient} and watches changes (deletions,creations,updates)
     *  on a  Axis2 service config file named {service-name}.axis2.<p/>
     * The {@link org.apache.axis2.client.ServiceClient} instance is created from the main Axis2 config instance and
     * this service-specific config file.
     */
    class CachedServiceClient extends FileWatchDog {
        ServiceClient _client;

        protected CachedServiceClient(File file, long delay) {
            super(file, delay);
        }

        protected boolean isInitialized() throws Exception {
            return _client != null;
        }

        protected void init() throws Exception {
            _client = new ServiceClient(new ConfigurationContext(_axisConfig), null);
        }

        protected void doOnUpdate() throws Exception {
            // axis2 service configuration
            // if the config file has been modified (i.e added or updated), re-create a ServiceClient
            // and load the new config.
            init(); //reset the ServiceClient instance
            try {
                InputStream ais = file.toURI().toURL().openStream();
                if (ais != null) {
                    if (__log.isDebugEnabled()) __log.debug("Configuring service " + _serviceName + " using: " + file);
                    ServiceBuilder builder = new ServiceBuilder(ais, new ConfigurationContext(_client.getAxisConfiguration()), _client.getAxisService());
                    builder.populateService(builder.buildOM());
                }
            } catch (Exception e) {
                if (__log.isWarnEnabled()) __log.warn("Exception while configuring service: " + _serviceName, e);
            }
        }
    }

}
