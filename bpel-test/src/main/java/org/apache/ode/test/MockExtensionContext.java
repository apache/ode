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
package org.apache.ode.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.apache.ode.bpel.rtrep.v2.OActivity;
import org.apache.ode.bpel.rtrep.v2.OLink;
import org.apache.ode.bpel.rtrep.v2.OProcess;
import org.apache.ode.bpel.rtrep.v2.OScope;
import org.apache.ode.bpel.rtrep.v2.OdeInternalInstance;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Node;

/**
 * Very simple mock implementation of the ExtensionContext interface.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class MockExtensionContext implements ExtensionContext {
	private Map<String, Node> variables = new HashMap<String, Node>();
	public boolean completed;
	public boolean faulted;
	public FaultException fault;
	public URI duDir;
	public List<String> msgs = new ArrayList<String>();
	
	public Map<String, Node> getVariables() {
		return variables;
	}
	
	public Long getProcessId() {
		return 4711L;
	}

	public Node readVariable(String variableName) throws FaultException {
		System.out.println("Reading " + variableName);
		return variables.get(variableName);
	}

	public void writeVariable(String variableName, Node value)
			throws FaultException {
		variables.put(variableName, value);
		System.out.println("Storing in " + variableName + ": " + DOMUtils.domToString(value));
	}

	public boolean isVariableVisible(String varName) {
		return variables.containsKey(varName);
	}

	public String getActivityName() {
		return "mockActivity";
	}
	
	public OActivity getOActivity() {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}
	
	public Map<String, OScope.Variable> getVisibleVariables()
		throws FaultException {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}
	
	public boolean isLinkActive(OLink olink) throws FaultException {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}
	
	public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
		throws FaultException {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}
	
	public Node readVariable(OScope.Variable variable) throws FaultException {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}
	
	public void writeVariable(OScope.Variable variable, Node value) throws FaultException {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}

	public void complete(String cid) {
		this.completed = true;
	}

	public void completeWithFault(String cid, Throwable t) {
		this.completed = true;
		this.faulted = true;
	}

	public void completeWithFault(String cid, FaultException fault) {
		this.completed = true;
		this.faulted = true;
		this.fault = fault;
	}

	public OdeInternalInstance getInternalInstance() {
		throw new UnsupportedOperationException("This method is not available in this mock implementation.");
	}

	public URI getDUDir() {
		return duDir;
	}

	public void printToConsole(String msg) {
		System.out.println(msg);
		msgs.add(msg);
	}
	
}