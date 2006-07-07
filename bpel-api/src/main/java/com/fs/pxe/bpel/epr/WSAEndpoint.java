package com.fs.pxe.bpel.epr;

import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * A service endpoint represented as a WS-Addressing EndpointReference.
 */
public class WSAEndpoint implements MutableEndpoint {

  private Element _eprElmt;

  public String getSessionId() {
    NodeList idNodes = _eprElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "identifier");
    if (idNodes.getLength() > 0) return idNodes.item(0).getTextContent();
    else return null;
  }

  public void setSessionId(String sessionId) {
    NodeList idList = _eprElmt.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "identifier");
    if (idList.getLength() > 0) idList.item(0).setTextContent(sessionId);
    else {
      Element sessElmt = _eprElmt.getOwnerDocument().createElementNS(Namespaces.INTALIO_SESSION_NS, "identifier");
      sessElmt.setTextContent(sessionId);
      _eprElmt.appendChild(sessElmt);
    }
  }

  public String getUrl() {
    return _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Address").item(0).getTextContent();
  }

  public void setUrl(String url) {
    NodeList addrList = _eprElmt.getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Address");
    if (addrList.getLength() > 0) addrList.item(0).setTextContent(url);
    else {
      Element addrElmt = _eprElmt.getOwnerDocument().createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
      addrElmt.setTextContent(url);
      _eprElmt.appendChild(addrElmt);
    }
  }

  public boolean accept(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element)node;
      if (elmt.getLocalName().equals("service-ref") && elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS))
        elmt= DOMUtils.getFirstChildElement(elmt);
      if (elmt.getLocalName().equals("EndpointReference") && elmt.getNamespaceURI().equals(Namespaces.WS_ADDRESSING_NS))
        return true;
    }
    return false;
  }

  public void set(Node node) {
    if (node.getNamespaceURI().equals(SERVICE_REF_QNAME.getNamespaceURI()))
      _eprElmt = DOMUtils.getFirstChildElement((Element)node);
    else
      _eprElmt = (Element) node;
  }

  public Document toXML() {
    // Wrapping
    Document doc = DOMUtils.newDocument();
    Element serviceRef = doc.createElementNS(Namespaces.WS_BPEL_20_NS, "service-ref");
    doc.appendChild(serviceRef);
    serviceRef.appendChild(doc.importNode(_eprElmt, true));
    return _eprElmt.getOwnerDocument();
  }

  public Map toMap() {
    HashMap<String,String> result = new HashMap<String,String>();
    result.put(ADDRESS, getUrl());
    String sid = getSessionId();
    if (sid != null) result.put(SESSION, sid);
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
      Element sessElmt = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "identifier");
      sessElmt.setTextContent((String) eprMap.get(SESSION));
      _eprElmt.appendChild(sessElmt);
    }
    _eprElmt.appendChild(addrElmt);
  }
}
