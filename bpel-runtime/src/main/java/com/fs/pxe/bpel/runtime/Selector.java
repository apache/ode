/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.utils.ObjectPrinter;

import java.io.Serializable;

public class Selector implements Serializable {
	private static final long serialVersionUID = 1L;

	public final PartnerLinkInstance plinkInstance;
  public final CorrelationKey correlationKey;
  public final String opName;
  public final String messageExchangeId;
  public final int idx;
  public final boolean oneWay;

  Selector(int idx, PartnerLinkInstance plinkInstance, String opName, boolean oneWay, String mexId, CorrelationKey ckey) {
    this.idx = idx;
    this.plinkInstance = plinkInstance;
    this.correlationKey = ckey;
    this.opName = opName;
    this.messageExchangeId = mexId;
    this.oneWay = oneWay;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "plinkInstnace", plinkInstance,
      "ckey", correlationKey,
      "opName" ,opName,
      "oneWay", oneWay ? "yes" : "no",
      "mexId", messageExchangeId,
      "idx", Integer.valueOf(idx)
    });
  }
}
