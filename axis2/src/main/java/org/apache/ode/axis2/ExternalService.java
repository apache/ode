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

package org.apache.ode.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.util.OMUtils;
import org.apache.ode.axis2.util.SOAPUtils;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Acts as a service not provided by ODE. Used mainly for invocation as a way to
 * maintain the WSDL decription of used services.
 */
public class ExternalService implements PartnerRoleChannel {

    private static final Log __log = LogFactory.getLog(ExternalService.class);

    private ExecutorService _executorService;

    private Definition _definition;
    private QName _serviceName;
    private String _portName;
    private AxisConfiguration _axisConfig;

    public ExternalService(Definition definition, QName serviceName,
                           String portName, ExecutorService executorService, AxisConfiguration axisConfig) {
        _definition = definition;
        _serviceName = serviceName;
        _portName = portName;
        _executorService = executorService;
        _axisConfig = axisConfig;
    }

    public void invoke(final PartnerRoleMessageExchange odeMex) {
        boolean isTwoWay = odeMex.getMessageExchangePattern() ==
                org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
        try {
            Element msgContent = SOAPUtils.wrap(odeMex.getRequest().getMessage(), _definition, _serviceName,
                    odeMex.getOperation(), odeMex.getOperation().getInput().getMessage());

            final OMElement payload = OMUtils.toOM(msgContent);

            Options options = new Options();
            EndpointReference axisEPR = new EndpointReference(((MutableEndpoint)odeMex.getEndpointReference()).getUrl());
            __log.debug("Axis2 sending message to " + axisEPR.getAddress() + " using MEX " + odeMex);
            __log.debug("Message: " + payload);
            options.setTo(axisEPR);

            ConfigurationContext ctx = new ConfigurationContext(_axisConfig);
            final ServiceClient serviceClient = new ServiceClient(ctx, null);
            serviceClient.setOptions(options);
            // Override options are passed to the axis MessageContext so we can
            // retrieve them in our session out handler.
            Options mexOptions = new Options();
            writeHeader(mexOptions, odeMex);
            serviceClient.setOverrideOptions(mexOptions);

            if (isTwoWay) {
                // Invoking in a separate thread even though we're supposed to wait for a synchronous reply
                // to force clear transaction separation.
                Future<OMElement> freply = _executorService.submit(new Callable<OMElement>() {
                    public OMElement call() throws Exception {
                        return serviceClient.sendReceive(payload);
                    }
                });
                OMElement reply = null;
                try {
                    reply = freply.get();
                } catch (Exception e) {
                    __log.error("We've been interrupted while waiting for reply to MEX " + odeMex + "!!!");
                    String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                    odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
                }

                final Message response = odeMex.createMessage(odeMex.getOperation().getOutput().getMessage().getQName());
                Element responseElmt = OMUtils.toDOM(reply);
                responseElmt = SOAPUtils.unwrap(responseElmt, _definition,
                        odeMex.getOperation().getOutput().getMessage(), _serviceName);
                __log.debug("Received synchronous response for MEX " + odeMex);
                __log.debug("Message: " + DOMUtils.domToString(responseElmt));
                response.setMessage(responseElmt);
                odeMex.reply(response);
            } else
                serviceClient.fireAndForget(payload);
        } catch (AxisFault axisFault) {
            String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
            __log.error(errmsg, axisFault);
            odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
        }

    }

    /**
     * Extracts endpoint information from ODE message exchange to stuff them into
     * Axis MessageContext.
     */
    private void writeHeader(Options options, PartnerRoleMessageExchange odeMex) {
        WSAEndpoint targetEPR  = EndpointFactory.convertToWSA((MutableEndpoint) odeMex.getEndpointReference());
        WSAEndpoint myRoleEPR = EndpointFactory.convertToWSA((MutableEndpoint) odeMex.getMyRoleEndpointReference());
        
        String partnerSessionId = odeMex.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID);
        String myRoleSessionId = odeMex.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID);

        if (partnerSessionId != null) {
            __log.debug("Partner session identifier found for WSA endpoint: " + partnerSessionId);
            targetEPR.setSessionId(partnerSessionId);
        }
        options.setProperty("targetSessionEndpoint", targetEPR);
        String soapAction = SOAPUtils.getSoapAction(_definition, _serviceName, _portName,
                odeMex.getOperationName());
        options.setProperty("soapAction", soapAction);

        if (myRoleEPR != null)  {
            if  (myRoleSessionId != null) {
                __log.debug("MyRole session identifier found for myrole (callback) WSA endpoint: " + myRoleSessionId);
                myRoleEPR.setSessionId(myRoleSessionId);
            }

            options.setProperty("callbackSessionEndpoint", odeMex.getMyRoleEndpointReference());
        } else {
            __log.debug("My-Role EPR not specified, SEP will not be used.");
        }
    }

    public org.apache.ode.bpel.iapi.EndpointReference getInitialEndpointReference() {
        Element eprElmt = ODEService.genEPRfromWSDL(_definition, _serviceName, _portName);
        if (eprElmt == null)
            throw new IllegalArgumentException("Service " + _serviceName + " and port " + _portName + 
                "couldn't be found in provided WSDL document!");
        return EndpointFactory.convertToWSA(ODEService.createServiceRef(eprElmt));
    }

    public void close() {
        // TODO Auto-generated method stub
    }
}
