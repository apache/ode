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
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OForEach;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannelListener;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;
import org.apache.ode.bpel.runtime.channels.TerminationChannelListener;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.stl.FilterIterator;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FOREACH extends ACTIVITY {

    private static final long serialVersionUID = 1L;
    private static final Log __log = LogFactory.getLog(FOREACH.class);

    private OForEach _oforEach;
    private Set<ChildInfo> _children = new HashSet<ChildInfo>();
    private Set<CompensationHandler> _compHandlers = new HashSet<CompensationHandler>();
    private int _startCounter = -1;
    private int _finalCounter = -1;
    private int _currentCounter = -1;
    private int _completedCounter = 0;
    private int _completionCounter = -1;

    public FOREACH(ActivityInfo self, ScopeFrame frame, LinkFrame linkFrame) {
        super(self,frame, linkFrame);
        _oforEach = (OForEach) self.o;
    }

    public void run() {
        try {
            _startCounter = evaluateCondition(_oforEach.startCounterValue);
            _finalCounter = evaluateCondition(_oforEach.finalCounterValue);
            if (_oforEach.completionCondition != null) {
                _completionCounter = evaluateCondition(_oforEach.completionCondition.branchCount);
            }
            _currentCounter = _startCounter;
        } catch (FaultException fe) {
            __log.error(fe);
            _self.parent.completed(createFault(fe.getQName(), _self.o), _compHandlers);
            return;
        }

        // Checking for bpws:invalidBranchCondition when the counter limit is superior
        // to the maximum number of children
        if (_completionCounter > 0 && _completionCounter > _finalCounter - _startCounter) {
            _self.parent.completed(
                    createFault(_oforEach.getOwner().constants.qnInvalidBranchCondition, _self.o), _compHandlers);
            return;
        }

        // There's really nothing to do
        if (_finalCounter < _startCounter || _completionCounter == 0) {
            _self.parent.completed(null, _compHandlers);
        } else {
            // If we're parrallel, starting all our child copies, otherwise one will suffice.
            if (_oforEach.parallel) {
                for (int m = _startCounter; m <= _finalCounter; m++) {
                    newChild();
                }
            } else newChild();
            instance(new ACTIVE());
        }
    }

    private class ACTIVE extends BpelJacobRunnable {
        private static final long serialVersionUID = -5642862698981385732L;

        private FaultData _fault;
        private boolean _terminateRequested = false;

        public void run() {
            Iterator<ChildInfo> active = active();
            // Continuing as long as a child is active
            if (active().hasNext()) {

                Set<ChannelListener> mlSet = new HashSet<ChannelListener>();
                mlSet.add(new TerminationChannelListener(_self.self) {
                    private static final long serialVersionUID = 2554750257484084466L;

                    public void terminate() {
                        // Terminating all children before sepuku
                        for (Iterator<ChildInfo> i = active(); i.hasNext(); )
                            replication(i.next().activity.self).terminate();
                        _terminateRequested = true;
                        instance(ACTIVE.this);
                    }
                });
                for (;active.hasNext();) {
                    // Checking out our children
                    final ChildInfo child = active.next();
                    mlSet.add(new ParentScopeChannelListener(child.activity.parent) {
                        private static final long serialVersionUID = -8027205709961438172L;

                        public void compensate(OScope scope, SynchChannel ret) {
                            // Forward compensation to parent
                            _self.parent.compensate(scope, ret);
                            instance(ACTIVE.this);
                        }

                        public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
                            child.completed = true;
                            //
                            if (_completionCounter > 0 && _oforEach.completionCondition.successfulBranchesOnly) {
                                if (faultData != null) _completedCounter++;
                            } else _completedCounter++;

                            _compHandlers.addAll(compensations);

                            // Keeping the fault to let everybody know
                            if (faultData != null && _fault == null) {
                                _fault = faultData;
                            }
                            if (shouldContinue() && _fault == null && !_terminateRequested) {
                                // Everything fine. If parrallel, just let our children be, otherwise making a new child
                                if (!_oforEach.parallel) newChild();
                            } else {
                                // Work is done or something wrong happened, children shouldn't continue
                                for (Iterator<ChildInfo> i = active(); i.hasNext(); )
                                    replication(i.next().activity.self).terminate();
                            }
                            instance(ACTIVE.this);
                        }

                        public void cancelled() { completed(null, CompensationHandler.emptySet()); }
                        public void failure(String reason, Element data) { completed(null, CompensationHandler.emptySet()); }
                    });
                }
                object(false,mlSet);
            } else {
                // No children left, either because they've all been executed or because we
                // had to make them stop.
                _self.parent.completed(_fault, _compHandlers);
            }
        }
    }

    private boolean shouldContinue() {
        boolean stop = false;
        if (_completionCounter > 0) {
            stop = (_completedCounter >= _completionCounter) || stop;
        }
        stop = (_startCounter + _completedCounter > _finalCounter) || stop;
        return !stop;
    }

    private int evaluateCondition(OExpression condition)
            throws FaultException {
        try {
            return getBpelRuntimeContext().getExpLangRuntime().
                    evaluateAsNumber(condition, getEvaluationContext()).intValue();
        } catch (EvaluationException e) {
            String msg;
            msg = "ForEach counter value couldn't be evaluated as xs:unsignedInt.";
            __log.error(msg, e);
            throw new FaultException(_oforEach.getOwner().constants.qnForEachCounterError,msg);
        }
    }

    private void newChild() {
        ChildInfo child = new ChildInfo(new ActivityInfo(genMonotonic(), _oforEach.innerScope,
                newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class)));
        _children.add(child);

        // Creating the current counter value node
        Document doc = DOMUtils.newDocument();
        Node counterNode = doc.createTextNode(""+_currentCounter++);

        // Instantiating the scope directly to keep control of its scope frame, allows
        // the introduction of the counter variable in there (monkey business that is).
        ScopeFrame newFrame = new ScopeFrame(
                _oforEach.innerScope, getBpelRuntimeContext().createScopeInstance(_scopeFrame.scopeInstanceId,
                _oforEach.innerScope), _scopeFrame, null);
        VariableInstance vinst = newFrame.resolve(_oforEach.counterVariable);

        try {
        initializeVariable(vinst, counterNode);
        } catch (ExternalVariableModuleException e) {
            __log.error("Exception while initializing external variable", e);
            _self.parent.failure(e.toString(), null);
            return;
        }

        // Generating event
        VariableModificationEvent se = new VariableModificationEvent(vinst.declaration.name);
        se.setNewValue(counterNode);
        if (_oforEach.debugInfo != null)
            se.setLineNo(_oforEach.debugInfo.startLine);
        sendEvent(se);

        instance(new SCOPE(child.activity, newFrame, _linkFrame));
    }

    public String toString() {
        return "<T:Act:Flow:" + _oforEach.name + ">";
    }

    private Iterator<ChildInfo> active() {
        return new FilterIterator<ChildInfo>(_children.iterator(), new MemberOfFunction<ChildInfo>() {
            public boolean isMember(ChildInfo childInfo) {
                return !childInfo.completed;
            }
        });
    }

}
