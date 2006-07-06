package com.fs.pxe.axis;

import com.fs.pxe.axis.epr.EndpointFactory;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.EndpointReferenceContext;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public class EndpointReferenceContextImpl implements EndpointReferenceContext {

  public EndpointReference resolveEndpointReference(Element element) {
    return EndpointFactory.createEndpoint(element);
  }

  public EndpointReference activateEndpoint(QName qName, QName qName1, Element element) {
    // Axis doesn't need any explicit endpoint activation / deactivation
    return null;
  }

  public void deactivateEndpoint(EndpointReference endpointReference) {
    // Axis doesn't need any explicit endpoint activation / deactivation
  }
}
