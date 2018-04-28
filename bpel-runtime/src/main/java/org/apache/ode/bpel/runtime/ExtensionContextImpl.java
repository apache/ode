/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.runtime;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.bom.Bpel20QNames;
import org.apache.ode.bpel.eapi.ExtensionContext;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.obj.OActivity;
import org.apache.ode.bpel.obj.OLink;
import org.apache.ode.bpel.obj.OProcess.OProperty;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.obj.OScope.Variable;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensionContextImpl implements ExtensionContext {

    private static final Logger __log = LoggerFactory.getLogger(ExtensionContextImpl.class);

    private BpelRuntimeContext _context;
    private ScopeFrame _scopeFrame;
    private ActivityInfo _activityInfo;

    private boolean hasCompleted = false;

    public ExtensionContextImpl(ActivityInfo activityInfo, ScopeFrame scopeFrame,
            BpelRuntimeContext context) {
        _activityInfo = activityInfo;
        _context = context;
        _scopeFrame = scopeFrame;
    }

    public List<OLink> getLinks() throws FaultException {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getProcessId() {
        return _context.getPid();
    }

    public String getActivityName() {
        return _activityInfo.o.getName();
    }

    public OActivity getOActivity() {
        return _activityInfo.o;
    }

    public Map<String, OScope.Variable> getVisibleVariables() throws FaultException {
        Map<String, OScope.Variable> visVars = new HashMap<String, OScope.Variable>();

        OActivity current = _scopeFrame.oscope;
        while (current != null) {
            if (current instanceof OScope) {
                for (String varName : ((OScope) current).getVariables().keySet()) {
                    if (!visVars.containsKey(varName)) {
                        visVars.put(varName, ((OScope) current).getVariables().get(varName));
                    }
                }
            }
            current = current.getParent();
        }

        return visVars;
    }

    public String readMessageProperty(Variable variable, OProperty property) throws FaultException {
        VariableInstance vi = _scopeFrame.resolve(variable);
        return _context.readProperty(vi, property);
    }

    public Node readVariable(OScope.Variable variable) throws FaultException {
        VariableInstance vi = _scopeFrame.resolve(variable);
        return _context.readVariable(vi.scopeInstance, vi.declaration.getName(), true);
    }

    public void writeVariable(String variableName, Node value) throws FaultException {
        VariableInstance vi = _scopeFrame.resolve(getVisibleVariable(variableName));
        _context.writeVariable(vi, value);

        VariableModificationEvent vme = new VariableModificationEvent(variableName);
        vme.setNewValue(value);
        sendEvent(vme);
    }

    public Node readVariable(String variableName) throws FaultException {
        VariableInstance vi = _scopeFrame.resolve(getVisibleVariable(variableName));
        return _context.readVariable(vi.scopeInstance, vi.declaration.getName(), true);
    }

    public void writeVariable(Variable variable, Node value) throws FaultException {
        VariableInstance vi = _scopeFrame.resolve(variable);
        _context.writeVariable(vi, value);
    }

    private Variable getVisibleVariable(String varName) {
        return _scopeFrame.oscope.getVisibleVariable(varName);
    }

    public BpelRuntimeContext getBpelRuntimeContext() {
        return _context;
    }

    public void sendEvent(ScopeEvent event) {
        if (event.getLineNo() == -1 && _activityInfo.o.getDebugInfo() != null) {
            event.setLineNo(_activityInfo.o.getDebugInfo().getStartLine());
        }
        _scopeFrame.fillEventInfo(event);
        getBpelRuntimeContext().sendEvent(event);
    }

    public void complete() {
        if (!hasCompleted) {
            _activityInfo.parent.completed(null, CompensationHandler.emptySet());
            hasCompleted = true;
        } else {
            if (__log.isWarnEnabled()) {
                __log.warn(
                        "Activity '" + _activityInfo.o.getName() + "' has already been completed.");
            }
        }
    }

    public void completeWithFault(Throwable t) {
        if (!hasCompleted) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            FaultData fault =
                    new FaultData(new QName(Bpel20QNames.NS_WSBPEL2_0, "subLanguageExecutionFault"),
                            _activityInfo.o, sw.getBuffer().toString());
            _activityInfo.parent.completed(fault, CompensationHandler.emptySet());
            hasCompleted = true;
        } else {
            if (__log.isWarnEnabled()) {
                __log.warn(
                        "Activity '" + _activityInfo.o.getName() + "' has already been completed.");
            }
        }
    }

    public void completeWithFault(FaultException ex) {
        if (!hasCompleted) {
            FaultData fault = new FaultData(ex.getQName(), _activityInfo.o, ex.getMessage());
            _activityInfo.parent.completed(fault, CompensationHandler.emptySet());
            hasCompleted = true;
        } else {
            if (__log.isWarnEnabled()) {
                __log.warn(
                        "Activity '" + _activityInfo.o.getName() + "' has already been completed.");
            }
        }

    }
    
}
