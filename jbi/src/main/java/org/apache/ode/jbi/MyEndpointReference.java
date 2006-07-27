package org.apache.ode.jbi;

import javax.jbi.servicedesc.ServiceEndpoint;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.utils.DOMUtils;

/**
 * Endpoint representing a BPEL "myRole" partner link.
 */
class MyEndpointReference implements EndpointReference {
  private OdeService _service;
 
  MyEndpointReference(OdeService service) {
    _service = service;
    
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MyEndpointReference)
      return _service.getInternalServiceEndpoint().getServiceName().equals(
          ((MyEndpointReference)obj)._service.getInternalServiceEndpoint().getServiceName());
    return false;
  }


  @Override
  public int hashCode() {
    return _service.getInternalServiceEndpoint().getServiceName().hashCode();
  }

  public Document toXML() {
    Document xml = DOMUtils.newDocument();
    
    // Prefer to use the external endpoint as our EPR,
    // but if we dont find one, use the internal endpoint.
    ServiceEndpoint se = _service.getExternalServiceEndpoint();
    if (se == null)
      se = _service.getInternalServiceEndpoint();
    
    Element root = xml.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
        EndpointReference.SERVICE_REF_QNAME.getLocalPart());
    xml.appendChild(root);
    
    // TODO: handle the operation name problem.
    DocumentFragment fragment = se.getAsReference(null);
    root.appendChild(fragment);
    return xml;
  }

  OdeService getService() {
    return _service;
  }

}
