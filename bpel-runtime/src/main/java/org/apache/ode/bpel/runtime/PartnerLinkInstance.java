/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

public class PartnerLinkInstance implements Serializable {
	private static final long serialVersionUID = 1L;

	public OPartnerLink partnerLink;
  public Long scopeInstanceId;

  public PartnerLinkInstance(Long scopeInstanceId, OPartnerLink partnerLink) {
    this.partnerLink = partnerLink;
    this.scopeInstanceId = scopeInstanceId;
  }

  public boolean equals(Object obj) {
    PartnerLinkInstance other = (PartnerLinkInstance) obj;
    return partnerLink.equals(other.partnerLink) && scopeInstanceId.equals(other.scopeInstanceId);
  }

  public int hashCode() {
    return this.partnerLink.hashCode() ^ scopeInstanceId.hashCode();
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "partnerLinkDecl", partnerLink,
      "scopeInstanceId", scopeInstanceId
    });
  }
}
