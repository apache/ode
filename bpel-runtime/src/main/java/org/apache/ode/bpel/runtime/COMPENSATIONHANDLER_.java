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

import org.apache.ode.bpel.evt.CompensationHandlerRegistered;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.CompensationChannelListener;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;
import org.apache.ode.jacob.SynchChannel;

import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Element;

/**
 * A scope that has completed succesfully, and may possibly have a compensation handler.
 */
class COMPENSATIONHANDLER_ extends BpelJacobRunnable {
    private static final long serialVersionUID = 1L;
    private CompensationHandler _self;
    private Set<CompensationHandler> _completedChildren;

    public COMPENSATIONHANDLER_(CompensationHandler self, Set<CompensationHandler> visibleCompensations) {
        _self = self;
        _completedChildren = visibleCompensations;
    }

    public void run() {
        sendEvent(new CompensationHandlerRegistered());
        object(new CompensationChannelListener(_self.compChannel) {
            private static final long serialVersionUID = -477602498730810094L;

            public void forget() {
                // Tell all our completed children to forget.
                for (Iterator<CompensationHandler> i = _completedChildren.iterator(); i.hasNext(); )
                    i.next().compChannel.forget();
            }

            public void compensate(final SynchChannel ret) {
                // Only scopes with compensation handlers can be compensated.
                assert _self.compensated.oscope.compensationHandler != null;

                ActivityInfo ai = new ActivityInfo(genMonotonic(),
                    _self.compensated.oscope.compensationHandler,
                    newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));


                ScopeFrame compHandlerScopeFrame = new ScopeFrame(
                    _self.compensated.oscope.compensationHandler,
                    getBpelRuntimeContext().createScopeInstance(_self.compensated.scopeInstanceId, _self.compensated.oscope.compensationHandler),
                    _self.compensated,
                    _completedChildren);

                // Create the compensation handler scope.
                instance(new SCOPE(ai,compHandlerScopeFrame, new LinkFrame(null)));

                object(new ParentScopeChannelListener(ai.parent) {
                    private static final long serialVersionUID = 8044120498580711546L;

                    public void compensate(OScope scope, SynchChannel ret) {
                        throw new AssertionError("Unexpected.");
                    }

                    public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                        // TODO: log faults.

                        // Compensations registered in a compensation handler are unreachable.
                        for (Iterator<CompensationHandler> i = compensations.iterator();i.hasNext(); ) {
                          i.next().compChannel.forget();
                        }

                        // Notify synchronized waiter that we are done.
                        ret.ret();
                    }

                    public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                    public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                });
            }
        });
    }

    private void sendEvent(ScopeEvent event) {
        _self.compensated.fillEventInfo(event);
        getBpelRuntimeContext().sendEvent(event);
    }

    public String toString() {
       return new StringBuffer(getClassName())
        .append(":")
        .append(_self.compensated)
        .append("(...)")
        .toString();
    }
}
