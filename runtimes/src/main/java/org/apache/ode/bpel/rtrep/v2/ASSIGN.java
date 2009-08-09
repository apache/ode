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

import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evt.CorrelationSetWriteEvent;
import org.apache.ode.bpel.evt.PartnerLinkModificationEvent;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.VariableModificationEvent;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.rtrep.common.extension.ExtensibilityQNames;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.apache.ode.bpel.rtrep.v2.channels.FaultData;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Assign activity run-time template.
 *
 * @author Ode team
 * @author Tammo van Lessen (University of Stuttgart) - extensionAssignOperation
 */
class ASSIGN extends ACTIVITY {
    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(ASSIGN.class);
    
    public ASSIGN(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
    }

    public void run() {
        OAssign oassign = getOAssign();

        FaultData faultData = null;

        for (OAssign.OAssignOperation operation : oassign.operations) {
            try {
                if (operation instanceof OAssign.Copy) {
                    copy((OAssign.Copy)operation);
                } else if (operation instanceof OAssign.ExtensionAssignOperation) {
                    invokeExtensionAssignOperation((OAssign.ExtensionAssignOperation)operation);
                }
            } catch (FaultException fault) {
            	if (operation instanceof OAssign.Copy) {
            		if (((OAssign.Copy) operation).ignoreMissingFromData) {
            			if (fault.getQName().equals(getOAssign().getOwner().constants.qnSelectionFailure) &&
            					(fault.getCause() != null && "ignoreMissingFromData".equals(fault.getCause().getMessage()))) {
            				continue;
    					}
            		}
            		if (((OAssign.Copy) operation).ignoreUninitializedFromVariable) {
            			if (fault.getQName().equals(getOAssign().getOwner().constants.qnUninitializedVariable) &&
            					(fault.getCause() == null || !"throwUninitializedToVariable".equals(fault.getCause().getMessage()))) {
            				continue;
            			}
            		}
            	}
                faultData = createFault(fault.getQName(), operation, fault
                        .getMessage());
                break;
            } catch (ExternalVariableModuleException e) {
                __log.error("Exception while initializing external variable", e);
                _self.parent.failure(e.toString(), null);
                return;
            }
        }

        if (faultData != null) {
            __log.error("Assignment Fault: " + faultData.getFaultName()
                    + ",lineNo=" + faultData.getFaultLineNo()
                    + ",faultExplanation=" + faultData.getExplanation());
            _self.parent.completed(faultData, CompensationHandler.emptySet());
        } else {
            _self.parent.completed(null, CompensationHandler.emptySet());
        }
    }

    protected Log log() {
        return __log;
    }

    private OAssign getOAssign() {
        return (OAssign) _self.o;
    }

    private void copy(OAssign.Copy ocopy) throws FaultException, ExternalVariableModuleException {

        if (__log.isDebugEnabled())
            __log.debug("Assign.copy(" + ocopy + ")");

        ScopeEvent se;
        AssignHelper assignHelper = new AssignHelper(_self, _scopeFrame, _linkFrame);

        // Check for message to message - copy, we can do this efficiently in
        // the database.
        if ((ocopy.to instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.to)
                .isMessageRef())
                || (ocopy.from instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.from)
                .isMessageRef())) {

            if ((ocopy.to instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.to)
                    .isMessageRef())
                    && ocopy.from instanceof OAssign.VariableRef
                    && ((OAssign.VariableRef) ocopy.from).isMessageRef()) {

                final VariableInstance lval = _scopeFrame.resolve(ocopy.to
                        .getVariable());
                final VariableInstance rval = _scopeFrame
                        .resolve(((OAssign.VariableRef) ocopy.from).getVariable());
                Element lvalue = (Element) fetchVariableData(rval, false);
                initializeVariable(lval, lvalue);
                se = new VariableModificationEvent(lval.declaration.name);
                ((VariableModificationEvent)se).setNewValue(lvalue);
            } else {
                // This really should have been caught by the compiler.
                __log
                        .fatal("Message/Non-Message Assignment, should be caught by compiler:"
                                + ocopy);
                throw new FaultException(
                        ocopy.getOwner().constants.qnSelectionFailure,
                        "Message/Non-Message Assignment:  " + ocopy);
            }
        } else {
            // Conventional Assignment logic.
            Node rvalue = assignHelper.evalRValue(ocopy.from);
            Node lvalue = assignHelper.evalLValue(ocopy.to);
            if (__log.isDebugEnabled()) {
                __log.debug("lvalue after eval " + lvalue);
                if (lvalue != null) __log.debug("content " + DOMUtils.domToString(lvalue));
            }

            // Get a pointer within the lvalue.
            Node lvaluePtr = lvalue;
            boolean headerAssign = false;
            if (ocopy.to instanceof OAssign.DirectRef) {
                OAssign.DirectRef dref = ((OAssign.DirectRef) ocopy.to);
                Element el = DOMUtils.findChildByName((Element)lvalue, dref.elName);
                if (el == null) {
                    el = (Element) ((Element)lvalue).appendChild(lvalue.getOwnerDocument()
                            .createElementNS(dref.elName.getNamespaceURI(), dref.elName.getLocalPart()));
                }
                lvaluePtr = el;
            } else if (ocopy.to instanceof OAssign.VariableRef) {
                OAssign.VariableRef varRef = ((OAssign.VariableRef) ocopy.to);
                if (varRef.headerPart != null) headerAssign = true;
                lvaluePtr = evalQuery(lvalue, varRef.part != null ? varRef.part : varRef.headerPart, varRef.location,
                        new EvaluationContextProxy(varRef.getVariable(), lvalue));
            } else if (ocopy.to instanceof OAssign.PropertyRef) {
                OAssign.PropertyRef propRef = ((OAssign.PropertyRef) ocopy.to);
                lvaluePtr = evalQuery(lvalue, propRef.propertyAlias.part,
                        propRef.propertyAlias.location,
                        new EvaluationContextProxy(propRef.getVariable(), lvalue));
            } else if (ocopy.to instanceof OAssign.LValueExpression) {
                OAssign.LValueExpression lexpr = (OAssign.LValueExpression) ocopy.to;
	            lexpr.setInsertMissingToData(ocopy.insertMissingToData);
                lvaluePtr = evalQuery(lvalue, null, lexpr.expression,
                        new EvaluationContextProxy(lexpr.getVariable(), lvalue));
                if (__log.isDebugEnabled())
                    __log.debug("lvaluePtr expr res " + lvaluePtr);
            }

            // For partner link assignmenent, the whole content is assigned.
            if (ocopy.to instanceof OAssign.PartnerLinkRef) {
                OAssign.PartnerLinkRef pLinkRef = ((OAssign.PartnerLinkRef) ocopy.to);
                PartnerLinkInstance plval = _scopeFrame.resolve(pLinkRef.partnerLink);
                replaceEndpointRefence(plval, rvalue);
                se = new PartnerLinkModificationEvent(((OAssign.PartnerLinkRef) ocopy.to).partnerLink.getName());
            } else if (ocopy.to.getVariable().type instanceof OPropertyVarType) {
                // For poperty assignment, the property, the variable that points to it and the correlation set
                // all have the same name
                CorrelationSetInstance csetInstance = _scopeFrame.resolveCorrelation(ocopy.to.getVariable().name);
                CorrelationKey ckey = new CorrelationKey(csetInstance.declaration.getId(), new String[] { rvalue.getTextContent() });
                if (__log.isDebugEnabled()) __log.debug("Writing correlation " + csetInstance.getName()
                        + " using value " + rvalue.getTextContent());
                getBpelRuntime().writeCorrelation(csetInstance, ckey);
                se = new CorrelationSetWriteEvent(csetInstance.declaration.name, ckey);
            } else {
                // Sneakily converting the EPR if it's not the format expected by the lvalue
                if (ocopy.from instanceof OAssign.PartnerLinkRef) {
                    rvalue = getBpelRuntime().convertEndpointReference((Element)rvalue, lvaluePtr);
                    if (rvalue.getNodeType() == Node.DOCUMENT_NODE)
                        rvalue = ((Document)rvalue).getDocumentElement();
                }

                if (headerAssign && lvaluePtr.getParentNode().getNodeName().equals("message") && rvalue.getNodeType()==Node.ELEMENT_NODE) {
                    lvalue = copyInto((Element)lvalue, (Element) lvaluePtr, (Element) rvalue);
                } else if (rvalue.getNodeType() == Node.ELEMENT_NODE && lvaluePtr.getNodeType() == Node.ELEMENT_NODE) {
                    lvalue = assignHelper.replaceElement((Element)lvalue, (Element) lvaluePtr, (Element) rvalue,
                            ocopy.keepSrcElementName);
                } else {
                    lvalue = assignHelper.replaceContent(lvalue, lvaluePtr, rvalue.getTextContent());
                }
                final VariableInstance lval = _scopeFrame.resolve(ocopy.to.getVariable());
                if (__log.isDebugEnabled())
                    __log.debug("ASSIGN Writing variable '" + lval.declaration.name +
                            "' value '" + DOMUtils.domToString(lvalue) +"'");
                commitChanges(lval, lvalue);
                se = new VariableModificationEvent(lval.declaration.name);
                ((VariableModificationEvent)se).setNewValue(lvalue);
            }
        }

        if (ocopy.debugInfo != null)
            se.setLineNo(ocopy.debugInfo.startLine);
        sendEvent(se);
    }


   	@Override
 	  Node fetchVariableData(VariableInstance variable, boolean forWriting)
 		  	    throws FaultException {
 		    try {
 		  	    return super.fetchVariableData(variable, forWriting);
 		    } catch (FaultException fe) {
 			      if (forWriting) {
 				        fe = new FaultException(fe.getQName(), fe.getMessage(), new Throwable("throwUninitializedToVariable"));
 			      }
 			      throw fe;
 		    }
 	  }
 

    private void replaceEndpointRefence(PartnerLinkInstance plval, Node rvalue) throws FaultException {
        if (rvalue.getNodeType() == Node.ATTRIBUTE_NODE)
            throw new FaultException(getOAssign().getOwner().constants.qnMismatchedAssignmentFailure,
                    "Can't assign an attribute to an endpoint, you probably want to select the attribute text.");

        // Eventually wrapping with service-ref element if we've been directly assigned some
        // value that isn't wrapped.
        if (rvalue.getNodeType() == Node.TEXT_NODE ||
                (rvalue.getNodeType() == Node.ELEMENT_NODE && !rvalue.getLocalName().equals("service-ref"))) {
            Document doc = DOMUtils.newDocument();
            Element serviceRef = doc.createElementNS(Namespaces.WSBPEL2_0_FINAL_SERVREF, "service-ref");
            doc.appendChild(serviceRef);
            NodeList children = rvalue.getChildNodes();
            for (int m = 0; m < children.getLength(); m++) {
                Node child = children.item(m);
                serviceRef.appendChild(doc.importNode(child, true));
            }
            rvalue = serviceRef;
        }

        getBpelRuntime().writeEndpointReference(plval, (Element)rvalue);
    }

    

    private Element copyInto(Element lval, Element ptr, Element src) {
        ptr.appendChild(ptr.getOwnerDocument().importNode(src, true));
        return lval;
    }

    

    private Node evalQuery(Node data, OMessageVarType.Part part,
                           OExpression expression, EvaluationContext ec) throws FaultException {
        assert data != null;

        if (part != null) {
            QName partName = new QName(null, part.name);
            Node qualLVal = DOMUtils.findChildByName((Element) data, partName);
            if (part.type instanceof OElementVarType) {
//                QName elName = ((OElementVarType) part.type).elementType;
//                qualLVal = DOMUtils.findChildByName((Element) qualLVal, elName);
                qualLVal = DOMUtils.getFirstChildElement((Element) qualLVal);
            } else if (part.type == null) {
                // Special case of header parts never referenced in the WSDL def
                if (qualLVal != null && qualLVal.getNodeType() == Node.ELEMENT_NODE
                        && ((Element)qualLVal).getAttribute("headerPart") != null
                        && DOMUtils.getTextContent(qualLVal) == null)
                    qualLVal = DOMUtils.getFirstChildElement((Element) qualLVal);
                // The needed part isn't there, dynamically creating it
                if (qualLVal == null) {
                    qualLVal = data.getOwnerDocument().createElementNS(null, part.name);
                    ((Element)qualLVal).setAttribute("headerPart", "true");
                    data.appendChild(qualLVal);
                }
            }
            data = qualLVal;
        }

        if (expression != null) {
            // Neat little trick....
            data = ec.evaluateQuery(data, expression);
        }

        return data;
    }

    private void invokeExtensionAssignOperation(OAssign.ExtensionAssignOperation eao) throws FaultException {
        final ExtensionContext context = new ExtensionContextImpl(_self, _scopeFrame, getBpelRuntime());

        try {
            ExtensionOperation ea = getBpelRuntime().createExtensionActivityImplementation(eao.extensionName);
            if (ea == null) {
                for (OProcess.OExtension oe : eao.getOwner().mustUnderstandExtensions) {
                    if (eao.extensionName.getNamespaceURI().equals(oe.namespaceURI)) {
                        __log.warn("Lookup of extension activity " + eao.extensionName + " failed.");
                        throw new FaultException(ExtensibilityQNames.UNKNOWN_EA_FAULT_NAME, "Lookup of extension activity " + eao.extensionName + " failed. No implementation found.");
                    }
                }
                // act like <empty> - do nothing
                context.complete(_self.parent.export());
                return;
            }

            ea.run(context, _self.parent.export(), eao.nestedElement.getElement());
        } catch (FaultException fault) {
            __log.error(fault);
            context.completeWithFault(_self.parent.export(), fault);
        }
    }


    private class EvaluationContextProxy implements EvaluationContext {

        private OScope.Variable _var;

        private Node _varNode;

        private Node _rootNode;

        private EvaluationContext _ctx;

        private EvaluationContextProxy(OScope.Variable var, Node varNode) {
            _var = var;
            _varNode = varNode;
            _ctx = getEvaluationContext();

        }

        public Node readVariable(OScope.Variable variable, OMessageVarType.Part part) throws FaultException {
            if (variable.name.equals(_var.name)) {
                if (part == null) return _varNode;
                return _ctx.getPartData((Element)_varNode, part);

            } else
                return _ctx.readVariable(variable, part);

        }

        public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
                throws FaultException {
            return _ctx.readMessageProperty(variable, property);
        }

        public boolean isLinkActive(OLink olink) throws FaultException {
            return _ctx.isLinkActive(olink);
        }

        public Node getRootNode() {
            return _rootNode;
        }

        public Node evaluateQuery(Node root, OExpression expr)
                throws FaultException {
            _rootNode = root;
            return getBpelRuntime().getExpLangRuntime().evaluateNode(expr, this);
        }

        public Node getPartData(Element message, OMessageVarType.Part part) throws FaultException {
            return _ctx.getPartData(message,part);
        }

        public Long getProcessId() {
            return _ctx.getProcessId();
        }

        public boolean narrowTypes() {
            return false;
        }

        public URI getBaseResourceURI() {
            return _ctx.getBaseResourceURI();
        }

        public Node getPropertyValue(QName propertyName) {
			return _ctx.getPropertyValue(propertyName);
		}
    }

}
