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
package org.apache.ode.bpel.rtrep.v2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.rtrep.v2.channels.FaultData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * N-tuple representing a scope "frame" (as in stack frame).
 */
class ScopeFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log __log = LogFactory.getLog(ScopeFrame.class);

    /** The compiled scope representation. */
    final OScope oscope;

    /** The parent scope frame. */
    final ScopeFrame parent;

    /** Database scope instance identifier. */
    final Long scopeInstanceId;

    Set<CompensationHandler> availableCompensations;

    /** The fault context for this scope. */
    private FaultData _faultData;
    
    final InstanceGlobals globals;

    /** Constructor used to create "fault" scopes. */
    ScopeFrame( OScope scopeDef,
                Long scopeInstanceId,
                ScopeFrame parent,
                Set<CompensationHandler> visibleCompensationHandlers,
                FaultData fault) {
        this(scopeDef,scopeInstanceId,parent,visibleCompensationHandlers, parent.globals);
        _faultData = fault;

    }
    public ScopeFrame( OScope scopeDef,
            Long scopeInstanceId,
            ScopeFrame parent,
            Set<CompensationHandler> visibleCompensationHandlers) {
        this(scopeDef,scopeInstanceId,parent,visibleCompensationHandlers,parent.globals);
    }
    
    public ScopeFrame( OScope scopeDef,
                       Long scopeInstanceId,
                       ScopeFrame parent,
                       Set<CompensationHandler> visibleCompensationHandlers,
                       InstanceGlobals globals) {
        this.oscope = scopeDef;
        this.scopeInstanceId = scopeInstanceId;
        this.parent = parent;
        this.availableCompensations = visibleCompensationHandlers;
        this.globals = globals;
    }


    public ScopeFrame find(OScope scope) {
        if (oscope.name.equals(scope.name)) return this;
        return (parent != null) ? parent.find(scope) : null;
    }

    public VariableInstance resolve(OScope.Variable variable) {
        ScopeFrame scopeFrame = find(variable.declaringScope);
        if (scopeFrame == null) return null;
        return new VariableInstance(scopeFrame.scopeInstanceId, variable);
    }

    public CorrelationSetInstance resolve(OScope.CorrelationSet cset) {
        return new CorrelationSetInstance(find(cset.declaringScope).scopeInstanceId, cset);
    }

    public PartnerLinkInstance resolve(OPartnerLink partnerLink) {
        return new PartnerLinkInstance(find(partnerLink.declaringScope).scopeInstanceId, partnerLink);
    }

    public String toString() {
        StringBuffer buf= new StringBuffer("{ScopeFrame: o=");
        buf.append(oscope);
        buf.append(", id=");
        buf.append(scopeInstanceId);
        if (availableCompensations != null) {
            buf.append(", avComps=");
            buf.append(availableCompensations);
        }
        if (_faultData != null) {
            buf.append(", fault=");
            buf.append(_faultData);
        }
        buf.append('}');
        return buf.toString();
    }

    public FaultData getFault() {
        if (_faultData != null)
            return _faultData;
        if (parent != null)
            return parent.getFault();
        return null;
    }

    public void fillEventInfo(ScopeEvent event) {
        ScopeFrame currentScope = this;
        ArrayList<String> parentNames = new ArrayList<String>();
        while (currentScope != null) {
            parentNames.add(currentScope.oscope.name);
            currentScope = currentScope.parent;
        }
        event.setParentScopesNames(parentNames);
        if (parent != null)
            event.setParentScopeId(parent.scopeInstanceId);
        event.setScopeId(scopeInstanceId);
        event.setScopeName(oscope.name);
        event.setScopeDeclerationId(oscope.getId());
        if (event.getLineNo() == -1 && oscope.debugInfo !=  null)
            event.setLineNo(oscope.debugInfo.startLine);
    }
}
