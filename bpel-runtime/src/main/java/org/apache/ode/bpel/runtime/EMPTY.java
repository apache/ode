/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstraction that performs the work of the <code>empty</code> activity.
 */
class EMPTY extends ACTIVITY {
	private static final long serialVersionUID = 1L;
	private static final Log __log = LogFactory.getLog(EMPTY.class);

  public EMPTY(ActivityInfo self, ScopeFrame frame, LinkFrame linkFrame) {
    super(self, frame, linkFrame);
  }

  public final void self() {
    if (__log.isDebugEnabled()) {
      __log.debug("<empty name=" + _self.o + ">");
    }

    _self.parent.completed(null, CompensationHandler.emptySet());
  }
}
