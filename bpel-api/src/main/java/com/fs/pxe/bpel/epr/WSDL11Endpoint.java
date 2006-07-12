package com.fs.pxe.bpel.epr;

import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
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

  public String getUrl() {
    Element port = (Element) _serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_11, "port").item(0);
    Element address = (Element) port.getElementsByTagNameNS(Namespaces.SOAP_NS, "address").item(0);
    return address.getAttribute("location");
  }

  public QName getServiceName() {
    return new QName(_serviceElmt.getAttribute("targetNamespace"), _serviceElmt.getAttribute("name"));
  }

  public boolean accept(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element) node;
      if (elmt.getLocalName().equals("service-ref") && elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS))
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
    Element serviceRef = doc.createElementNS(Namespaces.WS_BPEL_20_NS, "service-ref");
    doc.appendChild(serviceRef);
    serviceRef.appendChild(doc.importNode(_serviceElmt, true));
    return _serviceElmt.getOwnerDocument();
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
