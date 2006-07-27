/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.runtime.channels.CompensationChannel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;


/**
 * A handle to a compensation handler.
 */
public class CompensationHandler implements Serializable {
	private static final long serialVersionUID = 1L;

	/** The scope to which this compensation handler belongs. */
  final ScopeFrame compensated;

  /** Compensation activation channel. */
  final CompensationChannel compChannel;

  /** Time that the scope was started. */
  final long scopeStartTime;

  /** Time that the scope was completed. */
  final long scopeEndTime;

  CompensationHandler(ScopeFrame compensated, CompensationChannel compChannel, long scopeStartTime, long scopeEndTime) {
  	assert compChannel != null;
  	
    this.compensated = compensated;
    this.compChannel = compChannel;
    this.scopeEndTime = scopeEndTime;
    this.scopeStartTime = scopeStartTime;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer("{CompensationHandler ch=");
    buf.append(compChannel);
    buf.append(", scope=");
    buf.append(compensated);
    buf.append("}");
    return buf.toString();
  }

  static Set<CompensationHandler> emptySet() {
    return Collections.emptySet();
  }

}
