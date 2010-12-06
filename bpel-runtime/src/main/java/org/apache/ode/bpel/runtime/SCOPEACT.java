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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.LinkStatusChannel;
import org.apache.ode.bpel.runtime.channels.LinkStatusChannelListener;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.ReadWriteLockChannel;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.jacob.SynchChannelListener;
import org.apache.ode.jacob.ValChannel;
import org.apache.ode.jacob.ValChannelListener;
import org.w3c.dom.Element;

/**
 * A scope activity. The scope activity creates a new scope frame and proceeeds using the {@link SCOPE} template.
 */
public class SCOPEACT extends ACTIVITY {
    private static final Log __log = LogFactory.getLog(SCOPEACT.class);
    
    private static final long serialVersionUID = -4593029783757994939L;

    public SCOPEACT(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
    }

    public void run() {

        
        if (((OScope) _self.o).isolatedScope) {
            __log.debug("found ISOLATED scope, instance ISOLATEDGUARD");
            instance(new ISOLATEDGUARD(createLockList(), newChannel(SynchChannel.class)));

        } else {
            ScopeFrame newFrame = new ScopeFrame((OScope) _self.o, getBpelRuntimeContext().createScopeInstance(
                    _scopeFrame.scopeInstanceId, (OScope) _self.o), _scopeFrame, null);

            // Depending on whether we are ATOMIC or not, we'll need to create outgoing link status interceptors
            LinkFrame linkframe;
            if (((OScope) _self.o).atomicScope && !_self.o.outgoingLinks.isEmpty()) {
                ValChannel linkInterceptorControl = newChannel(ValChannel.class);
                ParentScopeChannel psc = newChannel(ParentScopeChannel.class);
                linkframe = createInterceptorLinkFrame();
                instance(new LINKSTATUSINTERCEPTOR(linkInterceptorControl,linkframe));
                instance(new UNLOCKER(psc, _self.parent, null, Collections.<IsolationLock>emptyList(), linkInterceptorControl));
                _self.parent = psc;
            } else
                linkframe = _linkFrame;
            
            instance(new SCOPE(_self, newFrame, linkframe));
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

    /**
     * Create outgoing link interceptors. Necessary for ISOLATED and ATOMIC (non-standard ext) scopes. I.e we need to prevent the
     * links from coming out until the scope completes successfully.
     * 
     */
    private LinkFrame createInterceptorLinkFrame() {
        LinkFrame newframe = new LinkFrame(_linkFrame);
        for (OLink outlink : _self.o.outgoingLinks) {
            LinkInfo original = _linkFrame.resolve(outlink);
            LinkStatusChannel newchannel = newChannel(LinkStatusChannel.class);
            newframe.links.put(original.olink, new LinkInfo(original.olink, newchannel, newchannel));
        }
        return newframe;
    }

    /**
     * Link Status interceptor. Used in ISOLATED and ATOMIC scopes to prevent links from getting out until its time.
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     * 
     */
    private class LINKSTATUSINTERCEPTOR extends BpelJacobRunnable {
        private static final long serialVersionUID = 3104008741240676253L;

        /** We'll listen here for notification that its ok to send links status out. */
        private final ValChannel _self;

        private final LinkFrame _interceptedChannels;

        /** The statuses that have been received */
        private final Map<OLink, Boolean> _statuses = new HashMap<OLink, Boolean>();

        /** NULL means defer links, TRUE means passthrough, FALSE means send FALSE */
        private Boolean _status;

        LINKSTATUSINTERCEPTOR(ValChannel self, LinkFrame interceptedChannels) {
            _self = self;
            _interceptedChannels = interceptedChannels;
        }

        @Override
        public void run() {

            __log.debug("LINKSTATUSINTERCEPTOR: running ");

            Set<ChannelListener> mlset = new HashSet<ChannelListener>();
            
            if (_status == null)
                mlset.add(new ValChannelListener(_self) {
    
                    private static final long serialVersionUID = 5029554538593371750L;
    
                    /** Our owner will notify us when it becomes clear what to do with the links. */
                    public void val(Object retVal) {
                        if (__log.isDebugEnabled()) {
                            __log.debug("LINKSTATUSINTERCEPTOR: status received " + retVal);
                        }
                        
                        _status = (Boolean) retVal;
                        for (OLink available : _statuses.keySet())
                            _linkFrame.resolve(available).pub.linkStatus(_statuses.get(available) && _status);
    
                        // Check if we still need to wait around for more links.
                        if (!isDone())
                            instance(LINKSTATUSINTERCEPTOR.this);
    
                    }
    
                });

            for (final Map.Entry<OLink, LinkInfo> m : _interceptedChannels.links.entrySet()) {
                if (_statuses.containsKey(m.getKey()))
                    continue;
            
                mlset.add(new LinkStatusChannelListener(m.getValue().pub) {
                    private static final long serialVersionUID = 1568144473514091593L;

                    public void linkStatus(boolean value) {
                        _statuses.put(m.getKey(), value);
                        if (_status != null)
                            _linkFrame.resolve(m.getKey()).pub.linkStatus(value && _status);
                        
                        if (!isDone())
                            instance(LINKSTATUSINTERCEPTOR.this);

                    }

                });
            }
            
            object(false, mlset);

        }

        /**
         * Did we get all the links we need?
         * @return
         */
        private boolean isDone() {
            return (_statuses.keySet().size() < SCOPEACT.this._self.o.outgoingLinks.size());

        }

    }
    
    
    /**
     * Guard for ISOLATED scopes to prevent start until all locks are acquired.
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     *
     */
    private class ISOLATEDGUARD extends BpelJacobRunnable {

        private static final long serialVersionUID = -5017579415744600900L;

        final List<IsolationLock> _locksNeeded;

        final LinkedList<IsolationLock> _locksAcquired = new LinkedList<IsolationLock>();

        final SynchChannel _synchChannel;

        ISOLATEDGUARD(List<IsolationLock> locks, SynchChannel synchChannel) {
            _locksNeeded = locks;
            _synchChannel = synchChannel;
        }

        @Override
        public void run() {
            if (_locksNeeded.isEmpty()) {
                // acquired all locks.
                if (__log.isDebugEnabled()) {
                    __log.debug("ISOLATIONGUARD: got all required locks: " + _locksAcquired);
                }

                ScopeFrame newFrame = new ScopeFrame((OScope) _self.o, getBpelRuntimeContext().createScopeInstance(
                        _scopeFrame.scopeInstanceId, (OScope) _self.o), _scopeFrame, null);

                
                final ParentScopeChannel parent = _self.parent;
                _self.parent = newChannel(ParentScopeChannel.class);
                ValChannel lsi = newChannel(ValChannel.class);
                instance(new UNLOCKER(_self.parent, parent, _synchChannel, _locksAcquired, lsi));
                LinkFrame linkframe = createInterceptorLinkFrame();
                instance(new LINKSTATUSINTERCEPTOR(lsi,linkframe));
                instance(new SCOPE(_self, newFrame, linkframe));
                return;
            } else {
                if (__log.isDebugEnabled()) {
                    __log.debug("ISOLATIONGUARD: don't have all locks still need: " + _locksNeeded);
                }

                // try to acquire the locks in sequence (IMPORTANT) not all at once.
                IsolationLock il = _locksNeeded.get(0);
                
                if (il.writeLock)
                    il.lockChannel.writeLock(_synchChannel);
                else
                    il.lockChannel.readLock(_synchChannel);

                object(new SynchChannelListener(_synchChannel) {
                    private static final long serialVersionUID = 2857261074409098274L;

                    public void ret() {
                        __log.debug("ISOLATIONGUARD: got lock: " + _locksNeeded.get(0));
                        _locksAcquired.add(_locksNeeded.remove(0));
                        instance(ISOLATEDGUARD.this);
                    }
                });

            }
        }

    }

    /**
     * Interceptor that waits for the scope to finish and unlock the acquired locks.
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     *
     */
    private class UNLOCKER extends BpelJacobRunnable {

        private static final long serialVersionUID = -476393080609348172L;

        private final ParentScopeChannel _self;

        private final ParentScopeChannel _parent;

        private final SynchChannel _synchChannel;
        
        private final List<IsolationLock> _locks;

        private final ValChannel _linkStatusInterceptor;

        public UNLOCKER(ParentScopeChannel self, ParentScopeChannel parent, SynchChannel synchChannel,
                List<IsolationLock> locksAcquired,
                ValChannel linkStatusInterceptor) {
            _self = self;
            _parent = parent;
            _synchChannel = synchChannel;
            _locks = locksAcquired;
            _linkStatusInterceptor = linkStatusInterceptor;
        }

        @Override
        public void run() {

            __log.debug("running UNLOCKER");
            object(new ParentScopeChannelListener(_self) {

                public void cancelled() {
                    _parent.cancelled();
                    unlockAll();
                    _linkStatusInterceptor.val(false);
                    // no more listening.
                }

                public void compensate(OScope scope, SynchChannel ret) {
                    _parent.compensate(scope, ret);
                    // keep listening
                    instance(UNLOCKER.this);
                }

                public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                    _parent.completed(faultData, compensations);
                    _linkStatusInterceptor.val(faultData == null);
                    unlockAll();
                    // no more listening

                }

                public void failure(String reason, Element data) {
                    _parent.failure(reason, data);
                    _linkStatusInterceptor.val(false);
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
            if (__log.isDebugEnabled()) {
                __log.debug("UNLOCKER: unlockAll: " + _locks);
            }

            if (((OScope)SCOPEACT.this._self.o).atomicScope)
                getBpelRuntimeContext().forceFlush();
                
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
    private static class IsolationLock implements Comparable<IsolationLock>, Serializable {
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
