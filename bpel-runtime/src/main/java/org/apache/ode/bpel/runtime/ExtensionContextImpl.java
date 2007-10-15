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

import java.util.HashMap;
import java.util.Map;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OProcess.OProperty;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.w3c.dom.Node;


/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensionContextImpl implements ExtensionContext {

	private BpelRuntimeContext _context;
	private ScopeFrame _scopeFrame;
	private OActivity _activity;

	public ExtensionContextImpl(OActivity activity, ScopeFrame scopeFrame, BpelRuntimeContext context) {
		_context = context;
		_scopeFrame = scopeFrame;
		_activity = activity;
	}
	
	public Long getProcessId() {
		return _context.getPid();
	}

	public Map<String, Variable> getVisibleVariables() throws FaultException {
		Map<String, Variable> visVars = new HashMap<String, Variable>();
		
        OActivity current = _scopeFrame.oscope;
        while (current != null) {
            if (current instanceof OScope) {
                for (String varName : ((OScope)current).variables.keySet()) {
                	if (!visVars.containsKey(varName)) {
                		visVars.put(varName, ((OScope)current).variables.get(varName));
                	}
                }
            }
            current = current.getParent();
        }
		
		return visVars;
	}

	public boolean isLinkActive(OLink olink) throws FaultException {
		// TODO Auto-generated method stub
		return false;
	}

	public String readMessageProperty(Variable variable, OProperty property)
			throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(variable);
		return _context.readProperty(vi, property);
	}

	public Node readVariable(Variable variable)
			throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(variable);
		return _context.fetchVariableData(vi, true);
	}

	public void writeVariable(String variableName, Node value)
			throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(getVisibleVariable(variableName));
		_context.commitChanges(vi, value);
	}

	public Node readVariable(String variableName) throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(getVisibleVariable(variableName));
		return _context.fetchVariableData(vi, true);
	}

	public void writeVariable(Variable variable, Node value)
			throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(variable);
		_context.commitChanges(vi, value);
	}

	private Variable getVisibleVariable(String varName) {
    	return _scopeFrame.oscope.getVisibleVariable(varName);
    }

	public String getActivityName() {
		return _activity.name;
	}

	public OActivity getOActivity() {
		return _activity;
	}

	public BpelRuntimeContext getBpelRuntimeContext() {
		return _context;
	}
}
