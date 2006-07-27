package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.PartnerLinkVal;

public class PartnerLinkValImpl extends BpelObjectImpl implements PartnerLinkVal {
  private static final long serialVersionUID = 1L;

  private String _partnerLink;
  private String _endpointReference;

  public String getPartnerLink() {
    return _partnerLink;
  }

  public void setPartnerLink(String partnerLink) {
    _partnerLink = partnerLink;
  }

  public String getEndpointReference() {
    return _endpointReference;
  }

  public void setEndpointReference(String endpointReference) {
    _endpointReference = endpointReference;
  }
}
