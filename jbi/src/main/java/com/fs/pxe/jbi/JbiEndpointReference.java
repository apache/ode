package com.fs.pxe.jbi;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.utils.DOMUtils;

/**
 * JBI-based implementation of the PXE {@link com.fs.pxe.bpel.iapi.EndpointReference}
 * interface. This is basically a wrapper around the 
 * {@link javax.jbi.servicedesc.ServiceEndpoint} interface. 
 */
class JbiEndpointReference implements EndpointReference {

  private ServiceEndpoint _se;

  JbiEndpointReference(ServiceEndpoint se) {
    if (se == null)
      throw new NullPointerException("Null ServiceEndpoint");
    _se = se;
  }
  
  public Document toXML() {
    DocumentFragment fragment = _se.getAsReference(null);
    if (fragment == null)
      return null;
    
    Document doc = DOMUtils.newDocument();
    Element root = doc.createElementNS(SERVICE_REF_QNAME.getNamespaceURI(),SERVICE_REF_QNAME.getLocalPart());
    root.appendChild(fragment);
    doc.appendChild(root);
    return doc;
  }

  public boolean equals(Object other) {
    if (other instanceof JbiEndpointReference)
      return _se.getServiceName().equals(((JbiEndpointReference)other)._se.getServiceName());
    return false;
  }
  
  public int hashCode() {
    return _se.getServiceName().hashCode();
  }

  ServiceEndpoint getServiceEndpoint() {
    return _se;
  }
}
