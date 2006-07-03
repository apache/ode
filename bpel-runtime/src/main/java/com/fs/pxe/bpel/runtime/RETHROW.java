/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.runtime.channels.FaultData;


/**
 * FaultActivity
 */
class RETHROW extends ACTIVITY {
  private static final long serialVersionUID = -6433171659586530126L;

  RETHROW(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public void self() {
    // find the faultData in the scope stack
    FaultData fault = _scopeFrame.getFault();
    if(fault == null){
      String msg = "Attempting to execute 'rethrow' activity with no visible fault in scope.";
      log().error(msg);
      throw new InvalidProcessException(msg); 
    }

    _self.parent.completed(fault,CompensationHandler.emptySet());
  }
}
