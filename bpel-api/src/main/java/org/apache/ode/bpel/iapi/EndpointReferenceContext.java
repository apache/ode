package org.apache.ode.bpel.iapi;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Endpoint reference context: facililates the creation of 
 * {@link EndpointReference} objects.
 *
 */
public interface EndpointReferenceContext {
  
  /**
   * Resolve an end-point reference from its XML representation. The
   * nature of the representation is determined by the integration 
   * layer. The BPEL engine uses this method to reconstruct 
   * {@link EndpointReference}  objects that have been persisted in the
   * database via {@link EndpointReference#toXML(javax.xml.transform.Result)}
   * method.
   * 
   * @param XML representation of the EPR
   * @return reconsistituted {@link EndpointReference}
   */
  EndpointReference resolveEndpointReference(Element epr);
  
  /**
   * Activate a "myRole" endpoint.
   * @param pid process identifier
   * @param serviceId service identifier
   * @param externalEpr external EPR representation
   * @return 
   */
  EndpointReference activateEndpoint(QName pid,
      QName serviceId, 
      Element externalEpr);
  

  /**
   * Deactivate a "myRole" endpoint.
   * @param epr EPR returned from {@link #activateEndpoint(QName, QName, Element)}
   */
  void deactivateEndpoint(EndpointReference epr);

  /**
   * Converts an endpoint reference from its XML representation to another
   * type of endpoint reference.
   * @param targetType
   * @param sourceEndpoint
   * @return converted EndpointReference, being of targetType
   */
  EndpointReference convertEndpoint(QName targetType, Element sourceEndpoint);
}
