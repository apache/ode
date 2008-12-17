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
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * A service endpoint represented as a wsdl20:service element.
 */
public class WSDL20Endpoint implements MutableEndpoint {

  private Element _serviceElmt;

  public WSDL20Endpoint() {
  }

  public String getSessionId() {
	  Element endpointElmt = (Element)_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").item(0);
	  NodeList idNodes = endpointElmt.getElementsByTagNameNS(Namespaces.ODE_SESSION_NS, "session");
	  if (idNodes.getLength() > 0) {
		  return idNodes.item(0).getTextContent();
	  } else {
		  // try the same with the intalio header
		  idNodes = endpointElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "session");
		  if (idNodes.getLength() > 0) {
			  return idNodes.item(0).getTextContent();
		  } 
		  return null;
	  }
  }

  public void setSessionId(String sessionId) {
	  Element endpointElmt = (Element)_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").item(0);
	  NodeList idList = endpointElmt.getElementsByTagNameNS(Namespaces.ODE_SESSION_NS, "session");
	  if (idList.getLength() > 0)
		  idList.item(0).setTextContent(sessionId);
	  else {
		  Element sessElmt = endpointElmt.getOwnerDocument().createElementNS(Namespaces.ODE_SESSION_NS, "session");
		  sessElmt.setTextContent(sessionId);
		  endpointElmt.appendChild(sessElmt);
	  }

	  // and the same for the intalio header
	  idList = endpointElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "session");
	  if (idList.getLength() > 0)
		  idList.item(0).setTextContent(sessionId);
	  else {
		  Element sessElmt = endpointElmt.getOwnerDocument().createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
		  sessElmt.setTextContent(sessionId);
		  endpointElmt.appendChild(sessElmt);
	  }
  }

  public String getUrl() {
	  return ((Element)_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").item(0)).getAttribute("address");
  }

  public void setUrl(String url) {
    Element endpointElmt = (Element)_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").item(0);
    NodeList addrList = endpointElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "address");
    if (addrList.getLength() > 0) addrList.item(0).setTextContent(url);
    else {
      Element addrElmt = _serviceElmt.getOwnerDocument().createElementNS(Namespaces.WSDL_20, "address");
      addrElmt.setTextContent(url);
      endpointElmt.appendChild(addrElmt);
    }
  }

  public boolean accept(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element) node;
      if (elmt.getLocalName().equals("service-ref") &&
              (elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS) ||
                      elmt.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_SERVREF)))
        elmt= DOMUtils.getFirstChildElement(elmt);
      if (elmt.getLocalName().equals("service") && elmt.getNamespaceURI().equals(Namespaces.WSDL_20))
        return true;
    }
    return false;
  }

  public void set(Node node) {
    if (node.getNamespaceURI().equals(SERVICE_REF_QNAME.getNamespaceURI()))
      _serviceElmt = DOMUtils.getFirstChildElement((Element)node);
    else
      _serviceElmt = (Element) node;
  }

  public Document toXML() {
    // Wrapping
    Document doc = DOMUtils.newDocument();
    Element serviceRef = doc.createElementNS(Namespaces.WSBPEL2_0_FINAL_SERVREF, "service-ref");
    doc.appendChild(serviceRef);
    serviceRef.appendChild(doc.importNode(_serviceElmt, true));
    return doc;
  }

  public Map toMap() {
    HashMap<String,String> result = new HashMap<String,String>(1);
    result.put(ADDRESS, getUrl());
    String sid = getSessionId();
    if (sid != null) result.put(SESSION, sid);
    return result;
  }

  public void fromMap(Map eprMap) {
    Document doc = DOMUtils.newDocument();
    Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
    doc.appendChild(serviceRef);
    _serviceElmt = doc.createElementNS(Namespaces.WSDL_20, "service");
    _serviceElmt.setAttribute("name", "");
    _serviceElmt.setAttribute("interface", "");
    serviceRef.appendChild(_serviceElmt);
    Element endpoint = doc.createElementNS(Namespaces.WSDL_20, "endpoint");
    endpoint.setAttribute("name", "");
    endpoint.setAttribute("binding", "");
    if (eprMap.get(ADDRESS) != null) endpoint.setAttribute("address", (String) eprMap.get(ADDRESS));
    if (eprMap.get(SESSION) != null) {
        Element session = doc.createElementNS(Namespaces.ODE_SESSION_NS, "session");
        session.setTextContent((String) eprMap.get(SESSION));
        endpoint.appendChild(session);
        // plus the deprecated intalio header
    	session = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
    	session.setTextContent((String) eprMap.get(SESSION));
    	endpoint.appendChild(session);
    }
    _serviceElmt.appendChild(endpoint);
    doc.appendChild(_serviceElmt);
  }
}
