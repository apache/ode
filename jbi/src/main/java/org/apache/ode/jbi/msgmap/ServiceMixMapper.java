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

package org.apache.ode.jbi.msgmap;

import java.util.Collection;
import java.util.Set;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Message mapper for dealing with the degenerate messages that servicemix components such as servicemix-http provide. These
 * messages are not normalized and hence do not conform to the JBI specification. They are in fact whatever the SOAP body element
 * happens to be. This mapper will make a reasonable attempt to handle these messages, which means don't count on it working.
 *
 */
public class ServiceMixMapper extends BaseXmlMapper implements Mapper {

    @SuppressWarnings("unchecked")
    public Recognized isRecognized(NormalizedMessage nmsMsg, Operation op) {
        // First of all, if we are not in ServiceMix, we exclude this
        // as a possibility.
        if (nmsMsg.getClass().getName().indexOf("servicemix") == -1) {
            __log.debug("Unrecognized message class: " + nmsMsg.getClass());
            return Recognized.FALSE;
        }

        Element msg;
        try {
            msg = parse(nmsMsg.getContent());
            if (__log.isDebugEnabled()) {
                __log.debug("isRecognized() message: " + prettyPrint(msg));
            }
        } catch (MessageTranslationException e) {
            __log.warn("Unable to parse message: ", e);
            return Recognized.FALSE;
        }

        if (op.getInput() == null) {
            __log.debug("no input def - unrecognized");
            return Recognized.FALSE;
        }

        if (op.getInput().getMessage() == null) {
            __log.debug("no message def - unrecognized");
            return Recognized.FALSE;
        }

        if (op.getInput().getMessage().getParts().size() == 0) {
            __log.debug("no message parts def - unsure");
            return Recognized.UNSURE;
        }

        // servicemix-http has a (bad) habit of placing the SOAP body content directly in the normalized message.
        // We need to recognize it
    	__log.debug("Recognizing document content");
        if (op.getInput().getMessage().getParts().size() == 1) {
            Part part = (Part) op.getInput().getMessage().getParts().values().iterator().next();
            QName elementName = part.getElementName();
            if (elementName != null && elementName.getLocalPart().equals(msg.getLocalName())
                    && elementName.getNamespaceURI().equals(msg.getNamespaceURI())) {
            	__log.debug("Recognized");
                return Recognized.TRUE;
            }
        }

        // Recognize RPC style message
    	__log.debug("Recognizing RPC style content");
        for (String pname : ((Set<String>) op.getInput().getMessage().getParts().keySet())) {
            Part part = op.getInput().getMessage().getPart(pname);
            
            if (part.getElementName() != null) {
                //RPC style invocation doesn't allow element parts, so we don't accept it
            	__log.debug("Part " + part.getName() + " has element content " + part.getElementName() + ". It's not allowed for RPC style.");
            	return Recognized.FALSE;
            }
            
            // with RPC semantic the body is wrapped by a partName which is same as bodyElementName
            Element pdata = DOMUtils.findChildByName(msg, new QName(null, part.getName()));
            if (pdata == null) {
                __log.debug("no part data for " + part.getName() + " -- unrecognized.");
                return Recognized.FALSE;
            }
        }

        return Recognized.TRUE;
    }

    public void toNMS(NormalizedMessage nmsMsg, Message odeMsg, javax.wsdl.Message msgdef, QName fault) throws MessagingException,
            MessageTranslationException {
        if (msgdef == null)
            throw new NullPointerException("msdef must not be null.");
        Element ode = odeMsg == null ? null : odeMsg.getMessage();
        Element part = ode == null ? null : DOMUtils.getFirstChildElement(ode);
        Element firstPartEl = part == null ? null : DOMUtils.getFirstChildElement(part);

        if (fault != null) {
            // We treat faults seperately as there are some assumption we can make, mainly that there is
            // a single part and it is an element part.

            if (msgdef.getParts().size() != 1)
                throw new MessageTranslationException("Message for fault \"" + fault + "\" does not contain exactly one part! Cannot map!");

            Part partDef = (Part) msgdef.getParts().values().iterator().next();
            if (partDef.getElementName() == null)
                throw new MessageTranslationException("Message for fault \"" + fault + "\" does not contain an element part.");

            if (firstPartEl == null) {
                // Oooops, our assumption did not pan out; we'll do our best i.e. create empty content.
                __log.warn("Proceessing fault \"" + fault + "\" with empty content (check your BPEL).");

                Document doc = newDocument();
                Element content = doc.createElementNS(partDef.getElementName().getNamespaceURI(), partDef.getElementName().getLocalPart());
                doc.appendChild(content);
                if (__log.isDebugEnabled())
                    __log.debug("toNMS() ode message (fault, BS): " + prettyPrint(content));
                nmsMsg.setContent(new DOMSource(doc));
            } else {
                if (__log.isDebugEnabled())
                    __log.debug("toNMS() ode message (fault): " + prettyPrint(firstPartEl));
                nmsMsg.setContent(new DOMSource(firstPartEl));
            }
            return;
        }

        if (msgdef.getParts().size() == 0) {
            if (__log.isDebugEnabled())
                __log.debug("toNMS() ode message (rpc-like): no parts");
            nmsMsg.setContent(null);
            return;
        } else if (msgdef.getParts().size() != 1 || ((Part) msgdef.getParts().values().iterator().next()).getElementName() == null) {
            // If we have more than one part, or a single non-element part, then we can't use the standard
            // NMS doc-lit like convention. Instead we place the entire message on the bus and hope for the
            // best.
            if (__log.isDebugEnabled())
                __log.debug("toNMS() ode message (rpc-like): " + prettyPrint(ode));
            nmsMsg.setContent(new DOMSource(ode));
            return;
        }

        if (__log.isDebugEnabled())
            __log.debug("toNMS() normalized message (doc-like):" + prettyPrint(firstPartEl));
        nmsMsg.setContent(new DOMSource(firstPartEl));
    }

    public void toODE(Message odeMsg, NormalizedMessage nmsMsg, javax.wsdl.Message msgdef) throws MessageTranslationException {
        Element nms;
        if (nmsMsg.getContent() != null) {
            nms = parse(nmsMsg.getContent());
        } else {
            Document doc = newDocument();
            Element message = doc.createElement("message");
            odeMsg.setMessage(message);
            if (__log.isDebugEnabled()) {
                __log.debug("toODE() normalized message:\n" + prettyPrint(message));
            }
            return;
        }
        
        if (__log.isDebugEnabled()) {
            __log.debug("toODE() normalized message:\n" + prettyPrint(nms));
        }

        boolean docLit = false;

        for (String pname : ((Set<String>) msgdef.getParts().keySet())) {
            Part part = msgdef.getPart(pname);
            // servicemix-http has a (bad) habit of placing the SOAP body content directly in the normalized message
            QName elementName = part.getElementName();
            if (elementName != null && elementName.getLocalPart().equals(nms.getLocalName())
                    && elementName.getNamespaceURI().equals(nms.getNamespaceURI())) {
                docLit = true;
                break;
            }
        }
        if (docLit) {
            __log.debug("toODE() doc-like message ");

            Document doc = newDocument();
            Element message = doc.createElement("message");
            doc.appendChild(message);

            Part firstPart = (Part) msgdef.getOrderedParts(null).get(0);
            Element p = doc.createElement(firstPart.getName());
            message.appendChild(p);
            p.appendChild(doc.importNode(nms, true));
            odeMsg.setMessage(message);
        } else {
            __log.debug("toODE() rpc-like message ");
            // Simple, just pass along the message
            if (__log.isDebugEnabled()) {
                __log.debug("toODE() ode message:\n" + prettyPrint(nms));
            }
            odeMsg.setMessage(nms);
        }
    }

    public Fault toFaultType(javax.jbi.messaging.Fault jbiFlt, Collection<Fault> faults) throws MessageTranslationException {
        if (faults.isEmpty())
            return null;

        // anynone's guess really
        return faults.iterator().next();
    }

    private String prettyPrint(Element el) {
        try {
            return DOMUtils.prettyPrint(el);
        } catch (java.io.IOException ioe) {
            return ioe.getMessage();
        }
    }

}
