package com.fs.pxe.sfwk.impl.endpoint;

import com.fs.pxe.sfwk.core.StatefulServiceEndpoint;
import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * A service endpoint represented as a wsdl20:service element.
 */
public class WSDL20ServiceEndpoint implements StatefulServiceEndpoint, MapReducibleEndpoint {

  private Element _serviceElmt;

  public WSDL20ServiceEndpoint() {
  }

  public String getSessionId() {
    Element endpointElmt = (Element)_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").item(0);
    NodeList idNodes = endpointElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "identifier");
    if (idNodes.getLength() > 0) return idNodes.item(0).getTextContent();
    else return null;
  }

  public void setSessionId(String sessionId) {
    Element endpointElmt = (Element)_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").item(0);
    NodeList idList = endpointElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "identifier");
    if (idList.getLength() > 0) idList.item(0).setTextContent(sessionId);
    else {
      Element sessElmt = _serviceElmt.getOwnerDocument().createElementNS(Namespaces.INTALIO_SESSION_NS, "identifier");
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

  public WSDL20ServiceEndpoint(Element serviceElmt) {
    _serviceElmt = serviceElmt;
  }

  public boolean accept(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element) node;
      if (elmt.getLocalName().equals("service") && elmt.getNamespaceURI().equals(Namespaces.WSDL_20))
        if (_serviceElmt.getElementsByTagNameNS(Namespaces.WSDL_20, "endpoint").getLength() > 0)
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
    String sid = getSessionId();
    if (sid != null) result.put(ADDRESS, sid);
    return result;
  }

  public void fromMap(Map eprMap) {
    Document doc = DOMUtils.newDocument();
    _serviceElmt = doc.createElementNS(Namespaces.WSDL_20, "service");
    _serviceElmt.setAttribute("name", "");
    _serviceElmt.setAttribute("interface", "");
    Element endpoint = doc.createElementNS(Namespaces.WSDL_20, "endpoint");
    endpoint.setAttribute("name", "");
    endpoint.setAttribute("binding", "");
    if (eprMap.get(ADDRESS) != null) endpoint.setAttribute("address", (String) eprMap.get(ADDRESS));
    if (eprMap.get(SESSION) != null) {
      Element session = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "identifier");
      session.setTextContent((String) eprMap.get(SESSION));
      endpoint.appendChild(session);
    }
    _serviceElmt.appendChild(endpoint);
    doc.appendChild(_serviceElmt);
  }
}
