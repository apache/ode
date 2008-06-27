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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import java.io.InputStream;
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
    
    protected HttpClientHelper helper;
    protected HttpMethodConverter httpMethodConverter;

    protected Binding portBinding;
    
    public HttpExternalService(ProcessConf pconf, QName serviceName, String portName, ExecutorService executorService, Scheduler scheduler, BpelServer server) {
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
        helper = new HttpClientHelper();
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
            helper.configure(client.getHostConfiguration(), client.getState(), method.getURI(), params);

            // this callable encapsulates the http method execution and the process of the response
            final Callable executionCallable;

            // execute it
            boolean isTwoWay = odeMex.getMessageExchangePattern() == MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
            if (isTwoWay) {
                // two way
                executionCallable = new HttpExternalService.TwoWayCallable(client, method, odeMex);
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
                executionCallable = new HttpExternalService.OneWayCallable(client, method, odeMex);
                executorService.submit(executionCallable);
                odeMex.replyOneWayOk();
            }
        } catch (UnsupportedEncodingException e) {
            String errmsg = "The HTTP encoding returned isn't supported " + odeMex;
            log.error(errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
        } catch (URIException e) {
            String errmsg = "Error sending message to " + getClass().getSimpleName() + " for ODE mex " + odeMex;
            log.error(errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
        } catch (Exception e) {
            String errmsg = "Unknown HTTP call error for ODE mex " + odeMex;
            log.error(errmsg, e);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, null);
        }
    }

    private class OneWayCallable implements Callable<Void> {
        HttpMethod method;
        PartnerRoleMessageExchange odeMex;
        HttpClient client;

        public OneWayCallable(HttpClient client, HttpMethod method, PartnerRoleMessageExchange odeMex) {
            this.method = method;
            this.odeMex = odeMex;
            this.client = client;
        }

        public Void call() throws Exception {
            try {
                // simply execute the http method
                if (log.isDebugEnabled())
                    log.debug("Executing http request : " + method.getName() + " " + method.getURI());

                final int statusCode = client.executeMethod(method);
                // invoke getResponseBody to force the loading of the body
                // Actually the processResponse may happen in a separate thread and
                // as a result the connection might be closed before the body processing (see the finally clause below).
                byte[] responseBody = method.getResponseBody();
                // ... and process the response
                processResponse(statusCode);
            } catch (final IOException e) {
                // ODE MEX needs to be invoked in a TX.
                try {
                    scheduler.execIsolatedTransaction(new Callable<Void>() {
                        public Void call() throws Exception {
                            String errmsg = "Unable to execute http request : " + e.getMessage();
                            log.error(errmsg, e);
                            odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
                            return null;
                        }
                    });
                } catch (Exception e1) {
                    String errmsg = "Error executing reply transaction; reply will be lost.";
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
                    if (log.isWarnEnabled())
                        log.warn("OneWay http request [" + method.getURI() + "] failed with status: " + method.getStatusLine());
                } else {
                    if (log.isDebugEnabled())
                        log.debug("OneWay http request [" + method.getURI() + "] status: " + method.getStatusLine());
                }
            } catch (URIException e) {
                if (log.isDebugEnabled()) log.debug(e);
            }
        }
    }

    private class TwoWayCallable extends OneWayCallable {
        public TwoWayCallable(org.apache.commons.httpclient.HttpClient client, HttpMethod method, PartnerRoleMessageExchange odeMex) {
            super(client, method, odeMex);
        }

        public void processResponse(final int statusCode) {
            try {
                // ODE MEX needs to be invoked in a TX.
                scheduler.execIsolatedTransaction(new Callable<Void>() {
                    public Void call() throws Exception {

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

                        return null;
                    }
                });
            } catch (Exception e) {
                String errmsg = "Error executing reply transaction; reply will be lost.";
                log.error(errmsg, e);
            }
        }

        private void unmanagedStatus() throws IOException {
            String errmsg = "Unmanaged Status Code! " + method.getStatusLine();
            log.error(errmsg);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
        }

        /**
         * For 500s if a fault is defined in the WSDL and the response body contains the corresponding xml doc, then reply with a fault ; else reply with failure.
         *
         * @throws IOException
         */
        private void _5xx_serverError() throws IOException {
            String errmsg = "Internal Server Error! " + method.getStatusLine();
            log.error(errmsg);

            Operation opDef = odeMex.getOperation();
            BindingOperation opBinding = portBinding.getBindingOperation(opDef.getName(), opDef.getInput().getName(), opDef.getOutput().getName());
            if (opDef.getFaults().isEmpty()) {
                errmsg = "Operation has no fault. This 500 error will be considered as a failure.";
                if (log.isDebugEnabled()) log.debug(errmsg);
                odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
            } else if (opBinding.getBindingFaults().isEmpty()) {
                errmsg = "No fault binding. This 500 error will be considered as a failure.";
                if (log.isDebugEnabled()) log.debug(errmsg);
                odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
            } else if (method.getResponseBodyAsStream() == null) {
                errmsg = "No body in the response. This 500 error will be considered as a failure.";
                if (log.isDebugEnabled()) log.debug(errmsg);
                odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
            } else {
                final InputStream bodyAsStream = method.getResponseBodyAsStream();
                try {
                    Element bodyEl = DOMUtils.parse(bodyAsStream).getDocumentElement();
                    QName bodyName = new QName(bodyEl.getNamespaceURI(), bodyEl.getNodeName());
                    Fault faultDef = WsdlUtils.inferFault(opDef, bodyName);

                    // is this fault bound with ODE extension?
                    if (!WsdlUtils.isOdeFault(opBinding.getBindingFault(faultDef.getName()))) {
                        errmsg = "Fault " + bodyName + " is not bound with " + new QName(Namespaces.ODE_HTTP_EXTENSION_NS, "fault") + ". This 500 error will be considered as a failure.";
                        if (log.isDebugEnabled()) log.debug(errmsg);
                        odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
                    } else {
                        Part partDef = (Part) opBinding.getOperation().getFault(faultDef.getName()).getMessage().getParts().values().iterator().next();

                        // build the element to be sent back
                        Document odeMsg = DOMUtils.newDocument();
                        Element odeMsgEl = odeMsg.createElementNS(null, "message");
                        Element partEl = odeMsgEl.getOwnerDocument().createElementNS(null, partDef.getName());
                        odeMsgEl.appendChild(partEl);
                        // import the response body
                        partEl.appendChild(odeMsgEl.getOwnerDocument().importNode(bodyEl, true));

                        QName faultType = new QName(targetNamespace, faultDef.getName());
                        Message response = odeMex.createMessage(faultType);
                        response.setMessage(odeMsgEl);
                        // finally send the fault. We did it!
                        odeMex.replyWithFault(faultType, response);
                    }
                } catch (Exception e) {
                    errmsg = "Unable to parse the response body as xml. This 500 error will be considered as a failure.";
                    if (log.isDebugEnabled()) log.debug(errmsg, e);
                    odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method, false));
                }
            }

        }

        private void _4xx_badRequest() throws IOException {
            String errmsg = "Bad Request! " + method.getStatusLine();
            log.error(errmsg);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
        }

        private void _3xx_redirection() throws IOException {
            String errmsg = "Redirections are not supported! " + method.getStatusLine();
            log.error(errmsg);
            odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
        }

        private void _2xx_success() {
            if (log.isDebugEnabled()) log.debug("Http Status Line=" + method.getStatusLine());
            if (log.isDebugEnabled()) log.debug("Received response for MEX " + odeMex);

            Operation opDef = odeMex.getOperation();
            BindingOperation opBinding = portBinding.getBindingOperation(opDef.getName(), opDef.getInput().getName(), opDef.getOutput().getName());

            // assumption is made that a response may have at most one body. HttpBindingValidator checks this.
            MIMEContent outputContent = WsdlUtils.getMimeContent(opBinding.getBindingOutput().getExtensibilityElements());
            boolean isBodyMandatory = outputContent != null && !outputContent.getType().endsWith("text/xml");


            try {
                final InputStream bodyAsStream = method.getResponseBodyAsStream();
                if (isBodyMandatory && bodyAsStream == null) {
                    String errmsg = "Response body is mandatory but missing! Msg Id=" + odeMex.getMessageExchangeId();
                    log.error(errmsg);
                    odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, null);
                } else {
                    Message odeResponse = odeMex.createMessage(odeMex.getOperation().getOutput().getMessage().getQName());

                    // handle the body if any
                    if (bodyAsStream != null) {
                        // only text/xml is supported in the response body
                        try {
                            Element bodyElement = DOMUtils.parse(bodyAsStream).getDocumentElement();
                            // we expect a single part per output message
                            // see org.apache.ode.axis2.httpbinding.HttpBindingValidator call in constructor
                            Part part = (Part) odeMex.getOperation().getOutput().getMessage().getParts().values().iterator().next();
                            Element partElement = processBodyElement(part, bodyElement);
                            odeResponse.setPart(part.getName(), partElement);
                        } catch (Exception e) {
                            String errmsg = "Unable to parse the response body: " + e.getMessage();
                            log.error(errmsg, e);
                            odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
                            return;
                        }
                    }
                    try {

                        if (log.isInfoEnabled())
                            log.info("Response:\n" + DOMUtils.domToString(odeResponse.getMessage()));
                        odeMex.reply(odeResponse);
                    } catch (Exception ex) {
                        String errmsg = "Unable to process response: " + ex.getMessage();
                        log.error(errmsg, ex);
                        odeMex.replyWithFailure(MessageExchange.FailureType.OTHER, errmsg, helper.prepareDetailsElement(method));
                    }
                }
            } catch (IOException e) {
                String errmsg = "Unable to get the request body : " + e.getMessage();
                log.error(errmsg, e);
                odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, errmsg, null);
                return;
            }
        }

        /**
         * Create the element to be inserted into {@link Message}.
         * If the part has a non-null element name, the bodyElement is simply appended.
         * Else if the bodyElement has a text content, the value is set to the message.
         * Else append all nodes of bodyElement to the returned element. Attributes are ignored.
         * <p/>
         * The name of the returned element is the part name.
         *
         * @param part
         * @param bodyElement
         * @return the element to insert "as is" to ODE message
         */
        private Element processBodyElement(Part part, Element bodyElement) {
            Document doc = DOMUtils.newDocument();
            Element partElement = doc.createElementNS(null, part.getName());
            if (part.getElementName() != null) {
                partElement.appendChild(doc.importNode(bodyElement, true));
            } else {
                if (DOMUtils.isEmptyElement(bodyElement)) {
                    // Append an empty text node.
                    // Warning! setting an empty string with setTextContent has not effect. See javadoc.
                    partElement.appendChild(doc.createTextNode(""));
                } else {
                    String textContent = DOMUtils.getTextContent(bodyElement);
                    if (textContent != null) {
                        // this is a simple type
                        partElement.setTextContent(textContent);
                    } else {
                        // this is a complex type, import every child
                        // !!! Attributes are ignored
                        for (int m = 0; m < bodyElement.getChildNodes().getLength(); m++) {
                            Node child = bodyElement.getChildNodes().item(m);
                            partElement.appendChild(doc.importNode(child, true));
                        }
                    }
                }

            }
            return partElement;
        }

    }
}

