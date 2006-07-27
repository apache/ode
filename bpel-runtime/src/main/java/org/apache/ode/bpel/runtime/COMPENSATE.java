/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import com.fs.jacob.SynchChannel;
import com.fs.jacob.SynchML;
import org.apache.ode.bpel.o.OCompensate;
import org.apache.ode.bpel.o.OScope;


/**
 * Runtime implementation of the <code>&lt;compensate&gt;</code> activity.
 */
class COMPENSATE extends ACTIVITY {
  private static final long serialVersionUID = -467758076635337675L;
  private OCompensate _ocompact;

  public COMPENSATE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
    _ocompact = (OCompensate) self.o;
  }

  public final void self() {
    OScope scopeToCompensate = _ocompact.compensatedScope;
    SynchChannel sc = newChannel(SynchChannel.class);
    _self.parent.compensate(scopeToCompensate,sc);
    object(new SynchML(sc) {
    private static final long serialVersionUID = 3763991229748926216L;

    public void ret() {
        _self.parent.completed(null, CompensationHandler.emptySet());
      }
    });
  }
}
