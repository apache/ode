/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.o.OScope;

public class CorrelationSetInstance {
  public OScope.CorrelationSet declaration;
  public Long scopeInstance;

  public CorrelationSetInstance(Long scopeInstanceId, OScope.CorrelationSet cset) {
    this.scopeInstance = scopeInstanceId;
    this.declaration = cset;
  }

}
