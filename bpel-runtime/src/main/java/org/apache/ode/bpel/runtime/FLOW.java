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
import org.apache.ode.bpel.o.OFlow;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.LinkStatusChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.utils.stl.FilterIterator;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.w3c.dom.Element;

class FLOW extends ACTIVITY {
    private static final long serialVersionUID = 1L;

    private OFlow _oflow;

    private Set<ChildInfo> _children = new HashSet<ChildInfo>();

    public FLOW(ActivityInfo self, ScopeFrame frame, LinkFrame linkFrame) {
        super(self,frame, linkFrame);
        _oflow = (OFlow) self.o;
    }

    public void run() {
        LinkFrame myLinkFrame = new LinkFrame(_linkFrame);
        for (Iterator<OLink> i = _oflow.localLinks.iterator(); i.hasNext(); ) {
            OLink link = i.next();
            LinkStatusChannel lsc = newChannel(LinkStatusChannel.class);
            myLinkFrame.links.put(link,new LinkInfo(link,lsc,lsc));
        }

        for (Iterator<OActivity> i = _oflow.parallelActivities.iterator(); i.hasNext();) {
            OActivity ochild = i.next();
            ChildInfo childInfo = new ChildInfo(
                new ActivityInfo(genMonotonic(), ochild,
                                 newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class)));
            _children.add(childInfo);

            instance(createChild(childInfo.activity,_scopeFrame, myLinkFrame));
        }
        instance(new ACTIVE());
    }

    private class ACTIVE extends BpelJacobRunnable {
        private static final long serialVersionUID = -8494641460279049245L;
        private FaultData _fault;
        private HashSet<CompensationHandler> _compensations = new HashSet<CompensationHandler>();

        public void run() {
            Iterator<ChildInfo> active = active();
            if (active.hasNext()) {
                Set<ChannelListener> mlSet = new HashSet<ChannelListener>();
                mlSet.add(new TerminationChannelListener(_self.self) {
                    private static final long serialVersionUID = 2554750258974084466L;

                    public void terminate() {
                        for (Iterator<ChildInfo> i = active(); i.hasNext(); )
                            replication(i.next().activity.self).terminate();
                        instance(ACTIVE.this);
                    }
                });

                for (;active.hasNext();) {
                    final ChildInfo child = active.next();
                    mlSet.add(new ParentScopeChannelListener(child.activity.parent) {
                        private static final long serialVersionUID = -8027205709169238172L;

                        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                            child.completed = true;
                            _compensations.addAll(compensations);

                            // If we receive a fault, we request termination of all our activities
                            if (faultData != null && _fault == null) {
                                for (Iterator<ChildInfo> i = active(); i.hasNext(); )
                                    replication(i.next().activity.self).terminate();
                                _fault = faultData;
                            }
                            instance(ACTIVE.this);
                        }

                        public void compensate(OScope scope, SynchChannel ret) {
                            // Flow does not do compensations, forward these to parent.
                            _self.parent.compensate(scope, ret);
                            instance(ACTIVE.this);
                        }

                        public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                        public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                    });
                }
                object(false,mlSet);
            } else /** No More active children. */ {
                // NOTE: we do not not have to do DPE here because all the children
                // have been started, and are therefore expected to set the value of
                // their outgoing links.
                _self.parent.completed(_fault, _compensations);
            }
        }
    }

    public String toString() {
        return "<T:Act:Flow:" + _oflow.name + ">";
    }

    private Iterator<ChildInfo> active() {
        return new FilterIterator<ChildInfo>(_children.iterator(), new MemberOfFunction<ChildInfo>() {
            public boolean isMember(ChildInfo childInfo) {
                return !childInfo.completed;
            }
        });
    }

}
