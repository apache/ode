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
package org.apache.ode.bpel.rtrep.common.extension;

import java.net.URI;
import java.util.Map;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.rtrep.v2.OActivity;
import org.apache.ode.bpel.rtrep.v2.OProcess;
import org.apache.ode.bpel.rtrep.v2.OScope;
import org.apache.ode.bpel.rtrep.v2.OdeInternalInstance;
import org.w3c.dom.Node;


/**
 * Context for executing extension activities or extension assign operations. 
 * Implementations of the {@link org.apache.ode.bpel.extension.ExtensionOperation} class 
 * use this interface to access BPEL variables, property sets and link status.
 * 
 * All <code>ExtensionOperation</code> implementations must complete with
 * <code>complete()</code>, <code>completeWithFault(...)</code>.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public interface ExtensionContext {

	/**
	 * Returns a list of variables visible in the current scope.
	 * 
	 * @return an unmodifiable list of visible variables.
	 * @throws FaultException
	 */
	Map<String, OScope.Variable> getVisibleVariables() throws FaultException;
	
	/**
	 * Returns whether a variable is visible in the current scope or not.
	 *
	 * @param variableName name of the variable.
	 * @return true if the variable is visible.
	 * @throws FaultException
	 */
	boolean isVariableVisible(String variableName);
	
	/**
     * Read the value of a BPEL variable.
     *
     * @param variable
     *          variable to read
     * @return the value of the variable, wrapped in a <code>Node</code>
     */
    Node readVariable(OScope.Variable variable) throws FaultException;
    
	/**
     * Read the value of a BPEL variable.
     *
     * @param variableName
     *          variable to read
     * @return the value of the variable, wrapped in a <code>Node</code>
     */
    Node readVariable(String variableName) throws FaultException;

    /**
     * Write the value into a BPEL variable.
     *
     * @param variable
     *          variable to write
     * @param value
     *          the value to be stored into the variable
     * @return the value of the variable, wrapped in a <code>Node</code>
     */
    void writeVariable(OScope.Variable variable, Node value) throws FaultException, ExternalVariableModuleException;

    /**
     * Write the value into a BPEL variable.
     *
     * @param variableName
     *          variable to write
     * @param value
     *          the value to be stored into the variable
     * @return the value of the variable, wrapped in a <code>Node</code>
     */
    void writeVariable(String variableName, Node value) throws FaultException, ExternalVariableModuleException;

    /**
     * Read the value of a BPEL property.
     *
     * @param variable
     *          variable containing property
     * @param property
     *          property to read
     * @return value of the property
     */
    String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
            throws FaultException;
    
    /**
     * Reads the current process instance id.
     * @return instance id
     */
    Long getProcessId();
    
    /**
     * Returns the name of the invoking activity.
     * @return activity name
     */
    String getActivityName();
    
    /**
     * Returns the location of the deployment bundle of the executed process.
     * @return URI of the deployment bundle.
     */
    URI getDUDir();
    
	/**
	 * Allows printing debug output to the console. Output will be redirected
	 * to the logger associated with <code>org.apache.ode.extension.Console</code>.
	 * The target log level is INFO.
	 */
    void printToConsole(String msg);
    
    /**
     * Marks the currently executed activity as successfully completed.
     */
    void complete(String cid);
    
    /**
     * Marks the currently executed activity as faulted.
     * @param t an exception to be reported as the fault cause.
     */
    void completeWithFault(String cid, Throwable t);

    /**
     * Marks the currently executed activity as faulted.
     * @param fault a fault.
     */
    void completeWithFault(String cid, FaultException fault);

    /*
     * Low-level-methods
     */
    
    /**
     * Returns the OActivity object. 
     */
    OActivity getOActivity();
    
    /**
     * Returns ODE's internal runtime instance. 
     */
    OdeInternalInstance getInternalInstance();
    
}
