package org.apache.ode.jbi;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.utils.DOMUtils;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.EndpointReferenceContext}
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

  private final OdeContext _ode;

  static final QName JBI_EPR = new QName("http://java.sun.com/jbi/end-point-reference", "end-point-reference");
  
  public EndpointReferenceContextImpl(OdeContext ode) {
    _ode = ode;
  }

  public EndpointReference resolveEndpointReference(Element epr) {
    QName elname = new QName(epr.getNamespaceURI(),epr.getLocalName());
    
    if (__log.isDebugEnabled()) {
      __log.debug( "resolveEndpointReference:\n" + prettyPrint( epr ) );
    }
    if (elname.equals(EndpointReference.SERVICE_REF_QNAME)) {
        epr = DOMUtils.getFirstChildElement(epr);
        elname = new QName(epr.getNamespaceURI(),epr.getLocalName());
    }
    // resolve JBI end-point-references directly
    if (epr != null && elname.equals(JBI_EPR)) {
      String serviceName = epr.getAttribute("service-name");
      QName serviceQName = convertClarkQName( serviceName );
      String endpointName = epr.getAttribute("end-point-name");
      ServiceEndpoint se = _ode.getContext().getEndpoint(serviceQName, endpointName);
      if (se == null) {
        __log.warn( "Unable to resolve JBI endpoint reference:\n" + prettyPrint( epr ) );
        return null;
      }
      if (__log.isDebugEnabled()) {
        __log.debug( "Resolved JBI endpoint reference: " + se );
      }
      return new JbiEndpointReference(se);
    }
    
    // Otherwise, we expect the EPR to be wrapped in a BPEL service-ref element.
    /*
    if (!elname.equals(EndpointReference.SERVICE_REF_QNAME))
      throw new IllegalArgumentException("EPR root element "
          + elname + " should be " + EndpointReference.SERVICE_REF_QNAME);
    
    Document doc = DOMUtils.newDocument();
    DocumentFragment fragment = doc.createDocumentFragment();
    NodeList children = epr.getChildNodes();
    for (int i = 0 ; i < children.getLength(); ++i)
      fragment.appendChild(doc.importNode(children.item(i), true));
    ServiceEndpoint se = _ode.getContext().resolveEndpointReference(fragment);
    if (se == null)
      return null;
    return new JbiEndpointReference(se);
    */
  __log.warn( "Unsupported endpoint reference:\n" + prettyPrint( epr ) );
    return null;
  }
  
  public EndpointReference activateEndpoint(QName pid, QName serviceId, Element externalEpr) {
    try {
      return _ode.activateEndpoint(pid,serviceId,externalEpr);
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
      _ode.deactivateEndpoint((MyEndpointReference)epr);
    } catch (Exception ex) {
      String errmsg = "Could not deactivate endpoint: " + epr;
      __log.error(errmsg, ex);
      throw new ContextException(errmsg,ex);
    }
  }

  public EndpointReference convertEndpoint(QName eprType, Element element) {
    EndpointReference endpoint = EndpointFactory.convert(eprType, element);
 
    __log.warn( "convertEndpoint: " + eprType + " " + prettyPrint( element ) );
    
    // Forcing JBI lookup
    return resolveEndpointReference(endpoint.toXML().getDocumentElement());
  }
  
  public static QName convertClarkQName(String name) {
    int pos = name.indexOf('}');
    if ( name.startsWith("{") && pos > 0 ) {
      String ns = name.substring(1,pos);
      String lname = name.substring(pos+1, name.length());
      return new QName( ns, lname );
    }
    return new QName( name );
  }
 
  private String prettyPrint( Element el ) {
      try {
          return DOMUtils.prettyPrint( el );
      } catch ( java.io.IOException ioe ) {
          return ioe.getMessage();
      }
  }
}
