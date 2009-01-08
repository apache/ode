/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OSequence;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.jacob.SynchChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Element;

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

    public void run() {
        final ActivityInfo child = new  ActivityInfo(genMonotonic(),
            _remaining.get(0),
            newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
        instance(createChild(child, _scopeFrame, _linkFrame));
        instance(new ACTIVE(child));
    }

    private class ACTIVE extends BpelJacobRunnable {
        private static final long serialVersionUID = -2663862698981385732L;
        private ActivityInfo _child;
        private boolean _terminateRequested = false;

        ACTIVE(ActivityInfo child) {
            _child = child;
        }

        public void run() {
            object(false, new TerminationChannelListener(_self.self) {
                private static final long serialVersionUID = -2680515407515637639L;

                public void terminate() {
                    replication(_child.self).terminate();

                    // Don't do any of the remaining activiites, DPE instead.
                    ArrayList<OActivity> remaining = new ArrayList<OActivity>(_remaining);
                    remaining.remove(0);
                    deadPathRemaining(remaining);

                    _terminateRequested = true;
                    instance(ACTIVE.this);
                }
            }.or(new ParentScopeChannelListener(_child.parent) {
                private static final long serialVersionUID = 7195562310281985971L;

                public void compensate(OScope scope, SynchChannel ret) {
                    _self.parent.compensate(scope,ret);
                    instance(ACTIVE.this);
                }

                public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                    TreeSet<CompensationHandler> comps = new TreeSet<CompensationHandler>(_compensations);
                    comps.addAll(compensations);
                    if (faultData != null || _terminateRequested || _remaining.size() <= 1) {
                        deadPathRemaining(_remaining);
                        _self.parent.completed(faultData, comps);
                    } else /* !fault && ! terminateRequested && !remaining.isEmpty */ {
                        ArrayList<OActivity> remaining = new ArrayList<OActivity>(_remaining);
                        remaining.remove(0);
                        instance(new SEQUENCE(_self, _scopeFrame, _linkFrame, remaining, comps));
                    }
                }

                public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
            }));
        }

        private void deadPathRemaining(List<OActivity> remaining) {
            for (Iterator<OActivity> i = remaining.iterator();i.hasNext();)
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
