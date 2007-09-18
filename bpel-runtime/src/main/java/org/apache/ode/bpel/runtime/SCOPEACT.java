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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.ReadWriteLockChannel;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.jacob.SynchChannelListener;
import org.w3c.dom.Element;

/**
 * A scope activity. The scope activity creates a new scope frame and proceeeds using the {@link SCOPE} template.
 */
public class SCOPEACT extends ACTIVITY {
    private static final long serialVersionUID = -4593029783757994939L;

    public SCOPEACT(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
    }

    public void run() {

        if (((OScope) _self.o).isolatedScope) {
            instance(new ISOLATEDGUARD(createLockList(), newChannel(SynchChannel.class)));

        } else {
            ScopeFrame newFrame = new ScopeFrame((OScope) _self.o, getBpelRuntimeContext().createScopeInstance(
                    _scopeFrame.scopeInstanceId, (OScope) _self.o), _scopeFrame, null);

            instance(new SCOPE(_self, newFrame, _linkFrame));
        }

    }

    /**
     * Create an ordered list of required locks that need to be acquired before the activity can execute. The list is ordered to
     * prevent dead-lock. The method of ordering is not especially important, so long as the same method is always used.
     * 
     * @return
     */
    private List<IsolationLock> createLockList() {
        LinkedList<IsolationLock> requiredLocks = new LinkedList<IsolationLock>();
        OScope o = ((OScope) _self.o);

        Set<Variable> vrs = new HashSet<Variable>(o.variableRd);
        vrs.addAll(o.variableWr);

        for (Variable v : vrs)
            requiredLocks.add(new IsolationLock(v, o.variableWr.contains(v), _scopeFrame.globals._varLocks.get(v)));

        // Very important, we must sort the locks to prevent deadlocks.
        Collections.sort(requiredLocks);

        return requiredLocks;
    }

    private class ISOLATEDGUARD extends BpelJacobRunnable {

        private static final long serialVersionUID = -5017579415744600900L;
        
        final List<IsolationLock> _locksNeeded;
        final LinkedList<IsolationLock> _locksAcquired = new LinkedList<IsolationLock>();
        final SynchChannel _synchChannel;

        public ISOLATEDGUARD(List<IsolationLock> locks, SynchChannel synchChannel) {
            _locksNeeded = locks;
            _synchChannel = synchChannel;
        }

        @Override
        public void run() {
            if (_locksNeeded.isEmpty()) {
                // acquired all locks.
                
                ScopeFrame newFrame = new ScopeFrame((OScope) _self.o, getBpelRuntimeContext().createScopeInstance(
                        _scopeFrame.scopeInstanceId, (OScope) _self.o), _scopeFrame, null);
                // need to make sure to release the locks after the scope completes, to do this, intercept messages
                // on the parent scope channel.
                if (!_locksAcquired.isEmpty()) {
                    final ParentScopeChannel parent = _self.parent;
                    _self.parent = newChannel(ParentScopeChannel.class);
                    instance(new UNLOCKER(_self.parent, parent,_synchChannel, _locksAcquired));
                }
                
                instance(new SCOPE(_self, newFrame, _linkFrame));
                return;
            } else {
                // try to acquire the locks in sequence (IMPORTANT) not all at once.
                IsolationLock il = _locksNeeded.get(0);
                if (il.writeLock)
                    il.lockChannel.writeLock(_synchChannel);
                else
                    il.lockChannel.readLock(_synchChannel);
                
                object(new SynchChannelListener(_synchChannel) {
                        private static final long serialVersionUID = 2857261074409098274L;

                        public void ret() {
                            _locksAcquired.add(_locksNeeded.remove(0));
                            instance(ISOLATEDGUARD.this);
                        }
                });
            
            }    
        }
        
    }

    
    private static class UNLOCKER extends BpelJacobRunnable {

        private static final long serialVersionUID = -476393080609348172L;
        
        private final ParentScopeChannel _self;
        private final ParentScopeChannel _parent;
        private final SynchChannel _synchChannel;
        private final LinkedList<IsolationLock> _locks;

        public UNLOCKER(ParentScopeChannel self, ParentScopeChannel parent, SynchChannel synchChannel, LinkedList<IsolationLock> locksAcquired) {
            _self = self;
            _parent = parent;
            _synchChannel = synchChannel;
            _locks = locksAcquired;
        }

        @Override
        public void run() {

            object(new ParentScopeChannelListener(_self) {

                public void cancelled() {
                    _parent.cancelled();
                    unlockAll();
                    // no more listening.
                }

                public void compensate(OScope scope, SynchChannel ret) {
                    _parent.compensate(scope, ret);
                    // keep listening
                    instance(UNLOCKER.this);
                }

                public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                    _parent.completed(faultData, compensations);
                    unlockAll();
                    // no more listening
                    
                }

                public void failure(String reason, Element data) {
                    _parent.failure(reason, data);
                    unlockAll();
                    // no more listening
                }
                
            });            
        }

        /**
         * Unlock all the acquired locks. 
         *
         */
        private void unlockAll() {
            for (IsolationLock il : _locks) 
                il.lockChannel.unlock(_synchChannel);
            _locks.clear();
        }
        
    }

    /**
     * Representation of a lock needed by an isolated scope.
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     * 
     */
    private static class IsolationLock implements Comparable<IsolationLock>,Serializable {
        private static final long serialVersionUID = 4214864393241172705L;

        OScope.Variable guardedObject;

        boolean writeLock;

        ReadWriteLockChannel lockChannel;

        public IsolationLock(OScope.Variable go, boolean writeLock, ReadWriteLockChannel channel) {
            this.guardedObject = go;
            this.writeLock = writeLock;
            this.lockChannel = channel;
        }

        public int compareTo(IsolationLock o) {
            return guardedObject.getId() - o.guardedObject.getId();
        }

    }
}
