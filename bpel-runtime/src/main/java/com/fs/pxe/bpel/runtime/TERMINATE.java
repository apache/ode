/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

/**
 * Abstraction that performs the work of the <code>empty</code> activity.
 */
class TERMINATE extends ACTIVITY {

	private static final long serialVersionUID = 1L;

	public TERMINATE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public final void self() {
    getBpelRuntimeContext().terminate();
    _self.parent.completed(null, CompensationHandler.emptySet());
  }

}
