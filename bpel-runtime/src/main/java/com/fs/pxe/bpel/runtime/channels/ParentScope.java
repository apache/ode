/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime.channels;

import com.fs.jacob.SynchChannel;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.CompensationHandler;

import java.util.Set;

/**
 * Channel used for child-to-parent scope communication.
 * @jacob.kind
 */
public interface ParentScope {

  void compensate(OScope scope, SynchChannel ret);

  void completed(FaultData faultData, Set<CompensationHandler> compensations);

}
