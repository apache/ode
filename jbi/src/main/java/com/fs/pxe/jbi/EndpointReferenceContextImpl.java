package com.fs.pxe.jbi;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.EndpointReferenceContext;
import com.fs.utils.DOMUtils;

/**
 * Implementation of the PXE {@link com.fs.pxe.bpel.iapi.EndpointReferenceContext}
 * interface used by the BPEL engine to convert XML descriptions of endpoint 
 * references (EPRs) into Java object representations. In the JBI context all
 * endpoint references are considered to be JBI 
 * {@link javax.jbi.servicedesc.ServiceEndpoint}s are resolved by using the
 * {@link javax.jbi.component.ComponentContext#resolveEndpointReference(org.w3c.dom.DocumentFragment)}
 * method. Note that is is possible to resolve both "internal" and "external"
 * endpoint in this manner.  The schema to the internal end-point representation
 * is described on page 50 of the JBI specification 1.0.
 *
 */
public class EndpointReferenceContextImpl implements EndpointReferenceContext {
  private static final Log __log = LogFactory.getLog(EndpointReferenceContextImpl.class);

  private final PxeContext _pxe;
  
  public EndpointReferenceContextImpl(PxeContext pxe) {
    _pxe = pxe;
  }

  public EndpointReference resolveEndpointReference(Element epr) {
    QName elname = new QName(epr.getNamespaceURI(),epr.getLocalName());
    
    // We always expect the EPR to be wrapped in a BPEL service-ref element.
    if (!elname.equals(EndpointReference.SERVICE_REF_QNAME))
      throw new IllegalArgumentException("EPR root element "
          + elname + " should be " + EndpointReference.SERVICE_REF_QNAME);
    
    Document doc = DOMUtils.newDocument();
    DocumentFragment fragment = doc.createDocumentFragment();
    NodeList children = epr.getChildNodes();
    for (int i = 0 ; i < children.getLength(); ++i)
      fragment.appendChild(doc.importNode(children.item(i), true));
    ServiceEndpoint se = _pxe.getContext().resolveEndpointReference(fragment);
    if (se == null)
      return null;
    return new JbiEndpointReference(se);
  }

  public EndpointReference activateEndpoint(QName pid, QName serviceId, Element externalEpr) {
    try {
      return _pxe.activateEndpoint(pid,serviceId,externalEpr);
    } catch (Exception ex) {
      throw new ContextException("Could not activate endpoint for " + serviceId, ex);
    }
  }

  public void deactivateEndpoint(EndpointReference epr) {
    if (!(epr instanceof MyEndpointReference)) {
      String errmsg = "deactivateEndpoint misused.";
      __log.fatal(errmsg);
      throw new IllegalArgumentException(errmsg);
    }

    try {
      _pxe.deactivateEndpoint((MyEndpointReference)epr);
    } catch (Exception ex) {
      String errmsg = "Could not deactivate endpoint: " + epr;
      __log.error(errmsg, ex);
      throw new ContextException(errmsg,ex);
    }
  }

  
}
