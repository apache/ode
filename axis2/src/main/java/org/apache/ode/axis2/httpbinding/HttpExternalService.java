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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.ExternalService;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.axis2.Properties;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.w3c.dom.Element;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;


/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpExternalService implements ExternalService {

    private static final Log log = LogFactory.getLog(HttpExternalService.class);
    private static final Messages msgs = Messages.getMessages(Messages.class);

    private MultiThreadedHttpConnectionManager connections;

    protected ExecutorService executorService;
    protected Scheduler scheduler;
    protected BpelServer server;
    protected ProcessConf pconf;
    private String targetNamespace;
    protected QName serviceName;
    protected String portName;
    protected WSAEndpoint endpointReference;

    protected HttpMethodConverter httpMethodConverter;

    protected Binding portBinding;

    public HttpExternalService(ProcessConf pconf, QName serviceName, String portName, ExecutorService executorService, Scheduler scheduler, BpelServer server) {
        if(log.isDebugEnabled()) log.debug("new HTTP External service, service name=["+serviceName+"]; port name=["+portName+"]");
        this.portName = portName;
        this.serviceName = serviceName;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.server = server;
        this.pconf = pconf;
        Definition definition = pconf.getDefinitionForService(serviceName);
        targetNamespace = definition.getTargetNamespace();
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

        httpMethodConverter = new HttpMethodConverter(this.portBinding);
        connections = new MultiThreadedHttpConnectionManager();
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

            // build the http method
            final HttpMethod method = httpMethodConverter.createHttpRequest(odeMex, params);

            // create a client
            HttpClient client = new HttpClient(connections);
            // don't forget to wire params so that EPR properties are passed around
            client.getParams().setDefaults(params);

            // configure the client (proxy, security, etc)
            HttpHelper.configure(client.getHostConfiguration(), client.getState(), method.getURI(), params);

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
            log.error("[Service: "+serviceName+", Port: "+portName+", Operation: "+odeMex.getOperationName()+"] "+errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
        } catch (URIException e) {
            String errmsg = "Error sending message to " + getClass().getSimpleName() + " for ODE mex " + odeMex;
            log.error("[Service: "+serviceName+", Port: "+portName+", Operation: "+odeMex.getOperationName()+"] "+errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
        } catch (Exception e) {
            String errmsg = "Unknown HTTP call error for ODE mex " + odeMex;
            log.error("[Service: "+serviceName+", Port: "+portName+", Operation: "+odeMex.getOperationName()+"] "+errmsg, e);
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
                if (log.isDebugEnabled()){
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
                    scheduler.execIsolatedTransaction(new Callable<Void>() {
                        public Void call() throws Exception {
                            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
                            String errmsg = "Unable to execute http request : " + e.getMessage();
                            log.error("[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] "+errmsg, e);
                            odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
                            return null;
                        }
                    });
                } catch (Exception e1) {
                    String errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Error executing reply transaction; reply will be lost.";
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
                String errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Exception occured while processing the HTTP response of a one-way request: " + e.getMessage();
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
                scheduler.execIsolatedTransaction(new Callable<Void>() {
                    public Void call() throws Exception {
                        try {
                            if (statusCode >= 200 && statusCode < 300) {
                                _2xx_success();
                            } else if (statusCode >= 300 && statusCode < 400) {
                                _3xx_redirection();
                            } else if (statusCode >= 400 && statusCode < 500) {
                                _4xx_badRequest();
                            } else if (statusCode >= 500 && statusCode < 600) {
                                _5xx_serverError();
                            } else {
                                unmanagedStatus();
                            }
                        } catch (Exception e) {
                            String errmsg = "Exception occured while processing the HTTP response of a two-way request: " + e.getMessage();
                            log.error("[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] "+errmsg, e);
                            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
                            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
                        }
                        return null;
                    }
                });
            } catch (Exception transactionException) {
                String errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Error executing reply transaction; reply will be lost.";
                log.error(errmsg, transactionException);
            }
        }

        private void unmanagedStatus() throws IOException {
            replyWithFailure("Unmanaged Status Code! Status-Line: " + method.getStatusLine() + " for " + method.getURI());
        }

        /**
         * For 500s if a fault is defined in the WSDL and the response body contains the corresponding xml doc, then reply with a fault ; else reply with failure.
         *
         * @throws IOException
         */
        private void _5xx_serverError() throws IOException {
            String errmsg;
            if (log.isWarnEnabled()) {
                errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Status-Line: " + method.getStatusLine() + " for " + method.getURI();
                log.warn(errmsg);
            }
            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
            Operation opDef = odeMex.getOperation();
            BindingOperation opBinding = portBinding.getBindingOperation(opDef.getName(), opDef.getInput().getName(), opDef.getOutput().getName());

            final String body;
            try {
                body = method.getResponseBodyAsString();
            } catch (IOException e) {
                errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Unable to get the request body : " + e.getMessage();
                log.error(errmsg, e);
                odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, HttpHelper.prepareDetailsElement(method));
                return;
            }
            Header h = method.getResponseHeader("Content-Type");
            String receivedType = h != null ? h.getValue() : null;
            if (opDef.getFaults().isEmpty()) {
                replyWithFailure("Operation [" + opDef.getName() + "] has no fault. This 500 error will be considered as a failure.");
            } else if (opBinding.getBindingFaults().isEmpty()) {
                replyWithFailure("No fault binding. This 500 error will be considered as a failure.");
            } else if (StringUtils.isEmpty(body)) {
                replyWithFailure("No body in the response. This 500 error will be considered as a failure.");
            } else if (receivedType != null && !HttpHelper.isXml(receivedType)) {
                replyWithFailure("Response Content-Type [" + receivedType + "] does not describe XML entities. Faults must be XML. This 500 error will be considered as a failure.");
            } else {

                if (receivedType == null) {
                    if (log.isWarnEnabled())
                        log.warn("[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Received Response with a body but no 'Content-Type' header! Will try to parse nevertheless.");
                }

                // try to parse body
                final Element bodyElement;
                try {
                    bodyElement = DOMUtils.stringToDOM(body);
                } catch (Exception e) {
                    replyWithFailure("Unable to parse the response body as xml. This 500 error will be considered as a failure.");
                    return;
                }

                // Guess which fault it is
                QName bodyName = new QName(bodyElement.getNamespaceURI(), bodyElement.getNodeName());
                Fault faultDef = WsdlUtils.inferFault(opDef, bodyName);

                if (faultDef == null) {
                    replyWithFailure("Unknown Fault Type [" + bodyName + "] This 500 error will be considered as a failure.");
                } else if (!WsdlUtils.isOdeFault(opBinding.getBindingFault(faultDef.getName()))) {
                    // is this fault bound with ODE extension?
                    replyWithFailure("Fault [" + bodyName + "] is not bound with " + new QName(Namespaces.ODE_HTTP_EXTENSION_NS, "fault") + ". This 500 error will be considered as a failure.");
                } else {
                    // a fault has only one part
                    Part partDef = (Part) faultDef.getMessage().getParts().values().iterator().next();

                    QName faultName = new QName(targetNamespace, faultDef.getName());
                    QName faultType = faultDef.getMessage().getQName();

                    // create the ODE Message now that we know the fault
                    Message response = odeMex.createMessage(faultType);

                    // build the element to be sent back
                    Element partElement = httpMethodConverter.createPartElement(partDef, bodyElement);
                    response.setPart(partDef.getName(), partElement);

                    // extract and set headers
                    httpMethodConverter.extractHttpResponseHeaders(response, method, faultDef.getMessage(), opBinding.getBindingOutput());

                    // finally send the fault. We did it!
                    if (log.isWarnEnabled())
                        log.warn("[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Fault response: faultName=" + faultName + " faultType=" + faultType + "\n" + DOMUtils.domToString(response.getMessage()));
                    odeMex.replyWithFault(faultName, response);
                }

            }
        }

        private void _4xx_badRequest() throws IOException {
            replyWithFailure("HTTP Status-Line: " + method.getStatusLine() + " for " + method.getURI());
        }

        private void _3xx_redirection() throws IOException {
            // redirections should be handled transparently by http-client
            replyWithFailure("Redirections disabled! HTTP Status-Line: " + method.getStatusLine() + " for " + method.getURI());
        }

        private void _2xx_success() throws IOException {
            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
            if (log.isDebugEnabled())
                log.debug("[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] HTTP Status-Line: " + method.getStatusLine() + " for " + method.getURI());
            if (log.isDebugEnabled()) log.debug("Received response for MEX " + odeMex);

            Operation opDef = odeMex.getOperation();
            BindingOperation opBinding = portBinding.getBindingOperation(opDef.getName(), opDef.getInput().getName(), opDef.getOutput().getName());

            javax.wsdl.Message outputMessage = odeMex.getOperation().getOutput().getMessage();

            // this is the message to populate and send to ODE
            Message odeResponse = odeMex.createMessage(outputMessage.getQName());

            /* process headers */
            httpMethodConverter.extractHttpResponseHeaders(odeResponse, method, outputMessage, opBinding.getBindingOutput());

            /* process the body if any */

            // assumption is made that a response may have at most one body. HttpBindingValidator checks this.
            MIMEContent outputContent = WsdlUtils.getMimeContent(opBinding.getBindingOutput().getExtensibilityElements());
            int statusCode = method.getStatusCode();

            boolean xmlExpected = outputContent != null && HttpHelper.isXml(outputContent.getType());
            // '202/Accepted' and '204/No Content' status codes explicitly state that there is no body, so we should not fail even if a part is bound to the body response 
            boolean isBodyExpected = outputContent != null;
            boolean isBodyMandatory = isBodyExpected && statusCode!=204 && statusCode!=202;
            final String body;
            try {
                body = method.getResponseBodyAsString();
            } catch (IOException e) {
                String errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Unable to get the request body : " + e.getMessage();
                log.error(errmsg, e);
                odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, HttpHelper.prepareDetailsElement(method));
                return;
            }

            final boolean emptyBody = StringUtils.isEmpty(body);
            if (emptyBody) {
                if (isBodyMandatory) {
                    replyWithFailure("Response body is mandatory but missing! Msg Id=" + odeMex.getMessageExchangeId());
                    return;
                }
            } else {
                if (isBodyExpected) {
                    Part partDef = outputMessage.getPart(outputContent.getPart());
                    Element partElement;

                    if (xmlExpected) {

                        Header h = method.getResponseHeader("Content-Type");
                        String receivedType = h != null ? h.getValue() : null;
                        boolean contentTypeSet = receivedType != null;
                        boolean xmlReceived = contentTypeSet && HttpHelper.isXml(receivedType);

                        // a few checks
                        if (!contentTypeSet) {
                            if (log.isDebugEnabled())
                                log.debug("Received Response with a body but no 'Content-Type' header!");
                        } else if (!xmlReceived) {
                            if (log.isDebugEnabled())
                                log.debug("Xml type was expected but non-xml type received! Expected Content-Type=" + outputContent.getType() + " Received Content-Type=" + receivedType);
                        }

                        // parse the body and create the message part
                        try {
                            Element bodyElement = DOMUtils.stringToDOM(body);
                            partElement = httpMethodConverter.createPartElement(partDef, bodyElement);
                        } catch (Exception e) {
                            String errmsg = "[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] Unable to parse the response body: " + e.getMessage();
                            log.error(errmsg, e);
                            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, HttpHelper.prepareDetailsElement(method));
                            return;
                        }
                    } else {
                        // if not xml, process it as text
                        partElement = httpMethodConverter.createPartElement(partDef, body);
                    }

                    // set the part
                    odeResponse.setPart(partDef.getName(), partElement);

                } else {
                    // the body was not expected but we don't know how to deal with it
                    if (log.isDebugEnabled()) log.debug("Body received but not mapped to any part! Body=\n" + body);
                }
            }

            // finally send the message
            try {
                if (log.isInfoEnabled())
                    log.info("Response: " + (odeResponse.getMessage() != null ? DOMUtils.domToString(odeResponse.getMessage()) : "empty"));
                odeMex.reply(odeResponse);
            } catch (Exception ex) {
                replyWithFailure("Unable to process response: " + ex.getMessage());
            }
        }

        void replyWithFailure(String errmsg) {
            log.error("[Service: "+serviceName+", Port: "+portName+", Operation: "+operation.getName()+"] "+errmsg);
            PartnerRoleMessageExchange odeMex = (PartnerRoleMessageExchange) server.getEngine().getMessageExchange(mexId);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, HttpHelper.prepareDetailsElement(method));
        }
    }
}
