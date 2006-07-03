package com.fs.pxe.sfwk.impl.endpoint;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * A service endpoint represented as a wsdl11:service element.
 */
public class WSDL11ServiceEndpoint implements ServiceEndpoint, MapReducibleEndpoint {

  private Element _serviceElmt;

  public WSDL11ServiceEndpoint() {
  }

  public WSDL11ServiceEndpoint(Element serviceElmt) {
    _serviceElmt = serviceElmt;
  }

  public String getUrl() {
    return _serviceElmt.getElementsByTagNameNS(Namespaces.SOAP_NS, "address").item(0).getTextContent();
  }

  public boolean accept(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element) node;
      if (elmt.getLocalName().equals("service") && elmt.getNamespaceURI().equals(Namespaces.WSDL_11))
        if (elmt.getElementsByTagNameNS(Namespaces.SOAP_NS, "address").getLength() > 0)
          return true;
    }
    return false;
  }

  public void set(Node node) {
    _serviceElmt = (Element) node;
  }

  public Node toXML() {
    return _serviceElmt;
  }

  public Map toMap() {
    HashMap result = new HashMap(1);
    result.put(ADDRESS, getUrl());
    return result;
  }

  public void fromMap(Map eprMap) {
    Document doc = DOMUtils.newDocument();
    _serviceElmt = doc.createElementNS(Namespaces.WSDL_11, "service");
    _serviceElmt.setAttribute("name", "");
    Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
    port.setAttribute("name", "");
    port.setAttribute("binding", "");
    Element address = doc.createElementNS(Namespaces.SOAP_NS, "address");
    if (eprMap.get(ADDRESS) != null) address.setAttribute("location", (String) eprMap.get(ADDRESS));

    _serviceElmt.appendChild(port);
    _serviceElmt.appendChild(address);
    doc.appendChild(_serviceElmt);
  }

}
