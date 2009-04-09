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

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.Policy;
import org.apache.ode.axis2.ExternalService;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.utils.Properties;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.axis2.util.AxisUtils;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.il.OMUtils;
import org.apache.ode.il.epr.EndpointFactory;
import org.apache.ode.il.epr.MutableEndpoint;
import org.apache.ode.il.epr.WSAEndpoint;
import org.apache.ode.utils.*;
import org.apache.ode.utils.uuid.UUID;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.rampart.RampartMessageData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * Acts as a service not provided by ODE. Used mainly for invocation as a way to maintain the WSDL decription of used services.
 *
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class SoapExternalService implements ExternalService, PartnerRoleChannel {

    private static final Log __log = LogFactory.getLog(ExternalService.class);
    private static final int EXPIRE_SERVICE_CLIENT = 30000;

    private static ThreadLocal<ServiceClient> _cachedClients = new ThreadLocal<ServiceClient>();
    private WatchDog<Map, OptionsObserver> _axisOptionsWatchDog;
    private WatchDog<Long, ServiceFileObserver> _axisServiceWatchDog;
    private ConfigurationContext _configContext;


    private static final org.apache.ode.utils.wsdl.Messages msgs = Messages.getMessages(Messages.class);

    private Definition _definition;
    private QName _serviceName;
    private String _portName;
    protected WSAEndpoint endpointReference;
    private AxisConfiguration _axisConfig;
    private SoapMessageConverter _converter;
    private ProcessConf _pconf;
    private URL endpointUrl;

    public SoapExternalService(Definition definition, QName serviceName, String portName,
                               AxisConfiguration axisConfig, ProcessConf pconf, MultiThreadedHttpConnectionManager connManager) throws AxisFault {
        _definition = definition;
        _serviceName = serviceName;
        _portName = portName;
        _axisConfig = axisConfig;
        _converter = new SoapMessageConverter(definition, serviceName, portName);
        _pconf = pconf;

        File fileToWatch = new File(_pconf.getBaseURI().resolve(_serviceName.getLocalPart() + ".axis2"));
        _axisServiceWatchDog = WatchDog.watchFile(fileToWatch, new ServiceFileObserver(fileToWatch));
        _axisOptionsWatchDog = new WatchDog<Map, OptionsObserver>(new EndpointPropertiesMutable(), new OptionsObserver());
        _configContext = new ConfigurationContext(_axisConfig);
        _configContext.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
        // make sure the client is not shared, see also org.apache.ode.axis2.Properties.Axis2
        _configContext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "false");

        // initial endpoint reference
        Element eprElmt = ODEService.genEPRfromWSDL(_definition, serviceName, portName);
        if (eprElmt == null)
            throw new IllegalArgumentException(msgs.msgPortDefinitionNotFound(serviceName, portName));
        endpointReference = EndpointFactory.convertToWSA(ODEService.createServiceRef(eprElmt));
        try {
            endpointUrl = new URL(endpointReference.getUrl());
        } catch (MalformedURLException e) {
            throw new OdeFault(e);
        }
    }


    public void invoke(final PartnerRoleMessageExchange odeMex) {
        boolean isTwoWay = odeMex.getMessageExchangePattern() == org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
        try {

            ServiceClient client = getServiceClient();

            // Override options are passed to the axis MessageContext so we can
            // retrieve them in our session out changeHandler.
            final MessageContext mctx = new MessageContext();
            try {
                /* make the given options the parent so it becomes the defaults of the MessageContexgt. That allows the user to override
                *  specific options on a given message context and not affect the overall options.
                */
                mctx.getOptions().setParent(client.getOptions());
                writeHeader(mctx, odeMex);

                _converter.createSoapRequest(mctx, odeMex.getRequest(), odeMex.getOperation());

                SOAPEnvelope soapEnv = mctx.getEnvelope();
                String mexEndpointUrl = ((MutableEndpoint) odeMex.getEndpointReference()).getUrl();

                EndpointReference axisEPR = new EndpointReference(mexEndpointUrl);
                // The endpoint URL might be overridden from the properties file(s)
                // The order of precedence is (in descending order): process, property, wsdl.
                if (endpointUrl.equals(new URL(mexEndpointUrl))) {
                    String address = (String) client.getOptions().getProperty(Properties.PROP_ADDRESS);
                    if (address != null) {
                        if (__log.isDebugEnabled())
                            __log.debug("Endpoint URL overridden by property files. " + mexEndpointUrl + " => " + address);
                        axisEPR.setAddress(address);
                    }
                } else {
                    if (__log.isDebugEnabled())
                        __log.debug("Endpoint URL overridden by process. " + endpointUrl + " => " + mexEndpointUrl);
                }
                if (__log.isDebugEnabled()) {
                    __log.debug("Axis2 sending message to " + axisEPR.getAddress() + " using MEX " + odeMex);
                    __log.debug("Message: " + soapEnv);
                }

                final OperationClient operationClient = client.createClient(isTwoWay ? ServiceClient.ANON_OUT_IN_OP
                        : ServiceClient.ANON_OUT_ONLY_OP);
                operationClient.addMessageContext(mctx);
                // this Options can be alter without impacting the ServiceClient options (which is a requirement)
                Options operationOptions = operationClient.getOptions();

                // provide HTTP credentials if any
                AuthenticationHelper.setHttpAuthentication(odeMex, operationOptions);

                operationOptions.setAction(mctx.getSoapAction());
                operationOptions.setTo(axisEPR);

                operationClient.execute(true);
                if (isTwoWay) {
                    final Operation operation = odeMex.getOperation();
                    MessageContext response = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    MessageContext flt = operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_FAULT_VALUE);
                    if (response != null && __log.isDebugEnabled())
                        __log.debug("Service response:\n" + response.getEnvelope().toString());

                    if (flt != null) reply(odeMex, operation, flt, true);
                    else reply(odeMex, operation, response, response.isFault());

                } else {  /* one-way case */
                    odeMex.replyOneWayOk();
                }
            } finally {
                 // make sure the HTTP connection is released to the pool!
                TransportOutDescription out = mctx.getTransportOut();
                if (out != null && out.getSender() != null) {
                    out.getSender().cleanup(mctx);
                }
            }
        } catch (Throwable t) {
            String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
            __log.error(errmsg, t);
            replyWithFailure(odeMex, MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg);
        }
    }

    private ServiceClient getServiceClient() throws AxisFault {
        try {
            // call manually the check procedure
            // we dont want a dedicated thread for that
            _axisServiceWatchDog.check();
            _axisOptionsWatchDog.check();
        } catch (RuntimeException e) {
            throw AxisFault.makeFault(e.getCause() != null ? e.getCause() : e);
        }
        AxisService anonymousService = _axisServiceWatchDog.getObserver().get();
        ServiceClient client = _cachedClients.get();
        if (client == null || !client.getAxisService().getName().equals(anonymousService.getName())) {
            // avoid race conditions in AxisConfiguration
            synchronized (_axisConfig) {
                // if the service has changed, discard the client and create a new one
                if (client != null) {
                    if (__log.isDebugEnabled()) __log.debug("Clean up and discard ServiceClient");
                    client.cleanup();
                }
                if (__log.isDebugEnabled())
                    __log.debug("Create a new ServiceClient for " + anonymousService.getName());
                client = new ServiceClient(_configContext, null);
                client.setAxisService(anonymousService);
            }
            _cachedClients.set(client);
        }

        // apply the options to the service client
        client.setOptions(_axisOptionsWatchDog.getObserver().get());
        return client;
    }

    private void applySecurityPolicy(Options options) {
        if (options != null && options.getProperty(Properties.PROP_SECURITY_POLICY) != null) {
            String policy = (String) options.getProperty(Properties.PROP_SECURITY_POLICY);
            AxisService service = _axisServiceWatchDog.getObserver().get();
            AxisUtils.applySecurityPolicy(service, policy);
        }
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
        WSAEndpoint targetWSAEPR = EndpointFactory.convertToWSA((MutableEndpoint) odeMex.getEndpointReference());
        WSAEndpoint myRoleWSAEPR = EndpointFactory.convertToWSA((MutableEndpoint) odeMex.getMyRoleEndpointReference());
        WSAEndpoint targetEPR = new WSAEndpoint(targetWSAEPR);

        String partnerSessionId = odeMex.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
        String myRoleSessionId = odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);

        if (partnerSessionId != null) {
            if (__log.isDebugEnabled()) {
                __log.debug("Partner session identifier found for WSA endpoint: " + partnerSessionId);
            }
            targetEPR.setSessionId(partnerSessionId);
        }
        options.setProperty(ODEService.TARGET_SESSION_ENDPOINT, targetEPR);

        if (myRoleWSAEPR != null) {
            WSAEndpoint myRoleEPR = new WSAEndpoint(myRoleWSAEPR);
            if (myRoleSessionId != null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("MyRole session identifier found for myrole (callback) WSA endpoint: " + myRoleSessionId);
                }
                myRoleEPR.setSessionId(myRoleSessionId);
            }
            options.setProperty(ODEService.CALLBACK_SESSION_ENDPOINT, myRoleEPR);
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
        return endpointReference;
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

    private void replyWithFailure(final PartnerRoleMessageExchange odeMex, final FailureType error, final String errmsg) {
        try {
            odeMex.replyWithFailure(error, errmsg, null);
        } catch (Exception e) {
            String emsg = "Error executing replyWithFailure; reply will be lost.";
            __log.error(emsg, e);

        }
    }

    private void reply(final PartnerRoleMessageExchange odeMex, final Operation operation, final MessageContext reply, final boolean isFault) {
        try {
            if (__log.isDebugEnabled()) __log.debug("Received response for MEX " + odeMex);
            if (isFault) {
                Document odeMsg = DOMUtils.newDocument();
                Element odeMsgEl = odeMsg.createElementNS(null, "message");
                odeMsg.appendChild(odeMsgEl);
                Fault fault = _converter.parseSoapFault(odeMsgEl, reply.getEnvelope(), operation);

                if (fault != null) {
                    if (__log.isWarnEnabled())
                        __log.warn("Fault response: faultName=" + fault.getName() + " faultType="+fault.getMessage().getQName()+ "\n" + DOMUtils.domToString(odeMsgEl));

                    QName faultType = fault.getMessage().getQName();
                    QName faultName = new QName(_definition.getTargetNamespace(), fault.getName());
                    Message response = odeMex.createMessage(faultType);
                    response.setMessage(odeMsgEl);

                    odeMex.replyWithFault(faultName, response);
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
     * on a  Axis2 service config file named {service-name}.axis2.<p/>
     * The {@link org.apache.axis2.client.ServiceClient} instance is created from the main Axis2 config instance and
     * this service-specific config file.
     */
    private class ServiceFileObserver extends WatchDog.DefaultObserver<AxisService> {
        File file;

        private ServiceFileObserver(File file) {
            this.file = file;
        }

        public void init() {
            // create an anonymous axis service that will be used by the ServiceClient
            // this service will be added to the AxisConfig so do not reuse the name of the external service
            // as it could blow up if the service is deployed in the same axis2 instance
            String serviceName = "axis_service_for_" + _serviceName + "#" + _portName + "_" + new GUID().toString();
            object = new AxisService(serviceName);
            object.setParent(_axisConfig);

            OutOnlyAxisOperation outOnlyOperation = new OutOnlyAxisOperation(ServiceClient.ANON_OUT_ONLY_OP);
            object.addOperation(outOnlyOperation);

            OutInAxisOperation outInOperation = new OutInAxisOperation(ServiceClient.ANON_OUT_IN_OP);
            object.addOperation(outInOperation);

            // set a right default action *after* operations have been added to the service.
            outOnlyOperation.setSoapAction("");
            outInOperation.setSoapAction("");
        }

        public void onUpdate() {
            // axis2 service configuration
            // if the config file has been modified (i.e added or updated), re-create a ServiceClient
            // and load the new config.
            init(); // create a new ServiceClient instance
            try {
                String name = object.getName();
                 AxisUtils.configureService(_configContext, object, file.toURI().toURL());
                 // do not allow the service.xml file to change the service name
                 object.setName(name);
            } catch (Exception e) {
                if (__log.isWarnEnabled()) __log.warn("Exception while configuring service: " + _serviceName, e);
                throw new RuntimeException("Exception while configuring service: " + _serviceName, e);
            }
            Options options = _axisOptionsWatchDog.getObserver().get();
            applySecurityPolicy(options);
        }
    }

    private class OptionsObserver extends WatchDog.DefaultObserver<Options> {

        public void init() {
            object = new Options();
            // set defaults values
            object.setExceptionToBeThrownOnSOAPFault(false);

            // this value does NOT override Properties.PROP_HTTP_CONNECTION_TIMEOUT
            // nor Properties.PROP_HTTP_SOCKET_TIMEOUT.
            // it will be applied only if the laters are not set.
            object.setTimeOutInMilliSeconds(60000);
        }

        public void onUpdate() {
            init();

            // note: don't make this map an instance attribute, so we always get the latest version
            final Map<String, String> properties = _pconf.getEndpointProperties(endpointReference);
            Properties.Axis2.translate(properties, object);

            applySecurityPolicy(object);
        }
    }


    private class EndpointPropertiesMutable implements WatchDog.Mutable<Map> {
        // ProcessConf#getProperties(String...) cannot return null (by contract)
        public boolean exists() {
            return true;
        }

        public boolean hasChangedSince(Map since) {
            Map latest = lastModified();  // cannot be null but better be prepared
            // check if mappings are equal
            return !CollectionUtils.equals(latest, since);
        }

        public Map lastModified() {
            return _pconf.getEndpointProperties(endpointReference);
        }

        public String toString() {
            return "Properties for Endpoint: " + _serviceName + "#" + _portName;
        }
    }

}
