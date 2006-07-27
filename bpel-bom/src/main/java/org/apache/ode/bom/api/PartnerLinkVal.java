package org.apache.ode.bom.api;

/**
 * Assignment L/R-value defined in terms of a BPEL partner link.
 */
public interface PartnerLinkVal extends From, To {

  String getPartnerLink();

  void setPartnerLink(String partnerLink);

  String getEndpointReference();

  void setEndpointReference(String epr);
}
