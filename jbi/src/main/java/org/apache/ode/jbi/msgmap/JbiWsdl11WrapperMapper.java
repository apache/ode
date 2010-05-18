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
import java.util.LinkedList;
import java.util.List;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Mapper for converting ODE messages to NMS messages using the WSDL 11 wrapper
 * format.
 */
public class JbiWsdl11WrapperMapper extends BaseXmlMapper implements Mapper {

    public static final String URI_WSDL11_WRAPPER = "http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper";

    public static final QName WSDL11_W_MESSAGE = new QName(URI_WSDL11_WRAPPER, "message");

    public JbiWsdl11WrapperMapper() {
    }

    public Recognized isRecognized(NormalizedMessage nmsMsg, Operation op) {
        Element srcel;
        try {
            srcel = parse(nmsMsg.getContent());
        } catch (MessageTranslationException e) {
            // Well, maybe it is not XML.
            if (__log.isDebugEnabled())
                __log.debug("Exception parsing NMS message.", e);
            return Recognized.FALSE;
        }

        QName srcName = new QName(srcel.getNamespaceURI(), srcel.getLocalName());
        return WSDL11_W_MESSAGE.equals(srcName) ? Recognized.TRUE : Recognized.FALSE;
    }

    /**
     * 
     * Convert ODE normalized message to JBI normalized "WSDL 1.1 Wrapper"
     * format.
     */
    public void toNMS(NormalizedMessage nmsMsg, Message odeMsg, javax.wsdl.Message msgdef, QName fault) throws MessagingException {
        if (msgdef == null)
            throw new NullPointerException("Null MessageDef");
        if (odeMsg == null)
            throw new NullPointerException("Null src.");

        if (__log.isTraceEnabled())
            __log.trace("toNMS(odeMsg=" + odeMsg + ")");

        Element srcMsgEl = odeMsg.getMessage();
        Document doc = newDocument();
        Element dstMsgEl = doc.createElementNS(URI_WSDL11_WRAPPER, "message");
        doc.appendChild(dstMsgEl);

        // The JBI NMS required attributes.
        dstMsgEl.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:msgns", odeMsg.getType().getNamespaceURI());
        dstMsgEl.setAttribute("version", "1.0");
        dstMsgEl.setAttribute("type", "msgns:" + odeMsg.getType().getLocalPart());

        // The parts (hopefully they are in order, as NMS does not identify
        // them!)
        Element srcPartEl = DOMUtils.getFirstChildElement(srcMsgEl);
        while (srcPartEl != null) {
            Element dstPartEl = doc.createElementNS(URI_WSDL11_WRAPPER, "part");
            dstMsgEl.appendChild(dstPartEl);
            Node srccontent = srcPartEl.getFirstChild();
            while (srccontent != null) {
                dstPartEl.appendChild(doc.importNode(srccontent, true));
                srccontent = srccontent.getNextSibling();
            }
            srcPartEl = DOMUtils.getNextSiblingElement(srcPartEl);
        }

        nmsMsg.setContent(new DOMSource(doc));

    }

    @SuppressWarnings("unchecked")
    public void toODE(Message dest, NormalizedMessage src, javax.wsdl.Message msgdef)
            throws MessageTranslationException {
        if (msgdef == null)
            throw new NullPointerException("Null MessageDef");
        if (dest == null)
            throw new NullPointerException("Null dest.");
        if (src == null)
            throw new NullPointerException("Null src.");

        if (__log.isTraceEnabled())
            __log.trace("convertMessage<toODE>(dest=" + dest + ",src=" + src);

        Element srcel = parse(src.getContent());

        Document odemsgdoc = newDocument();
        Element odemsg = odemsgdoc.createElement("message");
        odemsgdoc.appendChild(odemsg);

        List<Part> expectedParts = msgdef.getOrderedParts(null);

        Element srcpart = DOMUtils.getFirstChildElement(srcel);
        for (int i = 0; i < expectedParts.size(); ++i) {
            Part pdef = expectedParts.get(i);
            Element p = odemsgdoc.createElement(pdef.getName());
            odemsg.appendChild(p);
            if (srcpart != null) {
                NodeList nl = srcpart.getChildNodes();
                for (int j = 0; j < nl.getLength(); ++j)
                    p.appendChild(odemsgdoc.importNode(nl.item(j), true));
                srcpart = DOMUtils.getNextSiblingElement(srcpart);
            } else {
                __log.error("Improperly formatted message, missing part: " + pdef.getName());
            }
        }

        dest.setMessage(odemsg);

    }

    @SuppressWarnings("unchecked")
    public Fault toFaultType(javax.jbi.messaging.Fault jbiFlt, Collection<Fault> faults) throws MessageTranslationException {
        if (jbiFlt == null)
            throw new NullPointerException("Null jbiFlt.");
        if (faults == null)
            throw new NullPointerException("Null faults.");

        if (__log.isTraceEnabled())
            __log.trace("toFaultType(jbiFlt=" + jbiFlt + ")");

        
        final QName partElName = new QName(URI_WSDL11_WRAPPER, "part");
        List<QName> eltypes = new LinkedList<QName>();
        
        // Figure out what we have in the message we just got.
        Element srcel = parse(jbiFlt.getContent()); 
        Node n = srcel.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                QName elName = new QName(n.getNamespaceURI(),n.getLocalName());
                if (!elName.equals(partElName)) {
                    String err = "Invalid NMS message format, expected " + partElName + " but found " + elName;
                    __log.error(err);
                    throw new MessageTranslationException(err);
                }
                Element pdata = DOMUtils.getFirstChildElement((Element) n);
                if (pdata == null)
                    eltypes.add(null);
                else
                    eltypes.add(new QName(pdata.getNamespaceURI(),pdata.getLocalName()));
            }
            n = n.getNextSibling();
        }

        // See if it matches what we expect the faults to look like (first one wins!)
        fltiter:for (Fault f : faults) {
            if (f.getMessage() == null && eltypes.isEmpty())
                return f;
            
            if (f.getMessage().getParts().size() != eltypes.size())
                continue;
            
            List<Part> expectedParts = f.getMessage().getOrderedParts(null);

            int i = 0;
            for (Part p : expectedParts) {
                if (eltypes.size() <= i)
                    continue fltiter;
                QName etype = eltypes.get(i++);
                
                if ((p.getElementName() == null) ^ (etype == null))
                    continue fltiter;
                
                if (etype == null && p.getElementName() == null)
                    continue;

                if (!etype.equals(p.getElementName()))
                    continue fltiter;
            }
            
            return f;
            
        }
        
        // None of the faults has been recognized. 
        return null;
    }

}
