package com.fs.pxe.sfwk.impl.endpoint;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
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
public class URLServiceEndpoint implements ServiceEndpoint, MapReducibleEndpoint {

  private String _url;

  public URLServiceEndpoint() {
  }

  public URLServiceEndpoint(String url) {
    _url = url;
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
    }
    return false;
  }

  public void set(Node node) {
    if (node.getNodeType() == Node.TEXT_NODE) _url = ((Text)node).getWholeText();
    else if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element elmt = (Element)node;
      _url = elmt.getAttribute("location");
    }
  }

  public Node toXML() {
    Document doc = DOMUtils.newDocument();
    Node urlNode = doc.createTextNode(_url);
    return urlNode;
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
