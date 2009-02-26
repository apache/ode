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

import org.apache.ode.bpel.evt.EventContext;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Node;

import java.util.Iterator;

/**
 * Bpel Event Context Implementation.
 * @author Raja Balasubramanian
 */
public class EventContextImpl implements EventContext
{
    /**
     * Scope Object used in this scope instance.
     */
    private OScope __scope;
    /**
     * Scope Instance ID
     */
    private Long __scopeInstanceId;
    /**
     * BPEL Runtime Context
     */
    private BpelRuntimeContext __runtimeContext;

    /**
     * Constructor
     * @param __scope Scope Object used in this scope instance.
     * @param __scopeInstanceId Scope Instance ID
     * @param __runtimeContext BPEL Runtime Context
     */
    public EventContextImpl(OScope __scope, Long __scopeInstanceId, BpelRuntimeContext __runtimeContext)
    {
        this.__scope = __scope;
        this.__scopeInstanceId = __scopeInstanceId;
        this.__runtimeContext = __runtimeContext;
    }

    /**
     * Get Variable data for the given variable name, for this scope instance
     * @param varName Variable Name
     * @return DOM Node as XML String. If no value exists or variable not initialized, NULL will be returnrd.
     */
    public String getVariableData(String varName)
    {
        String value = null;
        try
        {
            Variable var = __scope.getVisibleVariable(varName);
            VariableInstance varInstance = new VariableInstance(__scopeInstanceId, var);
            Node varNode = __runtimeContext.readVariable(varInstance.scopeInstance, varInstance.declaration.name, false);
            value = DOMUtils.domToString(varNode);
        }
        catch(Throwable e)
        {
            //Don't throw any exception back to the caller. Just return null as value.
        }
        return value;
    }

    /**
     * Get All variable names used in this scope Instance
     * @return Array of Variable Names. If no variable(s) exists, null will be returned.
     */
    public String[] getVariableNames()
    {
        String[] __varNames = null;
        try
        {
            int varSize = __scope.variables.size();
            __varNames = new String[varSize];

            Iterator<String> _varNames = __scope.variables.keySet().iterator();
            int i = 0;
            while(_varNames.hasNext())
            {
                __varNames[i++] = _varNames.next();
            }
        }
        catch(Throwable e)
        {
            //Don't throw any exception back to the caller. Just return null as value.
        }
        return __varNames;
    }

    /**
     * Get ScopeInstanceId
     * @return scopeInstanceId
     */
    public Long getScopeInstanceId()
    {
        return __scopeInstanceId;
    }
}
