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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.runtime.BpelRuntimeContext.ValueReferencePair;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evar.IncompleteKeyException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.namespace.QName;


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

    /** Constructor used to create "fault" scopes. */
    ScopeFrame( OScope scopeDef,
                Long scopeInstanceId,
                ScopeFrame parent,
                Set<CompensationHandler> visibleCompensationHandlers,
                FaultData fault) {
        this(scopeDef,scopeInstanceId,parent,visibleCompensationHandlers);
        _faultData = fault;

    }

    public ScopeFrame( OScope scopeDef,
                       Long scopeInstanceId,
                       ScopeFrame parent,
                       Set<CompensationHandler> visibleCompensationHandlers) {
        this.oscope = scopeDef;
        this.scopeInstanceId = scopeInstanceId;
        this.parent = parent;
        this.availableCompensations = visibleCompensationHandlers;
    }


    public ScopeFrame find(OScope scope) {
        if (oscope.name.equals(scope.name)) {
            return this;
        }

        return (parent != null)
                ? parent.find(scope)
                : null;
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


    //
    // Move all the external variable stuff in here so that it can be used by the expr-lang evaluation
    // context.
    // 

    Node fetchVariableData(BpelRuntimeContext brc, VariableInstance variable, boolean forWriting)
            throws FaultException
    {
        if (variable.declaration.extVar != null) {
            // Note, that when using external variables, the database will not contain the value of the
            // variable, instead we need to go the external variable subsystems.
            Element reference = (Element) fetchVariableData(brc, resolve(variable.declaration.extVar.related), false);
            try {
                Node ret = brc.readExtVar(variable.declaration, reference );
                if (ret == null) {
                    throw new FaultException(oscope.getOwner().constants.qnUninitializedVariable,
                            "The external variable \"" + variable.declaration.name + "\" has not been initialized.");
                }
                return ret;
            } catch (IncompleteKeyException ike) {
                // This indicates that the external variable needed to be written do, put has not been.
                __log.error("External variable could not be read due to incomplete key; the following key " +
                        "components were missing: " + ike.getMissing());
                throw new FaultException(oscope.getOwner().constants.qnUninitializedVariable,
                        "The extenral variable \"" + variable.declaration.name + "\" has not been properly initialized;" +
                                "the following key compoenents were missing:" + ike.getMissing());
            } catch (ExternalVariableModuleException e) {
                throw new BpelEngineException(e);
            }
        } else /* not external */ {
            Node data = brc.readVariable(variable.scopeInstance,variable.declaration.name, forWriting);
            if (data == null) {
                // Special case of messageType variables with no part
                if (variable.declaration.type instanceof OMessageVarType) {
                    OMessageVarType msgType = (OMessageVarType) variable.declaration.type;
                    if (msgType.parts.size() == 0) {
                        Document doc = DOMUtils.newDocument();
                        Element root = doc.createElement("message");
                        doc.appendChild(root);
                        return root;
                    }
                }
                throw new FaultException(oscope.getOwner().constants.qnUninitializedVariable,
                        "The variable " + variable.declaration.name + " isn't properly initialized.");
            }
            return data;
        }
    }


    Node fetchVariableData(BpelRuntimeContext brc, VariableInstance var, OMessageVarType.Part part, boolean forWriting)
            throws FaultException {
        Node container = fetchVariableData(brc, var, forWriting);

        // If we want a specific part, we will need to navigate through the
        // message/part structure
        if (var.declaration.type instanceof OMessageVarType && part != null) {
            container = getPartData((Element) container, part);
        }
        return container;
    }


    Node initializeVariable(BpelRuntimeContext context, VariableInstance var, Node value)
            throws ExternalVariableModuleException {
        if (var.declaration.extVar != null) /* external variable */ {
            if (__log.isDebugEnabled())
                __log.debug("Initialize external variable: name="+var.declaration + " value="+DOMUtils.domToString(value));
            VariableInstance related = resolve(var.declaration.extVar.related);
            Node reference = null;
            try {
                reference = fetchVariableData(context, related, true);
            } catch (FaultException fe) {
                // In this context this is not necessarily a problem, since the assignment may re-init the related var
            }
            if (reference != null) value = context.readExtVar(var.declaration, reference);

            return value;
        } else /* normal variable */ {
            if (__log.isDebugEnabled()) __log.debug("Initialize variable: name="+var.declaration + " value="+DOMUtils.domToString(value));
            return context.writeVariable(var, value);
        }
    }


    Node commitChanges(BpelRuntimeContext context, VariableInstance var, Node value) throws ExternalVariableModuleException {
        return writeVariable(context, var, value);
    }


    Node writeVariable(BpelRuntimeContext context, VariableInstance var, Node value) throws ExternalVariableModuleException {
        if (var.declaration.extVar != null) /* external variable */ {
        	if(__log.isDebugEnabled())
        		__log.debug("Write external variable: name="+var.declaration + " value="+DOMUtils.domToString(value));
            VariableInstance related = resolve(var.declaration.extVar.related);
            Node reference = null;
            try {
                reference = fetchVariableData(context, related, true);
            } catch (FaultException fe) {
                // In this context this is not necessarily a problem, since the assignment may re-init the related var
            }

            ValueReferencePair vrp  = context.writeExtVar(var.declaration, reference, value);
            writeVariable(context, related, vrp.reference);
            return vrp.value;
        } else /* normal variable */ {
        	if(__log.isDebugEnabled())
        		__log.debug("Write variable: name="+var.declaration + " value="+DOMUtils.domToString(value));
            return context.writeVariable(var, value);
        }
    }


    Node getPartData(Element message, Part part) {
        // borrowed from ASSIGN.evalQuery()
        QName partName = new QName(null, part.name);
        Node ret = DOMUtils.findChildByName(message, partName);
        if (part.type instanceof OElementVarType) {
            QName elName = ((OElementVarType) part.type).elementType;
            ret = DOMUtils.findChildByName((Element) ret, elName);
        }
        return ret;
    }

}
