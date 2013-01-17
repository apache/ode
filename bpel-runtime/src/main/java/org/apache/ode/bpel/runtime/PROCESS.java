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

import java.util.Set;

import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OFailureHandling;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScope;
import org.apache.ode.bpel.runtime.channels.ReadWriteLock;
import org.apache.ode.bpel.runtime.channels.Termination;
import org.apache.ode.jacob.ReceiveProcess;
import org.apache.ode.jacob.Synch;
import org.w3c.dom.Element;

public class PROCESS extends BpelJacobRunnable {
    private static final long serialVersionUID = 1L;
    private OProcess _oprocess;
    private InstanceGlobals _globals;

    public PROCESS(OProcess process) {
        _oprocess = process;
    }

    public void run() {
        BpelRuntimeContext ntive = getBpelRuntimeContext();
        Long scopeInstanceId = ntive.createScopeInstance(null, _oprocess.procesScope);

        createGlobals();
        ProcessInstanceStartedEvent evt = new ProcessInstanceStartedEvent();
        evt.setRootScopeId(scopeInstanceId);
        evt.setScopeDeclarationId(_oprocess.procesScope.getId());
        ntive.sendEvent(evt);

        ActivityInfo child = new ActivityInfo(genMonotonic(),
            _oprocess.procesScope,
            newChannel(Termination.class), newChannel(ParentScope.class));
        ScopeFrame processFrame = new ScopeFrame(_oprocess.procesScope, scopeInstanceId, null, null,_globals);
        instance(new SCOPE(child, processFrame, new LinkFrame(null)));

        object(new ReceiveProcess<ParentScope>(child.parent, new ParentScope() {
            public void compensate(OScope scope, Synch ret) {
                assert false;
            }

            public void completed(FaultData fault, Set<CompensationHandler> compensations) {
                BpelRuntimeContext nativeAPI = (BpelRuntimeContext)getExtension(BpelRuntimeContext.class);
                if (fault == null) {
                    nativeAPI.completedOk();
                } else {
                    nativeAPI.completedFault(fault);
                }
            }

            public void cancelled() {
                this.completed(null, CompensationHandler.emptySet());
            }

            public void failure(String reason, Element data) {
                FaultData faultData = createFault(OFailureHandling.FAILURE_FAULT_NAME, _oprocess, reason);
                this.completed(faultData, CompensationHandler.emptySet());
            }
        }) {
            private static final long serialVersionUID = -8564969578471906493L;
        });
    }

    private void createGlobals() {
        _globals = new InstanceGlobals();
        
        // For each variable, we create a lock.
        for (OBase child : _oprocess.getChildren()) {
            if (child instanceof OScope.Variable) {
                OScope.Variable var = (Variable) child;
                ReadWriteLock vlock = newChannel(ReadWriteLock.class);
                instance(new READWRITELOCK(vlock));
                _globals._varLocks.put(var, vlock);
            }
        }
    }
}
