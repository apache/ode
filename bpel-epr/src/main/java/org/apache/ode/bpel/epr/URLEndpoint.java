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

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the ServiceEndpoint interface backended by a simple URL.
 */
public class URLEndpoint implements MutableEndpoint {

    private String _url;

    public URLEndpoint() {
    }

    public String getUrl() {
        return _url != null ? _url.trim() : null;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public boolean accept(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) return true;
        else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elmt = (Element)node;
            if (elmt.getLocalName().equals("address") && elmt.getNamespaceURI().equals(Namespaces.SOAP_NS))
                return true;
            if (elmt.getLocalName().equals("service-ref") &&
                    (elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS) ||
                     elmt.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_SERVREF))) {
                if (DOMUtils.getFirstChildElement(elmt) == null)
                    return true;
                elmt = DOMUtils.getFirstChildElement(elmt);
                if (elmt.getLocalName().equals("address") && elmt.getNamespaceURI().equals(Namespaces.SOAP_NS))
                    return true;
            }
        }
        return false;
    }

    public void set(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) _url = ((Text)node).getWholeText();
        else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elmt = (Element)node;
            if (elmt.getNamespaceURI().equals(Namespaces.SOAP_NS))
                _url = elmt.getAttribute("location");
            else if (elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS) ||
                     elmt.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_SERVREF)) {
                if (DOMUtils.getFirstChildElement(elmt) == null)
                    _url = elmt.getTextContent();
                else {
                    elmt = DOMUtils.getFirstChildElement(elmt);
                    _url = elmt.getAttribute("location");
                }
            }
        }
    }

    public Document toXML() {
        Document doc = DOMUtils.newDocument();
        Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
        doc.appendChild(serviceRef);
        Node urlNode = doc.createTextNode(_url);
        serviceRef.appendChild(urlNode);
        return doc;
    }

    public Map toMap() {
        HashMap result = new HashMap();
        result.put(ADDRESS, _url);
        return result;
    }

    public void fromMap(Map eprMap) {
        _url = (String) eprMap.get(ADDRESS);
    }
}
