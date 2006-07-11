package com.fs.pxe.bpel.provider;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.EndpointReferenceContext;

public class EndpointReferenceContextImpl  implements EndpointReferenceContext {

  private static final Log __log = LogFactory.getLog(EndpointReferenceContextImpl.class);
  BpelServiceProvider _serviceProvider;
  
  public EndpointReferenceContextImpl(BpelServiceProvider provider) {
    _serviceProvider = provider;
  }

  public EndpointReference resolveEndpointReference(Element epr) {

    // For now assume all are URL eprs. 
    try {
      return new URLEndpointReferenceImpl(epr);
    } catch (Exception ex) {
      __log.error("Failed to create EPR.",ex);
      return null;
    }
  }

  public EndpointReference activateEndpoint(QName processId, QName serviceId, Element externalEpr) {
    // TODO Auto-generated method stub
    return null;
  }

  public void deactivateEndpoint(EndpointReference epr) {
    // TODO Auto-generated method stub
    
  }

  public EndpointReference convertEndpoint(QName targetType, Element sourceEndpoint) {
    return null;
  }

}
