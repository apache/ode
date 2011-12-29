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
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * An incoming handler adding session id information in the message
 * context.
 */
public class SessionInHandler extends AbstractHandler {
    private static final long serialVersionUID = -806564877582696569L;

    private static final Log __log = LogFactory.getLog(SessionInHandler.class);

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        SOAPHeader header = messageContext.getEnvelope().getHeader();
        if (header != null) {
            if (__log.isDebugEnabled())
                __log.debug("Found a header in incoming message, checking if there are endpoints there.");
            // Checking if a session identifier has been provided for a stateful endpoint
            OMElement wsaToSession = header.getFirstChildWithName(new QName(Namespaces.INTALIO_SESSION_NS, "session"));
            if (wsaToSession != null) {
                // Building an endpoint supposed to target the right instance
                Document doc = DOMUtils.newDocument();
                Element serviceEpr = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
                Element sessionId = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
                doc.appendChild(serviceEpr);
                serviceEpr.appendChild(sessionId);
                sessionId.setTextContent(wsaToSession.getText());
                if (__log.isDebugEnabled())
                    __log.debug("A TO endpoint has been found in the header with session: " + wsaToSession.getText());

                // Did the client provide an address too?
                OMElement wsaToAddress = header.getFirstChildWithName(new QName(Namespaces.WS_ADDRESSING_NS, "To"));
                if (wsaToAddress != null) {
                    Element addressElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
                    addressElmt.setTextContent(wsaToAddress.getText());
                    serviceEpr.appendChild(addressElmt);
                }
                if (__log.isDebugEnabled())
                    __log.debug("Constructed a TO endpoint: " + DOMUtils.domToString(serviceEpr));
                messageContext.setProperty("targetSessionEndpoint", serviceEpr);
            }

            // Seeing if there's a callback, in case our client would be stateful as well
            OMElement callback = header.getFirstChildWithName(new QName(Namespaces.INTALIO_SESSION_NS, "callback"));
            if (callback != null) {
                OMElement callbackSession = callback.getFirstChildWithName(new QName(Namespaces.ODE_SESSION_NS, "session"));
                if(callbackSession==null){
                    callbackSession = callback.getFirstChildWithName(new QName(Namespaces.INTALIO_SESSION_NS, "session"));
                }
                if (callbackSession != null) {
                    // Building an endpoint that represents our client (we're supposed to call him later on)
                    Document doc = DOMUtils.newDocument();
                    Element serviceEpr = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
                    Element sessionId = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
                    doc.appendChild(serviceEpr);
                    serviceEpr.appendChild(sessionId);
                    sessionId.setTextContent(callbackSession.getText());
                    if (__log.isDebugEnabled())
                        __log.debug("A CALLBACK endpoint has been found in the header with session: " + callbackSession.getText());

                    // Did the client give his address as well?
                    OMElement wsaToAddress = callback.getFirstChildWithName(new QName(Namespaces.WS_ADDRESSING_NS, "Address"));
                    if (wsaToAddress != null) {
                        Element addressElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
                        addressElmt.setTextContent(wsaToAddress.getText());
                        serviceEpr.appendChild(addressElmt);
                    }
                    if (__log.isDebugEnabled())
                        __log.debug("Constructed a CALLBACK endpoint: " + DOMUtils.domToString(serviceEpr));
                    messageContext.setProperty("callbackSessionEndpoint", serviceEpr);
                }
            }
        }
        return InvocationResponse.CONTINUE;
    }

}
