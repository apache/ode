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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OWhile;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.jacob.SynchChannel;
import org.w3c.dom.Element;

/**
 * BPEL &lt;while&gt; activity
 */
class WHILE extends ACTIVITY {
    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(WHILE.class);

    private Set<CompensationHandler> _compHandlers = new HashSet<CompensationHandler>();

    public WHILE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
    }

    public void run() {

        boolean condResult = false;

        try {
            condResult = checkCondition();
        } catch (FaultException fe) {
            __log.error(fe);
            _self.parent.completed(createFault(fe.getQName(), _self.o),_compHandlers);
            return;
        }

        if (condResult) {
            ActivityInfo child = new ActivityInfo(genMonotonic(),
                    getOWhile().activity,
                    newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
            instance(createChild(child, _scopeFrame, _linkFrame));
            instance(new WAITER(child));
        } else /* stop. */ {
            _self.parent.completed(null, _compHandlers);
        }
    }

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    public String toString() {
        return "<T:Act:While:" + _self.o + ">";
    }

    protected Log log() {
        return __log;
    }

    private OWhile getOWhile() {
        return (OWhile)_self.o;
    }

    /**
     * Evaluates the while condition.
     *
     * @return <code>true</code> if the while condition is satisfied, <code>false</code> otherwise.
     * @throws FaultException in case of standard expression fault (e.g. selection failure)
     */
    private boolean checkCondition() throws FaultException {
        try {
            return getBpelRuntimeContext().getExpLangRuntime().evaluateAsBoolean(getOWhile().whileCondition,getEvaluationContext());
        } catch (EvaluationException e) {
            String msg = "Unexpected expression evaluation error checking while condition.";
            __log.error(msg, e);
            throw new InvalidProcessException(msg,e);
        }
    }

    private class WAITER extends BpelJacobRunnable {
        private static final long serialVersionUID = -7645042174027252066L;
        private ActivityInfo _child;
        private boolean _terminated;

        WAITER(ActivityInfo child) {
            _child = child;
        }

        public void run() {
            object(false, new TerminationChannelListener(_self.self) {
                private static final long serialVersionUID = -5471984635653784051L;

                public void terminate() {
                    _terminated = true;
                    replication(_child.self).terminate();
                    instance(WAITER.this);
                }
            }.or(new ParentScopeChannelListener(_child.parent) {
                private static final long serialVersionUID = 3907167240907524405L;

                public void compensate(OScope scope, SynchChannel ret) {
                    _self.parent.compensate(scope,ret);
                    instance(WAITER.this);
                }

                public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                    _compHandlers.addAll(compensations);
                    if (_terminated || faultData != null)
                        _self.parent.completed(faultData, compensations);
                    else
                        instance(WHILE.this);
                }

                public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
            }));
        }
    }
}
