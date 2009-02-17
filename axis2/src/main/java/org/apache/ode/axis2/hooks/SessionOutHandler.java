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

package org.apache.ode.axis2.hooks;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.il.epr.EndpointFactory;
import org.apache.ode.il.epr.MutableEndpoint;
import org.apache.ode.il.epr.WSAEndpoint;
import org.apache.ode.axis2.ODEService;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.utils.Namespaces;

/**
 * An outgoing handler adding session id information in the message
 * context.
 */
public class SessionOutHandler extends AbstractHandler {

    private static final long serialVersionUID = 1L;
    private static final Log __log = LogFactory.getLog(SessionOutHandler.class);


    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        EndpointReference otargetSession = (EndpointReference) messageContext.getProperty(ODEService.TARGET_SESSION_ENDPOINT);
        EndpointReference ocallbackSession = (EndpointReference) messageContext.getProperty(ODEService.CALLBACK_SESSION_ENDPOINT);
        if (otargetSession == null)
            otargetSession = (EndpointReference) messageContext.getOptions().getProperty(ODEService.TARGET_SESSION_ENDPOINT);
        if (ocallbackSession == null)
            ocallbackSession = (EndpointReference) messageContext.getOptions().getProperty(ODEService.CALLBACK_SESSION_ENDPOINT);

        if (otargetSession != null || ocallbackSession != null) {
            SOAPHeader header = messageContext.getEnvelope().getHeader();
            SOAPFactory factory = (SOAPFactory) messageContext.getEnvelope().getOMFactory();
            OMNamespace odeSessNS = factory.createOMNamespace(Namespaces.ODE_SESSION_NS, "odesession");
            OMNamespace wsAddrNS = factory.createOMNamespace(Namespaces.WS_ADDRESSING_NS, "addr");
            if (header == null) {
                header = factory.createSOAPHeader(messageContext.getEnvelope());
            }
            
            if (otargetSession != null && otargetSession instanceof MutableEndpoint) {

                WSAEndpoint targetEpr = EndpointFactory.convertToWSA((MutableEndpoint) otargetSession);
                
                OMElement to = factory.createSOAPHeaderBlock("To", wsAddrNS);
                header.addChild(to);
                to.setText(targetEpr.getUrl());

                String action = messageContext.getSoapAction(); 
                OMElement wsaAction = factory.createSOAPHeaderBlock("Action", wsAddrNS);
                header.addChild(wsaAction);
                wsaAction.setText(action);

                // we only set the ReplyTo and MessageID headers if doing Request-Response
                org.apache.axis2.addressing.EndpointReference replyToEpr = messageContext.getReplyTo();
                if (replyToEpr != null) {
                    OMElement replyTo = factory.createSOAPHeaderBlock("ReplyTo", wsAddrNS);
                    OMElement address = factory.createOMElement("Address", wsAddrNS);
                    replyTo.addChild(address);
                    header.addChild(replyTo);
                    address.setText(replyToEpr.getAddress());
                    
                    String messageId = messageContext.getMessageID();
                    OMElement messageIdElem = factory.createSOAPHeaderBlock("MessageID", wsAddrNS);
                    header.addChild(messageIdElem);
                    messageIdElem.setText(messageId);                
                }
                
                if (targetEpr.getSessionId() != null) {
                    OMElement session = factory.createSOAPHeaderBlock("session", odeSessNS);
                    header.addChild(session);
                    session.setText(targetEpr.getSessionId());
                }
                __log.debug("Sending stateful TO epr in message header using session " + targetEpr.getSessionId());
            }

            if (ocallbackSession != null && ocallbackSession instanceof MutableEndpoint) {
                WSAEndpoint callbackEpr = EndpointFactory.convertToWSA((MutableEndpoint) ocallbackSession);
                OMElement callback = factory.createSOAPHeaderBlock("callback", odeSessNS);
                header.addChild(callback);
                OMElement address = factory.createOMElement("Address", wsAddrNS);
                callback.addChild(address);
                address.setText(callbackEpr.getUrl());
                if (callbackEpr.getSessionId() != null) {
                    OMElement session = factory.createOMElement("session", odeSessNS);
                    session.setText(callbackEpr.getSessionId());
                    callback.addChild(session);
                }
                __log.debug("Sending stateful FROM epr in message header using session " + callbackEpr.getSessionId());
            }

            __log.debug("Sending a message containing wsa endpoints in headers for session passing.");
            __log.debug(messageContext.getEnvelope().toString());

        }
        return InvocationResponse.CONTINUE;        
    }
}
