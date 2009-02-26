package org.apache.ode.bpel.engine.migration;

import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

public class OldSelector implements Serializable {
    private static final long serialVersionUID = 1L;

    public final PartnerLinkInstance plinkInstance;
    // here for the backward compatibility
    @SuppressWarnings("unused")
    public Object correlationKey = null;
  public final String opName;
  public final String messageExchangeId;
  public final int idx;
  public final boolean oneWay;

  OldSelector(int idx, PartnerLinkInstance plinkInstance, String opName, boolean oneWay, String mexId) {
    this.idx = idx;
    this.plinkInstance = plinkInstance;
    this.opName = opName;
    this.messageExchangeId = mexId;
    this.oneWay = oneWay;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "plinkInstnace", plinkInstance,
      "opName" ,opName,
      "oneWay", oneWay ? "yes" : "no",
      "mexId", messageExchangeId,
      "idx", Integer.valueOf(idx)
    });
  }
}
