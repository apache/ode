/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.OScope;

/**
 * A scope activity. The scope activity creates a new scope frame and proceeeds
 * using the {@link SCOPE} template. 
 */
public class SCOPEACT extends ACTIVITY {
  public SCOPEACT(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public void self() {
    ScopeFrame newFrame = new ScopeFrame(
            (OScope) _self.o,getBpelRuntimeContext().createScopeInstance(_scopeFrame.scopeInstanceId,(OScope) _self.o),
            _scopeFrame,
            null);
    instance(new SCOPE(_self,newFrame, _linkFrame));
  }
}
