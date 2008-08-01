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

import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.w3c.dom.Node;


/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensionContextImpl implements ExtensionContext {
	private static final Log __log = LogFactory.getLog(ExtensionContextImpl.class);
	
	private RuntimeInstanceImpl _context;
	private ScopeFrame _scopeFrame;
	private ActivityInfo _activityInfo;
	
	public ExtensionContextImpl(ActivityInfo activityInfo, ScopeFrame scopeFrame, RuntimeInstanceImpl context) {
		_activityInfo = activityInfo;
		_context = context;
		_scopeFrame = scopeFrame;
	}
	
	public Long getProcessId() {
		return _context.getPid();
	}

	public Map<String, OScope.Variable> getVisibleVariables() throws FaultException {
		Map<String, OScope.Variable> visVars = new HashMap<String, OScope.Variable>();
		
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

	public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
			throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(variable);
		return _context.readProperty(vi, property);
	}

	public Node readVariable(OScope.Variable variable)
			throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(variable);
		return _context.fetchVariableData(vi, true);
	}

	public void writeVariable(String variableName, Node value)
			throws FaultException, ExternalVariableModuleException {
		VariableInstance vi = _scopeFrame.resolve(getVisibleVariable(variableName));
		_context.commitChanges(vi, value);
	}

	public Node readVariable(String variableName) throws FaultException {
		VariableInstance vi = _scopeFrame.resolve(getVisibleVariable(variableName));
		return _context.fetchVariableData(vi, true);
	}

	public void writeVariable(OScope.Variable variable, Node value)
			throws FaultException, ExternalVariableModuleException {
        VariableInstance vi = _scopeFrame.resolve(variable);
        _context.commitChanges(vi, value);
	}

	private OScope.Variable getVisibleVariable(String varName) {
    	return _scopeFrame.oscope.getVisibleVariable(varName);
    }

	public String getActivityName() {
		return _activityInfo.o.name;
	}

	public OActivity getOActivity() {
		return _activityInfo.o;
	}

}
