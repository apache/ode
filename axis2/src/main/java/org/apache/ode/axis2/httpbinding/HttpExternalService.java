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

package org.apache.ode.axis2.httpbinding;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.ExternalService;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.utils.Properties;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.axis2.util.ClusterUrlTransformer;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.w3c.dom.Element;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpExternalService implements ExternalService {

    private static final Log log = LogFactory.getLog(ExternalService.class);
    private static final Messages msgs = Messages.getMessages(Messages.class);

    private MultiThreadedHttpConnectionManager connections;

    protected ExecutorService executorService;
    protected Scheduler scheduler;
    protected BpelServer server;
    protected ProcessConf pconf;
    protected QName serviceName;
    protected String portName;
    protected WSAEndpoint endpointReference;

    protected HttpMethodConverter httpMethodConverter;

    protected Binding portBinding;
    private URL endpointUrl;

    private ClusterUrlTransformer clusterUrlTransformer;

    public HttpExternalService(ProcessConf pconf, QName serviceName, String portName,
                               ExecutorService executorService, Scheduler scheduler, BpelServer server,
                               MultiThreadedHttpConnectionManager connManager, ClusterUrlTransformer clusterUrlTransformer) throws OdeFault {
        if (log.isDebugEnabled())
            log.debug("new HTTP External service, service name=[" + serviceName + "]; port name=[" + portName + "]");
        this.portName = portName;
        this.serviceName = serviceName;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.server = server;
        this.pconf = pconf;
        this.clusterUrlTransformer = clusterUrlTransformer;
        Definition definition = pconf.getDefinitionForService(serviceName);
        Service serviceDef = definition.getService(serviceName);
        if (serviceDef == null)
            throw new IllegalArgumentException(msgs.msgServiceDefinitionNotFound(serviceName));
        Port port = serviceDef.getPort(portName);
        if (port == null)
            throw new IllegalArgumentException(msgs.msgPortDefinitionNotFound(serviceName, portName));
        portBinding = port.getBinding();
        if (portBinding == null)
            throw new IllegalArgumentException(msgs.msgBindingNotFound(portName));

        // validate the http binding
        if (!WsdlUtils.useHTTPBinding(port)) {
            throw new IllegalArgumentException(msgs.msgNoHTTPBindingForPort(portName));
        }
        // throws an IllegalArgumentException if not valid
        new HttpBindingValidator(this.portBinding).validate();

        // initial endpoint reference
        Element eprElmt = ODEService.genEPRfromWSDL(definition, serviceName, portName);
        if (eprElmt == null)
            throw new IllegalArgumentException(msgs.msgPortDefinitionNotFound(serviceName, portName));
        endpointReference = EndpointFactory.convertToWSA(ODEService.createServiceRef(eprElmt));
        try {
            endpointUrl = new URL(endpointReference.getUrl());
        } catch (MalformedURLException e) {
            throw new OdeFault(e);
        }

        httpMethodConverter = new HttpMethodConverter(definition, serviceName, portName);
        connections = connManager;
    }

    public String getPortName() {
        return portName;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void close() {
        connections.shutdown();
    }

    public EndpointReference getInitialEndpointReference() {
        return endpointReference;
    }

    public void invoke(PartnerRoleMessageExchange odeMex) {
        if (log.isDebugEnabled()) log.debug("Preparing " + getClass().getSimpleName() + " invocation...");
        try {
            // note: don't make this map an instance attribute, so we always get the latest version
            final Map<String, String> properties = pconf.getEndpointProperties(endpointReference);
            final HttpParams params = Properties.HttpClient.translate(properties);

            // base baseUrl
            String mexEndpointUrl = ((MutableEndpoint) odeMex.getEndpointReference()).getUrl();
            String baseUrl = mexEndpointUrl;
            // The endpoint URL might be overridden from the properties file(s)
            // The order of precedence is (in descending order): process, property, wsdl.
            if(endpointUrl.equals(new URL(mexEndpointUrl))){
                String address = (String) params.getParameter(Properties.PROP_ADDRESS);
                if(address!=null) {
                    if (log.isDebugEnabled()) log.debug("Endpoint URL overridden by property files. "+mexEndpointUrl+" => "+address);
                    baseUrl = address;
                }
            }else{
                if (log.isDebugEnabled()) log.debug("Endpoint URL overridden by process. "+endpointUrl+" => "+mexEndpointUrl);
            }

            baseUrl = clusterUrlTransformer.rewriteOutgoingClusterURL(baseUrl);

            // build the http method
            final HttpMethod method = httpMethodConverter.createHttpRequest(odeMex, params, baseUrl);

            // create a client
            HttpClient client = new HttpClient(connections);


            // configure the client (proxy, security, etc)
            Element message = odeMex.getRequest().getMessage();
            Element authenticatePart = message == null ? null : DOMUtils.findChildByName(message, new QName(null, "WWW-Authenticate"));
            HttpHelper.configure(client, method.getURI(), authenticatePart, params);

            // this callable encapsulates the http method execution and the process of the response
            final Callable executionCallable;

            // execute it
            boolean isTwoWay = odeMex.getMessageExchangePattern() == MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
            if (isTwoWay) {
                // two way
                executionCallable = new HttpExternalService.TwoWayCallable(client, method, odeMex.getMessageExchangeId(), odeMex.getOperation());
                scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
                    public void afterCompletion(boolean success) {
                        // If the TX is rolled back, then we don't send the request.
                        if (!success) return;
                        // The invocation must happen in a separate thread
                        executorService.submit(executionCallable);
                    }

                    public void beforeCompletion() {
                    }
                });
                odeMex.replyAsync();
            } else {
                // one way, just execute and forget
                executionCallable = new HttpExternalService.OneWayCallable(client, method, odeMex.getMessageExchangeId(), odeMex.getOperation());
                executorService.submit(executionCallable);
                odeMex.replyOneWayOk();
            }
        } catch (UnsupportedEncodingException e) {
            String errmsg = "The returned HTTP encoding isn't supported " + odeMex;
            log.error("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + odeMex.getOperationName() + "] " + errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
        } catch (URIException e) {
            String errmsg = "Error sending message to " + getClass().getSimpleName() + " for ODE mex " + odeMex;
            log.error("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + odeMex.getOperationName() + "] " + errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
        } catch (Exception e) {
            String errmsg = "Unknown HTTP call error for ODE mex " + odeMex;
            log.error("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + odeMex.getOperationName() + "] " + errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, null);
        }
    }

    private class OneWayCallable implements Callable<Void> {
        HttpMethod method;
        String mexId;
        Operation operation;
        HttpClient client;

        public OneWayCallable(HttpClient client, HttpMethod method, String mexId, Operation operation) {
            this.method = method;
            this.mexId = mexId;
            this.operation = operation;
            this.client = client;
        }

        public Void call() throws Exception {
            try {
                // simply execute the http method
                if (log.isDebugEnabled()) {
                    log.debug("Executing HTTP Request : " + method.getName() + " " + method.getURI());
                    log.debug(HttpHelper.requestToString(method));
                }

                final int statusCode = client.executeMethod(method);
                // invoke getResponseBody to force the loading of the body
                // Actually the processResponse may happen in a separate thread and
                // as a result the connection might be closed before the body processing (see the finally clause below).
                byte[] responseBody = method.getResponseBody();
                // ... and process the response
                if (log.isDebugEnabled()) {
                    log.debug("Received response for MEX " + mexId);
                    log.debug(HttpHelper.responseToString(method));
                }
                processResponse(statusCode);
            } catch (final IOException e) {
                // ODE MEX needs to be invoked in a TX.
                try {
                    scheduler.execTransaction(new Callable<Void>() {
                        public Void call() throws Exception {
                            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
                            String errmsg = "Unable to execute http request : " + e.getMessage();
                            log.error("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] " + errmsg, e);
                            odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
                            return null;
                        }
                    });
                } catch (Exception e1) {
                    String errmsg = "[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] Error executing reply transaction; reply will be lost.";
                    log.error(errmsg, e);
                }
            } finally {
                method.releaseConnection();
            }
            return null;
        }

        public void processResponse(int statusCode) {
            // a one-way message does not care about the response
            try {
                // log the URI since the engine may have moved on while this One Way request was executing
                if (statusCode >= 400) {
                    log.error("OneWay HTTP Request failed, Status-Line: " + method.getStatusLine() + " for " + method.getURI());
                } else {
                    if (log.isDebugEnabled())
                        log.debug("OneWay HTTP Request, Status-Line: " + method.getStatusLine() + " for " + method.getURI());
                }
            } catch (Exception e) {
                String errmsg = "[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] Exception occured while processing the HTTP response of a one-way request: " + e.getMessage();
                log.error(errmsg, e);
            }
        }
    }

    private class TwoWayCallable extends OneWayCallable {
        public TwoWayCallable(org.apache.commons.httpclient.HttpClient client, HttpMethod method, String mexId, Operation operation) {
            super(client, method, mexId, operation);
        }

        public void processResponse(final int statusCode) {
            // ODE MEX needs to be invoked in a TX.
            try {
                scheduler.execTransaction(new Callable<Void>() {
                    public Void call() throws Exception {
                        try {
                            if (statusCode >= 200 && statusCode < 300) {
                                _2xx_success();
                            } else if (statusCode >= 300 && statusCode < 400) {
                                _3xx_redirection();
                            } else if (statusCode >= 400 && statusCode < 600) {
                                _4xx_5xx_error();
                            } else {
                                unmanagedStatus();
                            }
                        } catch (Exception e) {
                            replyWithFailure("Exception occured while processing the HTTP response of a two-way request. mexId= " + mexId, e);
                        }
                        return null;
                    }
                });
            } catch (Exception transactionException) {
                String errmsg = "[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] Error executing reply transaction; reply will be lost.";
                log.error(errmsg, transactionException);
            }
        }

        private void unmanagedStatus() throws Exception {
            replyWithFailure("Unmanaged Status Code! Status-Line: " + method.getStatusLine() + " for " + method.getURI());
        }

        private void _4xx_5xx_error() throws Exception {
            int status = method.getStatusCode();
            if (HttpHelper.isFaultOrFailure(status) > 0) {
                // reply with a fault, meaning the request should not be repeated
                replyWithFault();
            } else {
                // reply with a failure, meaning the request might be repeated later
                replyWithFailure("HTTP Status-Line: " + method.getStatusLine() + " for " + method.getURI());
            }
        }

        private void _3xx_redirection() throws Exception {
            // redirections should be handled transparently by http-client
            replyWithFailure("Redirections disabled! HTTP Status-Line: " + method.getStatusLine() + " for " + method.getURI());
        }

        private void _2xx_success() throws Exception {
            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
            if (log.isDebugEnabled())
                log.debug("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] HTTP Status-Line: " + method.getStatusLine() + " for " + method.getURI());
            if (log.isDebugEnabled()) log.debug("Received response for MEX " + odeMex);

            Operation opDef = odeMex.getOperation();

            // this is the message to populate and send to ODE
            QName outputMsgName = odeMex.getOperation().getOutput().getMessage().getQName();
            Message odeResponse = odeMex.createMessage(outputMsgName);

            httpMethodConverter.parseHttpResponse(odeResponse, method, opDef);

            // finally send the message
            try {
                if (log.isInfoEnabled())
                    log.info("Response: " + (odeResponse.getMessage() != null ? DOMUtils.domToString(odeResponse.getMessage()) : "empty"));
                odeMex.reply(odeResponse);
            } catch (Exception ex) {
                replyWithFailure("Unable to process response: " + ex.getMessage(), ex);
            }
        }

        void replyWithFault() {
            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
            Object[] fault = httpMethodConverter.parseFault(odeMex, method);
            Message response = (Message) fault[1];
            QName faultName = (QName) fault[0];

            // finally send the fault. We did it!
            if (log.isWarnEnabled())
                log.warn("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] Fault response: faultName=" + faultName + " faultType=" + response.getType() + "\n" + DOMUtils.domToString(response.getMessage()));

            odeMex.replyWithFault(faultName, response);
        }


        void replyWithFailure(String errmsg) {
            replyWithFailure(errmsg, null);
        }

        void replyWithFailure(String errmsg, Throwable t) {
            log.error("[Service: " + serviceName + ", Port: " + portName + ", Operation: " + operation.getName() + "] " + errmsg, t);
            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, HttpHelper.prepareDetailsElement(method));
        }
    }
}