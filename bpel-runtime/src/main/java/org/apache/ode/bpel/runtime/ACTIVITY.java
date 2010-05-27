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
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ActivityEvent;
import org.apache.ode.bpel.evt.EventContext;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.VariableReadEvent;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OConstants;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.jacob.IndexedObject;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Base template for activities.
 */
abstract class ACTIVITY extends BpelJacobRunnable implements IndexedObject {
    private static final Log __log = LogFactory.getLog(ACTIVITY.class);
    protected ActivityInfo _self;

    /**
     * Permeability flag, if <code>false</code> we defer outgoing links until successful completion.
     */
    protected boolean _permeable = true;

    protected ScopeFrame _scopeFrame;

    protected LinkFrame _linkFrame;

    public ACTIVITY(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        assert self != null;
        assert scopeFrame != null;
        assert linkFrame != null;

        _self = self;
        _scopeFrame = scopeFrame;
        _linkFrame = linkFrame;
    }

    public Object getKey() {
        return new Key(_self.o,_self.aId);
    }


    protected void sendVariableReadEvent(VariableInstance var) {
        VariableReadEvent vre = new VariableReadEvent();
        vre.setVarName(var.declaration.name);
        sendEvent(vre);
    }

    protected void sendEvent(ActivityEvent event) {
        event.setActivityName(_self.o.name);
        event.setActivityType(_self.o.getType());
        event.setActivityDeclarationId(_self.o.getId());
        event.setActivityId(_self.aId);
        if (event.getLineNo() == -1) {
            event.setLineNo(getLineNo());
        }
        sendEvent((ScopeEvent) event);
    }

    protected void sendEvent(ScopeEvent event) {
        if (event.getLineNo() == -1 && _self.o.debugInfo != null) {
            event.setLineNo(_self.o.debugInfo.startLine);
        }
        _scopeFrame.fillEventInfo(event);
        fillEventContext(event);
        getBpelRuntimeContext().sendEvent(event);
    }

    /**
     * Populate BpelEventContext, to be used by Registered Event Listeners
     * @param event ScopeEvent
     */
    protected void fillEventContext(ScopeEvent event)
    {
        EventContext eventContext = new EventContextImpl(_scopeFrame.oscope,
                                                            _scopeFrame.scopeInstanceId,
                                                            getBpelRuntimeContext());
        event.eventContext = eventContext;
    }

    protected void dpe(Collection<OLink> links) {
        // Dead path all of the outgoing links (nothing has been activated yet!)
        for (OLink link : links) {
            if (__log.isDebugEnabled()) __log.debug("DPE on link " + link.name);
            _linkFrame.resolve(link).pub.linkStatus(false);
        }
    }

    protected OConstants getConstants() {
        return _self.o.getOwner().constants;
    }

    /**
     * Perform dead-path elimination on an activity that was
     * <em>not started</em>.
     *
     * @param activity
     */
    protected void dpe(OActivity activity) {
        dpe(activity.sourceLinks);
        dpe(activity.outgoingLinks);
        // TODO: register listeners for target / incoming links
    }

    protected EvaluationContext getEvaluationContext() {
        return new ExprEvaluationContextImpl(_scopeFrame, getBpelRuntimeContext());
    }

    private int getLineNo() {
        if (_self.o.debugInfo != null && _self.o.debugInfo.startLine != -1) {
            return _self.o.debugInfo.startLine;
        }
        return -1;
    }

    //
    // Syntactic sugar for methods that used to be on BpelRuntimeContext..
    //

    Node fetchVariableData(VariableInstance variable, boolean forWriting)
        throws FaultException
    {
        return _scopeFrame.fetchVariableData(getBpelRuntimeContext(), variable, forWriting);
    }

    Node fetchVariableData(VariableInstance var, OMessageVarType.Part part, boolean forWriting)
        throws FaultException
    {
      return _scopeFrame.fetchVariableData(getBpelRuntimeContext(), var, part, forWriting);
    }

    Node initializeVariable(VariableInstance lvar, Node val)
        throws ExternalVariableModuleException
    {
        return _scopeFrame.initializeVariable(getBpelRuntimeContext(), lvar, val);
    }

    void commitChanges(VariableInstance lval, Node lvalue)
        throws ExternalVariableModuleException
    {
        _scopeFrame.commitChanges(getBpelRuntimeContext(),lval, lvalue);
    }

    Node getPartData(Element message, Part part) {
        return _scopeFrame.getPartData(message, part);
    }


    //
    // End syntactic sugar.
    //

    public static final class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        final OActivity type;

        final long aid;

        public Key(OActivity type, long aid) {
            this.type = type;
            this.aid = aid;
        }

        @Override
        public String toString() {
            return type + "::" + aid;
        }
    }
}
