/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import com.fs.jacob.SynchChannel;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OSequence;
import org.apache.ode.bpel.runtime.channels.*;

import java.util.*;

/**
 * Implementation of the BPEL &lt;sequence&gt; activity.
 */
class SEQUENCE extends ACTIVITY {
	private static final long serialVersionUID = 1L;
	private final List<OActivity> _remaining;
  private final Set<CompensationHandler> _compensations;

  SEQUENCE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    this(self, scopeFrame, linkFrame, ((OSequence)self.o).sequence, CompensationHandler.emptySet());
  }

  SEQUENCE(ActivityInfo self,
           ScopeFrame scopeFrame,
           LinkFrame linkFrame,
           List<OActivity> remaining,
           Set<CompensationHandler> compensations) {
    super(self, scopeFrame, linkFrame);
    _remaining = Collections.unmodifiableList(remaining);
    _compensations =Collections.unmodifiableSet(compensations);
  }

  public void self() {
    final ActivityInfo child = new  ActivityInfo(genMonotonic(),
            _remaining.get(0),
            newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
    instance(createChild(child, _scopeFrame, _linkFrame));
    instance(new ACTIVE(child));
  }

  private class ACTIVE extends BpelAbstraction {
    private static final long serialVersionUID = -2663862698981385732L;
    private ActivityInfo _child;
    private boolean _terminateRequested = false;

    ACTIVE(ActivityInfo child) {
      _child = child;
    }

    public void self() {
      object(false, new TerminationML(_self.self) {
        private static final long serialVersionUID = -2680515407515637639L;

        public void terminate() {
          replication(_child.self).terminate();

          // Don't do any of the remaining activiites, DPE instead.
          deadPathRemaining();

          _terminateRequested = true;
          instance(ACTIVE.this);
        }
      }.or(new ParentScopeML(_child.parent) {
        private static final long serialVersionUID = 7195562310281985971L;

        public void compensate(OScope scope, SynchChannel ret) {
          _self.parent.compensate(scope,ret);
          instance(ACTIVE.this);
        }

        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
          HashSet<CompensationHandler> comps = new HashSet<CompensationHandler>(_compensations);
          comps.addAll(compensations);
          if (faultData != null || _terminateRequested || _remaining.size() <= 1) {
            _self.parent.completed(faultData, comps);
          } else /* !fault && ! terminateRequested && !remaining.isEmpty */ {
            ArrayList<OActivity> remaining = new ArrayList<OActivity>(_remaining);
            remaining.remove(0);
            instance(new SEQUENCE(_self, _scopeFrame, _linkFrame, remaining, comps));
          }
        }
      }));
    }

    private void deadPathRemaining() {
      for (Iterator<OActivity> i = _remaining.iterator();i.hasNext();)
        dpe(i.next());
    }

  }

  public String toString() {
    StringBuffer buf = new StringBuffer("SEQUENCE(self=");
    buf.append(_self);
    buf.append(", linkframe=");
    buf.append(_linkFrame);
    buf.append(", remaining=");
    buf.append(_remaining);
    buf.append(')');
    return buf.toString();
  }
}
