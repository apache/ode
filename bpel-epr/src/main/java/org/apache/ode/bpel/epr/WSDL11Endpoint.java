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

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * A service endpoint represented as a wsdl11:service element.
 */
public class WSDL11Endpoint implements MutableEndpoint {

  private Element _serviceElmt;

  public WSDL11Endpoint() {
  }

    public WSDL11Endpoint(QName serviceQName, String portName, String location) {
        Document doc = DOMUtils.newDocument();
        Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
        doc.appendChild(serviceRef);
        _serviceElmt = doc.createElementNS(Namespaces.WSDL_11, "service");
        serviceRef.appendChild(_serviceElmt);
        if (serviceQName != null) {
            _serviceElmt.setAttribute("name", serviceQName.getLocalPart());
            _serviceElmt.setAttribute("targetNamespace", serviceQName.getNamespaceURI());
        }
        Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
        if (portName != null) {
            port.setAttribute("name", portName);
        }
        port.setAttribute("binding", "");
        Element address = doc.createElementNS(Namespaces.SOAP_NS, "address");
        if (location != null) address.setAttribute("location", location);

        _serviceElmt.appendChild(port);
        port.appendChild(address);
    }

    public WSDL11Endpoint(QName serviceName, String portName) {
        this(serviceName, portName, null);
    }

    public String getUrl() {
        Element port = (Element) _serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_11, "port").item(0);
        // get soap:address
        Element address = (Element) port.getElementsByTagNameNS(Namespaces.SOAP_NS, "address").item(0);
        // ... or the http:address
        if (address == null) {
            address = (Element) port.getElementsByTagNameNS(Namespaces.HTTP_NS, "address").item(0);
        }
        if (address == null) {
            throw new IllegalArgumentException("soap:address and http:address element in element "
                    + DOMUtils.domToString(_serviceElmt) + " is missing or in the wrong namespace.");
        }
        return address.getAttribute("location");
    }

  public QName getServiceName() {
    return new QName(_serviceElmt.getAttribute("targetNamespace"), _serviceElmt.getAttribute("name"));
  }

  public boolean accept(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element) node;
      if (elmt.getLocalName().equals("service-ref") &&
              (elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS) ||
               elmt.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_SERVREF)))
        elmt= DOMUtils.getFirstChildElement(elmt);
      if (elmt.getLocalName().equals("service") && elmt.getNamespaceURI().equals(Namespaces.WSDL_11))
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
    HashMap<String,Object> result = new HashMap<String,Object>(1);
    result.put(ADDRESS, getUrl());
    result.put(SERVICE_QNAME, new QName(_serviceElmt.getAttribute("targetNamespace"), _serviceElmt.getAttribute("name")));
    Element port = DOMUtils.getFirstChildElement(_serviceElmt);
    result.put(PORT_NAME, port.getAttribute("name"));
    // TODO binding
    return result;
  }

  public void fromMap(Map eprMap) {
    Document doc = DOMUtils.newDocument();
    Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
    doc.appendChild(serviceRef);
    _serviceElmt = doc.createElementNS(Namespaces.WSDL_11, "service");
    serviceRef.appendChild(_serviceElmt);
    if (eprMap.get(SERVICE_QNAME) != null) {
      QName serviceQName = ((QName) eprMap.get(SERVICE_QNAME));
      _serviceElmt.setAttribute("name", serviceQName.getLocalPart());
      _serviceElmt.setAttribute("targetNamespace", serviceQName.getNamespaceURI());
    }
    Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
    if (eprMap.get(PORT_NAME) != null) {
      port.setAttribute("name", (String) eprMap.get(PORT_NAME));
    }
    port.setAttribute("binding", "");
    Element address = doc.createElementNS(Namespaces.SOAP_NS, "address");
    if (eprMap.get(ADDRESS) != null) address.setAttribute("location", (String) eprMap.get(ADDRESS));

    _serviceElmt.appendChild(port);
    port.appendChild(address);
  }

}
