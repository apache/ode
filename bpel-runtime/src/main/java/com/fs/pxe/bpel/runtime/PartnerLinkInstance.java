/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.utils.ObjectPrinter;

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
