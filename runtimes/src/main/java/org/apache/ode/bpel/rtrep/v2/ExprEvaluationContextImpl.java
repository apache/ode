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
import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.VariableReadEvent;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * The context in which BPEL expressions are evaluated. This class is handed of the OExpression
 * instances to provide access to variables, link statuses, and the like.
 */
public class ExprEvaluationContextImpl implements EvaluationContext {
    private static final Log __log = LogFactory.getLog(ExprEvaluationContextImpl.class);

	private OdeInternalInstance _native;

	private ScopeFrame _scopeInstance;

	private Map<OLink, Boolean> _linkVals;

	private Node _root;

    public ExprEvaluationContextImpl(ScopeFrame scopeInstace, OdeInternalInstance ntv) {
        _native = ntv;
        _scopeInstance = scopeInstace;
    }

    public ExprEvaluationContextImpl(ScopeFrame scopeInstace, OdeInternalInstance ntv, Node root) {
        this(scopeInstace, ntv);
        _root = root;
    }

    public ExprEvaluationContextImpl(ScopeFrame scopeInstnce, OdeInternalInstance ntv, Map<OLink, Boolean> linkVals) {
        this(scopeInstnce, ntv);
        _linkVals = linkVals;
    }

    public Node readVariable(OScope.Variable variable, OMessageVarType.Part part) throws FaultException {
        if (__log.isTraceEnabled())
            __log.trace("readVariable(" + variable + "," + part + ")");

		// TODO: check for null _scopeInstance

        Node ret;
        if (variable.type instanceof OConstantVarType) {
            ret = ((OConstantVarType) variable.type).getValue();
        } else if (variable.type instanceof OPropertyVarType) {
            CorrelationSetInstance set = _scopeInstance.resolve(variable.name);
            CorrelationKey key = _native.readCorrelation(set);
            if (key == null) return null;
            String keyValue = key.getValues()[0];
            Document doc = DOMUtils.newDocument();
            ret = doc.createTextNode(keyValue);
        } else {
            VariableInstance varInstance = _scopeInstance.resolve(variable);
            if (varInstance == null)
                return null;
            VariableReadEvent vre = new VariableReadEvent();
            vre.setVarName(varInstance.declaration.name);
            sendEvent(vre);
            ret = _native.fetchVariableData(varInstance, _scopeInstance, part, false);
        }
        return ret;
    }

	public Node evaluateQuery(Node root, OExpression expr) throws FaultException {
        return _native.getExpLangRuntime().evaluateNode(expr, new ExprEvaluationContextImpl(_scopeInstance, _native, root));
    }

    public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property) throws FaultException {
        VariableInstance varInstance = _scopeInstance.resolve(variable);
        return _native.readProperty(varInstance, property);
    }

	public boolean isLinkActive(OLink olink) throws FaultException {
		return _linkVals.get(olink);
	}

    public String toString() {
        return "{ExprEvaluationContextImpl scopeInstance=" + _scopeInstance + ", activeLinks=" + _linkVals + "}";
    }

	public Node getRootNode() {
		return _root;
	}

	public Node getPartData(Element message, OMessageVarType.Part part) throws FaultException {
		return _native.getPartData(message, part);
	}

	public Long getProcessId() {
		return _native.getPid();
	}

	public boolean narrowTypes() {
		return true;
	}

	private void sendEvent(ScopeEvent se) {
		_scopeInstance.fillEventInfo(se);
		_native.sendEvent(se);
	}

    public URI getBaseResourceURI() {
        return _native.getBaseResourceURI();
    }

	public Node getPropertyValue(QName propertyName) {
		return _native.getProcessProperty(propertyName);
	}

}
