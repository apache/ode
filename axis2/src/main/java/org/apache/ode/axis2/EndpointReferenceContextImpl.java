package org.apache.ode.axis2;

import org.apache.ode.bpel.epr.EndpointFactory;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class EndpointReferenceContextImpl implements EndpointReferenceContext {

  private static final Log __log = LogFactory.getLog(EndpointReferenceContextImpl.class);

  public EndpointReference resolveEndpointReference(Element element) {
    if (__log.isDebugEnabled())
      __log.debug("Resolving endpoint reference " + DOMUtils.domToString(element));
    return EndpointFactory.createEndpoint(element);
  }

  public EndpointReference activateEndpoint(QName qName, QName qName1, Element element) {
    // Axis doesn't need any explicit endpoint activation / deactivation
    return null;
  }

  public void deactivateEndpoint(EndpointReference endpointReference) {
    // Axis doesn't need any explicit endpoint activation / deactivation
  }

  public EndpointReference convertEndpoint(QName qName, Element element) {
    EndpointReference endpoint = EndpointFactory.convert(qName, element);
    return endpoint;
  }
}
