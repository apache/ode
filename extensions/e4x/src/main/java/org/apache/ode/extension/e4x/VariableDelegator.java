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
package org.apache.ode.extension.e4x;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Delegator;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XMLLibImpl;
import org.w3c.dom.Node;

/**
 * <code>VariableDelegator</code> is in charge of inserting BPEL variables
 * into the JS/E4X context.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class VariableDelegator extends Delegator {
	private static final Log __logger = LogFactory.getLog(VariableDelegator.class);
	
	private ExtensionContext _ectx;
	private Context _sctx;
	private Map<String, Object> _env = new HashMap<String, Object>();
	private Set<String> _variables = new HashSet<String>();

	public VariableDelegator(Scriptable obj, ExtensionContext ectx, Context sctx) {
		super(obj);
		_ectx = ectx;
		_sctx = sctx;
	}
	
	public Object get(String name, Scriptable start) {
        // do not override variables defined in JS
		if (super.has(name, start)) {
        	return super.get(name, start);
        }
        
		if (_env.get(name) != null) {
	        if (__logger.isDebugEnabled()) {
	        	__logger.debug("Reading JS variable '" + name + "'.");
	        }

			return _env.get(name);
		}
		
		try {
	        if (!_ectx.isVariableVisible(name)) { 
	        	return ScriptableObject.NOT_FOUND;
	        }
	        if (__logger.isDebugEnabled()) {
	        	__logger.debug("Reading BPEL variable '" + name + "'.");
	        }
			Node n = _ectx.readVariable(name);
			XMLObject xmlObj = (XMLObject)_sctx.newObject(start, "XML", new Object[] {Context.javaToJS(TopLevelFunctions.domToString(n), start)});
			_env.put(name, xmlObj);
			if (!_variables.contains(name)) {
				_variables.add(name);
			}
			return xmlObj;
		} catch (Exception e) {
			throw new RuntimeException("Error accessing variable " + name + ".", e);
		}
	}

	public boolean has(String name, Scriptable start) {
		if (super.has(name, start) || _env.containsKey(name)) {
        	return true;
        }
        
		return (_ectx.isVariableVisible(name));
	}

	public void put(String name, Scriptable start, Object value) {
        if (__logger.isDebugEnabled()) {
        	__logger.debug("Setting JS variable '" + name + "' to '" + value + "'.");
        }

		_env.put(name, value);
		if (_ectx.isVariableVisible(name) && !_variables.contains(name)) {
			_variables.add(name);
		}
	}

	/*
	 * This is needed because we're actually building a nested scope and not 
	 * purely delegating. Not sure if there's a better solution.
	 */
	public Scriptable getParentScope() {
		return obj;
	}

	public void writeVariables() throws FaultException, ExternalVariableModuleException {
		for (String varName : _variables) {
	        if (__logger.isDebugEnabled()) {
	        	__logger.debug("Writing BPEL variable '" + varName + "' to '" + _env.get(varName) + "'.");
	        }
	        //TODO: only changed variables should be written. Its currently not that easy
	        //      to detect whether the content has changed without recursively injecting
	        //      emcaPut implementations.
			_ectx.writeVariable(varName, XMLLibImpl.toDomNode(_env.get(varName)));
		}
	}
}
