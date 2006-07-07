package com.fs.pxe.axis.epr;

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
    System.out.println("### Getting endpoint " + DOMUtils.domToString(_serviceElmt));
    Element port = (Element) _serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_11, "port").item(0);
    Element address = (Element) port.getElementsByTagNameNS(Namespaces.SOAP_NS, "address").item(0);
    return address.getAttribute("location");
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
    if (node.getNamespaceURI().equals(SERVICE_REF_QNAME.getNamespaceURI()))
      _serviceElmt = DOMUtils.getFirstChildElement((Element)node);
    else
      _serviceElmt = (Element) node;
  }

  public Document toXML() {
    return _serviceElmt.getOwnerDocument();
  }

  public Map toMap() {
    HashMap<String,Object> result = new HashMap<String,Object>(1);
    result.put(ADDRESS, getUrl());
    result.put(SERVICE_QNAME, new QName(_serviceElmt.getNamespaceURI(), _serviceElmt.getAttribute("name")));
    Element port = DOMUtils.getFirstChildElement(_serviceElmt);
    result.put(PORT_NAME, port.getAttribute("name"));
    // TODO binding
    return result;
  }

  public void fromMap(Map eprMap) {
    Document doc = DOMUtils.newDocument();
    Element serviceRef = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(), SERVICE_REF_QNAME.getLocalPart());
    _serviceElmt = doc.createElementNS(Namespaces.WSDL_11, "service");
    serviceRef.appendChild(_serviceElmt);
    if (eprMap.get(SERVICE_QNAME) != null) {
      QName serviceQName = ((QName) eprMap.get(SERVICE_QNAME));
      _serviceElmt.setAttribute("name", serviceQName.getLocalPart());
      _serviceElmt.setAttribute("xmlns", serviceQName.getNamespaceURI());
    }
    Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
    if (eprMap.get(PORT_NAME) != null) {
      port.setAttribute("name", (String) eprMap.get(PORT_NAME));
    }
    port.setAttribute("binding", "");
    Element address = doc.createElementNS(Namespaces.SOAP_NS, "address");
    if (eprMap.get(ADDRESS) != null) address.setAttribute("location", (String) eprMap.get(ADDRESS));

    _serviceElmt.appendChild(port);
    _serviceElmt.appendChild(address);
    doc.appendChild(_serviceElmt);
  }

}
