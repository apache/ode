/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.OScope;

public class CorrelationSetInstance {
  public OScope.CorrelationSet declaration;
  public Long scopeInstance;

  public CorrelationSetInstance(Long scopeInstanceId, OScope.CorrelationSet cset) {
    this.scopeInstance = scopeInstanceId;
    this.declaration = cset;
  }

}
