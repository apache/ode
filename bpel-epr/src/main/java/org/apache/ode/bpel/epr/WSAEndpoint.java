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

package org.apache.ode.bpel.epr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * A service endpoint represented as a WS-Addressing EndpointReference.
 */
public class WSAEndpoint implements MutableEndpoint {

    private static final Log __log = LogFactory.getLog(WSAEndpoint.class);

    private Element _eprElmt;

    public WSAEndpoint() {
    }

    // prototype constructor
    public WSAEndpoint(WSAEndpoint prototype) {
    	_eprElmt = (Element)DOMUtils.newDocument().importNode(prototype._eprElmt, true);
    }
    
    public WSAEndpoint(Map map) {
        this();
        fromMap(map);
    }

    public String getSessionId() {
        NodeList idNodes = _eprElmt.getElementsByTagNameNS(Namespaces.ODE_SESSION_NS, "session");
        if (idNodes.getLength() > 0) {
            return idNodes.item(0).getTextContent();
        } else {
        	// try the same with the intalio header
            idNodes = _eprElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "session");
            if (idNodes.getLength() > 0) {
                return idNodes.item(0).getTextContent();
            } 
           	return null;
        }
    }

    public void setSessionId(String sessionId) {
        NodeList idList = _eprElmt.getElementsByTagNameNS(Namespaces.ODE_SESSION_NS, "session");
        if (idList.getLength() > 0)
            idList.item(0).setTextContent(sessionId);
        else {
            Element sessElmt = _eprElmt.getOwnerDocument().createElementNS(Namespaces.ODE_SESSION_NS, "session");
            sessElmt.setTextContent(sessionId);
            _eprElmt.appendChild(sessElmt);
        }

    	// and the same for the intalio header
        idList = _eprElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "session");
        if (idList.getLength() > 0)
            idList.item(0).setTextContent(sessionId);
        else {
            Element sessElmt = _eprElmt.getOwnerDocument().createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
            sessElmt.setTextContent(sessionId);
            _eprElmt.appendChild(sessElmt);
        }
    }

    public String getUrl() {
        return _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Address").item(0).getTextContent().trim();
    }

    public void setUrl(String url) {
        NodeList addrList = _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Address");
        if (addrList.getLength() > 0)
            addrList.item(0).setTextContent(url);
        else {
            Element addrElmt = _eprElmt.getOwnerDocument().createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
            addrElmt.setTextContent(url);
            _eprElmt.appendChild(addrElmt);
        }
    }

    public QName getServiceName() {
        NodeList metadataList = _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Metadata");
        if (metadataList.getLength() > 0) {
            Element metadata = (Element) metadataList.item(0);
            Element service = DOMUtils.getFirstChildElement(metadata);
            String serviceTextQName = service.getTextContent();
            int twoDotsIdx = serviceTextQName.indexOf(":");
            String prefix = serviceTextQName.substring(0, twoDotsIdx);
            String serviceNS = _eprElmt.getOwnerDocument().lookupNamespaceURI(prefix);
            // Lookup failed, checking directly on our element
            if (serviceNS == null) {
                serviceNS = service.getAttribute("xmlns:" + prefix);
            }
            if (serviceNS == null)
                __log.warn("Couldn't find an appropriate namespace for service!");
            QName result = new QName(serviceNS, serviceTextQName.substring(twoDotsIdx + 1, serviceTextQName.length()));
            if (__log.isDebugEnabled())
                __log.debug("Got service name from WSAEndpoint: " + result);
            return result;
        }
        return null;
    }

    public String getPortName() {
        NodeList metadataList = _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Metadata");
        if (metadataList.getLength() > 0) {
            Element metadata = (Element) metadataList.item(0);
            Element service = DOMUtils.getFirstChildElement(metadata);
            return service.getAttribute("EndpointName");
        }
        return null;
    }

    public boolean accept(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elmt = (Element) node;
            if (elmt.getLocalName().equals(SERVICE_REF_QNAME.getLocalPart())
                    && elmt.getNamespaceURI().equals(SERVICE_REF_QNAME.getNamespaceURI()))
                elmt = DOMUtils.getFirstChildElement(elmt);
            if (elmt != null && elmt.getLocalName().equals("EndpointReference")
                    && elmt.getNamespaceURI().equals(Namespaces.WS_ADDRESSING_NS))
                return true;
        }
        return false;
    }

    public void set(Node node) {
        if (node.getNamespaceURI().equals(SERVICE_REF_QNAME.getNamespaceURI()))
            _eprElmt = DOMUtils.getFirstChildElement((Element) node);
        else
            _eprElmt = (Element) node;
        if (__log.isDebugEnabled())
            __log.debug("Setting a WSAEndpoint value: " + DOMUtils.domToString(_eprElmt));
    }

    public Document toXML() {
        // Wrapping
        Document doc = DOMUtils.newDocument();
        Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
        doc.appendChild(serviceRef);
        serviceRef.appendChild(doc.importNode(_eprElmt, true));
        return doc;
    }

    public Map toMap() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put(ADDRESS, getUrl());
        String sid = getSessionId();
        if (sid != null)
            result.put(SESSION, sid);
        NodeList metadataList = _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Metadata");
        if (metadataList.getLength() > 0) {
            Element metadata = (Element) metadataList.item(0);
            Element service = DOMUtils.getFirstChildElement(metadata);
            String serviceTextQName = service.getTextContent();
            int twoDotsIdx = serviceTextQName.indexOf(":");
            String prefix = serviceTextQName.substring(0, twoDotsIdx);
            String serviceNS = null;
            try {
                serviceNS = _eprElmt.getOwnerDocument().lookupNamespaceURI(prefix);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Lookup failed, checking directly on our element
            if (serviceNS == null) {
                serviceNS = service.getAttribute("xmlns:" + prefix);
            }
            result.put(SERVICE_QNAME, new QName(serviceNS, serviceTextQName.substring(twoDotsIdx + 1, serviceTextQName
                    .length())));
            result.put(PORT_NAME, service.getAttribute("EndpointName"));
            if (__log.isDebugEnabled()) {
                __log.debug("Filled transfo map with service: " + result.get(SERVICE_QNAME));
                __log.debug("Filled transfo map with port: " + result.get(PORT_NAME));
            }
        }
        return result;
    }

    public void fromMap(Map eprMap) {
        Document doc = DOMUtils.newDocument();
        Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
        doc.appendChild(serviceRef);
        _eprElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
        serviceRef.appendChild(_eprElmt);
        Element addrElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
        addrElmt.setTextContent((String) eprMap.get(ADDRESS));
        if (eprMap.get(SESSION) != null) {
            Element sessElmt = doc.createElementNS(Namespaces.ODE_SESSION_NS, "session");
            sessElmt.setTextContent((String) eprMap.get(SESSION));
            _eprElmt.appendChild(sessElmt);
            // and the same for the (deprecated) intalio namespace for backward compatibility
            sessElmt = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
            sessElmt.setTextContent((String) eprMap.get(SESSION));
            _eprElmt.appendChild(sessElmt);
        }
        if (eprMap.get(SERVICE_QNAME) != null) {
            Element metadataElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Metadata");
            _eprElmt.appendChild(metadataElmt);
            Element serviceElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_WSDL_NS, "ServiceName");
            metadataElmt.appendChild(serviceElmt);
            QName serviceQName = (QName) eprMap.get(SERVICE_QNAME);
            serviceElmt.setAttribute("xmlns:servicens", serviceQName.getNamespaceURI());
            serviceElmt.setTextContent("servicens:" + serviceQName.getLocalPart());
            serviceElmt.setAttribute("EndpointName", (String) eprMap.get(PORT_NAME));
        }
        _eprElmt.appendChild(addrElmt);
        if (__log.isDebugEnabled())
            __log.debug("Constructed a new WSAEndpoint: " + DOMUtils.domToString(_eprElmt));
    }
}
