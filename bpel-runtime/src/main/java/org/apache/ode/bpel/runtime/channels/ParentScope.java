/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime.channels;

import com.fs.jacob.SynchChannel;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.CompensationHandler;

import java.util.Set;

/**
 * Channel used for child-to-parent scope communication.
 * @jacob.kind
 */
public interface ParentScope {

  void compensate(OScope scope, SynchChannel ret);

  void completed(FaultData faultData, Set<CompensationHandler> compensations);

}
