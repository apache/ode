package org.apache.ode.iapi;

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
  
}
