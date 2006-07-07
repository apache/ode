package com.fs.pxe.bpel.epr;

import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
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
    return _url;
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
      if (elmt.getLocalName().equals("service-ref") && elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS)) {
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
      else if (elmt.getNamespaceURI().equals(Namespaces.WS_BPEL_20_NS)) {
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
