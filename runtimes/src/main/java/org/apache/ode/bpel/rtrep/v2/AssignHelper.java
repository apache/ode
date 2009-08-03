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

import java.util.List;
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.rtrep.v2.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * Helper class that shares common code
 * for ASSIGN activity and SCOPE activity (used by in-line variable
 * initialization)
 * 
 * NOTE: This will extend ACTIVITY class, but it is not
 * 		 meant to be activity, it reuses methods provided
 * 		 by superclass.
 * 
 * @author madars.vitolins _at gmail.com (University of Latvia)
 */
public class AssignHelper extends ACTIVITY {

    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(AssignHelper.class);

    private static final ASSIGNMessages __msgs = MessageBundle
            .getMessages(ASSIGNMessages.class);
    
	public AssignHelper(ActivityInfo self, ScopeFrame scopeFrame,
			LinkFrame linkFrame) {
		super(self, scopeFrame, linkFrame);
	}

	/**
	 * This method is stub, it is not meant to run
	 */
	@Override
	public void run() {
		String errMsg="AssignHelper cannot be ran as ACTIVITY";
		__log.error(errMsg);
		_self.parent.failure(errMsg, null);
	}
	
	/**
	 * Moved from ASSIGN here 
	 */
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
	
    private OActivity getOActivity() {
        return _self.o;
    }
    
	
	/**
	 * Initializes single variable by using it's 'from-spec'
	 * @param var
	 * @author madars.vitolins _at gmail.com, 2009.04.17
	 */
	public void initVar(Variable var) throws FaultException,
			ExternalVariableModuleException {
		__log.info("Initializing variable [" + var.name + "]");
		// Init variable from another variable
		if (var.type instanceof OMessageVarType
				&& var.from instanceof OAssign.VariableRef
				&& ((OAssign.VariableRef) var.from).isMessageRef()) {
			final VariableInstance lval = _scopeFrame.resolve(var);
			final VariableInstance rval = _scopeFrame
					.resolve(((OAssign.VariableRef) var.from).getVariable());
			Element lvalue = (Element) fetchVariableData(rval, false);
			initializeVariable(lval, lvalue);
		} else {
			
			Node rvalue = evalRValue(var.from);
			Node lvalue = evalLValue(var);
			// Dump r-value
			if (__log.isDebugEnabled()) {
				__log.debug("rvalue after eval " + rvalue);
				if (rvalue != null)
					__log.debug("content " + DOMUtils.domToString(rvalue));
			}
			// Dump l-value
			if (__log.isDebugEnabled()) {
				__log.debug("lvalue after eval " + rvalue);
				if (lvalue != null)
					__log.debug("content " + DOMUtils.domToString(lvalue));
			}

			Node lvaluePtr = lvalue;
			// Sneakily converting the EPR if it's not the format expected by
			// the lvalue
			if (var.from instanceof OAssign.PartnerLinkRef) {
				rvalue = getBpelRuntime().convertEndpointReference(
						(Element) rvalue, lvaluePtr);
				if (rvalue.getNodeType() == Node.DOCUMENT_NODE)
					rvalue = ((Document) rvalue).getDocumentElement();
			}

			if (rvalue.getNodeType() == Node.ELEMENT_NODE
					&& lvaluePtr.getNodeType() == Node.ELEMENT_NODE) {
				lvalue = replaceElement((Element) lvalue,
						(Element) lvaluePtr, (Element) rvalue, true);
			} else {
				lvalue = replaceContent(lvalue, lvaluePtr, rvalue
						.getTextContent());
			}
			final VariableInstance lval = _scopeFrame.resolve(var);
			if (__log.isDebugEnabled())
				__log.debug("SCOPE initialized variable '"
						+ lval.declaration.name + "' value '"
						+ DOMUtils.domToString(lvalue) + "'");

			// Commit changes!
			commitChanges(lval, lvalue);
		}
	}// initVar()
	
	/**
	 * Assumes that variable is variable not kind of partner links. 
	 * 
	 * @param var
	 * @return
	 * @throws FaultException
	 * @throws ExternalVariableModuleException
	 * @author madars.vitolins _at gmail.com
	 */
	public Node evalLValue(Variable var) throws FaultException,
			ExternalVariableModuleException {
		final OdeInternalInstance napi = getBpelRuntime();
		Node lval = null;
		VariableInstance lvar = _scopeFrame.resolve(var);
		if (!napi.isVariableInitialized(lvar)) {
			Document doc = DOMUtils.newDocument();
			Node val = var.type.newInstance(doc);
			if (val.getNodeType() == Node.TEXT_NODE) {
				Element tempwrapper = doc.createElementNS(null,
						"temporary-simple-type-wrapper");
				doc.appendChild(tempwrapper);
				tempwrapper.appendChild(val);
				val = tempwrapper;
			} else
				doc.appendChild(val);
			// Only external variables need to be initialized, others are new
			// and going to be overwritten
			if (lvar.declaration.extVar != null)
				lval = initializeVariable(lvar, val);
			else
				lval = val;
		} else
			lval = fetchVariableData(lvar, true);
		return lval;
	}
	
	/**
	 * madars.vitolins _at gmail.com - 2009.04.17 - Moved from ASSIGN here
	 * @param to
	 * @return
	 * @throws FaultException
	 * @throws ExternalVariableModuleException
	 */
    public Node evalLValue(OAssign.LValue to) throws FaultException, ExternalVariableModuleException {
        final OdeInternalInstance napi = getBpelRuntime();
        Node lval = null;
        if (!(to instanceof OAssign.PartnerLinkRef)) {
            VariableInstance lvar;
            try {
                lvar = _scopeFrame.resolve(to.getVariable());
            } catch (RuntimeException e) {
                __log.error("iid: " + napi.getInstanceId() + " error evaluating lvalue");
                throw new FaultException(getOActivity().getOwner().constants.qnSelectionFailure, e.getMessage());
            }
            if (!napi.isVariableInitialized(lvar)) {
                Document doc = DOMUtils.newDocument();
                Node val = to.getVariable().type.newInstance(doc);
                if (val.getNodeType() == Node.TEXT_NODE) {
                    Element tempwrapper = doc.createElementNS(null, "temporary-simple-type-wrapper");
                    doc.appendChild(tempwrapper);
                    tempwrapper.appendChild(val);
                    val = tempwrapper;
                } else doc.appendChild(val);
                // Only external variables need to be initialized, others are new and going to be overwritten
                if (lvar.declaration.extVar != null) lval = initializeVariable(lvar, val);
                else lval = val;
            } else
                lval = fetchVariableData(lvar, true);
        }
        return lval;
    }
    
    /**
     * Get the r-value. There are several possibilities:
     * <ul>
     * <li>a message is selected - an element representing the whole message is
     * returned.</li>
     * <li>a (element) message part is selected - the element is returned.
     * </li>
     * <li>a (typed) message part is select - a wrapper element is returned.
     * </li>
     * <li>an attribute is selected - an attribute node is returned. </li>
     * <li>a text node/string expression is selected - a text node is returned.
     * </li>
     * </ul>
     * (madars.vitolins _at gmail.com - 2009.04.17 - moved from ASSIGN here)
     * @param from
     *
     * @return Either {@link Element}, {@link org.w3c.dom.Text}, or
     *         {@link org.w3c.dom.Attr} node representing the r-value.
     *
     * @throws FaultException
     *             DOCUMENTME
     * @throws UnsupportedOperationException
     *             DOCUMENTME
     * @throws IllegalStateException
     *             DOCUMENTME
     */
    public Node evalRValue(OAssign.RValue from) throws FaultException, ExternalVariableModuleException {
        if (__log.isDebugEnabled())
            __log.debug("Evaluating FROM expression \"" + from + "\".");

        Node retVal;
        if (from instanceof OAssign.DirectRef) {
            OAssign.DirectRef dref = (OAssign.DirectRef) from;
            sendVariableReadEvent(_scopeFrame.resolve(dref.variable));
            Node data = fetchVariableData(_scopeFrame.resolve(dref.variable), false);
            retVal = DOMUtils.findChildByName((Element)data, dref.elName);
        } else if (from instanceof OAssign.VariableRef) {
            OAssign.VariableRef varRef = (OAssign.VariableRef) from;
            sendVariableReadEvent(_scopeFrame.resolve(varRef.variable));
            Node data = fetchVariableData(_scopeFrame.resolve(varRef.variable), false);
            retVal = evalQuery(data, varRef.part != null ? varRef.part : varRef.headerPart, varRef.location, getEvaluationContext());
        } else if (from instanceof OAssign.PropertyRef) {
            OAssign.PropertyRef propRef = (OAssign.PropertyRef) from;
            sendVariableReadEvent(_scopeFrame.resolve(propRef.variable));
            Node data = fetchVariableData(_scopeFrame.resolve(propRef.variable), false);
            retVal = evalQuery(data, propRef.propertyAlias.part,
                    propRef.propertyAlias.location, getEvaluationContext());
        } else if (from instanceof OAssign.PartnerLinkRef) {
            OAssign.PartnerLinkRef pLinkRef = (OAssign.PartnerLinkRef) from;
            PartnerLinkInstance pLink = _scopeFrame.resolve(pLinkRef.partnerLink);
            Node tempVal =pLinkRef.isMyEndpointReference ?
                    getBpelRuntime().fetchMyRoleEndpointReferenceData(pLink)
                    : getBpelRuntime().fetchPartnerRoleEndpointReferenceData(pLink);
            if (__log.isDebugEnabled())
                __log.debug("RValue is a partner link, corresponding endpoint "
                        + tempVal.getClass().getName() + " has value " + DOMUtils.domToString(tempVal));
            retVal = tempVal;
        } else if (from instanceof OAssign.Expression) {
            OExpression expr = ((OAssign.Expression) from).expression;
            List<Node> l = getBpelRuntime().getExpLangRuntime().evaluate(expr, getEvaluationContext());

            if (l.size() == 0) {
                String msg = __msgs.msgRValueNoNodesSelected(expr.toString());
                if (__log.isDebugEnabled()) __log.debug(from + ": " + msg);
                throw new FaultException(getOActivity().getOwner().constants
                    .qnSelectionFailure, msg, new Throwable("ignoreMissingFromData"));
            } else if (l.size() > 1) {
                String msg = __msgs.msgRValueMultipleNodesSelected(expr.toString());
                if (__log.isDebugEnabled()) __log.debug(from + ": " + msg);
                throw new FaultException(getOActivity().getOwner().constants.qnSelectionFailure, msg);
            }
            retVal = l.get(0);
        } else if (from instanceof OAssign.Literal) {
            Element literalRoot = ((OAssign.Literal) from).getXmlLiteral().getDocumentElement();
            assert literalRoot.getLocalName().equals("literal");
            // We'd like a single text node...

            literalRoot.normalize();
            retVal = literalRoot.getFirstChild();

            // Adjust for whitespace before an element.
            if (retVal != null && retVal.getNodeType() == Node.TEXT_NODE
                    && retVal.getTextContent().trim().length() == 0
                    && retVal.getNextSibling() != null) {
                retVal = retVal.getNextSibling();
            }

            if (retVal == null) {
                // Special case, no children --> empty TII
                retVal = literalRoot.getOwnerDocument().createTextNode("");
            } else if (retVal.getNodeType() == Node.ELEMENT_NODE) {
                // Make sure there is no more elements.
                Node x = retVal.getNextSibling();
                while (x != null) {
                    if (x.getNodeType() == Node.ELEMENT_NODE) {
                        String msg = __msgs.msgLiteralContainsMultipleEIIs();
                        if (__log.isDebugEnabled())
                            __log.debug(from + ": " + msg);
                        throw new FaultException(
                        		getOActivity().getOwner().constants.qnSelectionFailure,
                                msg);

                    }
                    x = x.getNextSibling();
                }
            } else if (retVal.getNodeType() == Node.TEXT_NODE) {
                // Make sure there are no elements following this text node.
                Node x = retVal.getNextSibling();
                while (x != null) {
                    if (x.getNodeType() == Node.ELEMENT_NODE) {
                        String msg = __msgs.msgLiteralContainsMixedContent();
                        if (__log.isDebugEnabled())
                            __log.debug(from + ": " + msg);
                        throw new FaultException(
                        		getOActivity().getOwner().constants.qnSelectionFailure,
                                msg);

                    }
                    x = x.getNextSibling();
                }

            }

            if (retVal == null) {
                String msg = __msgs.msgLiteralMustContainTIIorEII();
                if (__log.isDebugEnabled())
                    __log.debug(from + ": " + msg);
                throw new FaultException(
                		getOActivity().getOwner().constants.qnSelectionFailure,
                        msg);
            }
        } else {
            String msg = __msgs
                    .msgInternalError("Unknown RVALUE type: " + from);
            if (__log.isErrorEnabled())
                __log.error(from + ": " + msg);
            throw new FaultException(
            		getOActivity().getOwner().constants.qnSelectionFailure, msg);
        }

        // Now verify we got something.
        if (retVal == null) {
            String msg = __msgs.msgEmptyRValue();
            if (__log.isDebugEnabled())
                __log.debug(from + ": " + msg);
            throw new FaultException(
            		getOActivity().getOwner().constants.qnSelectionFailure, msg);
        }

        // Now check that we got the right thing.
        switch (retVal.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.ATTRIBUTE_NODE:
            case Node.ELEMENT_NODE:
            case Node.CDATA_SECTION_NODE:
                break;
            default:
                String msg = __msgs.msgInvalidRValue();
                if (__log.isDebugEnabled())
                    __log.debug(from + ": " + msg);

                throw new FaultException(
                        getOActivity().getOwner().constants.qnSelectionFailure, msg);

        }

        return retVal;
    }
    
    /**
     * madars.vitolins _at gmail.com - 2009.04.17 - moved from ASSIGN here
     */
    public Element replaceElement(Element lval, Element ptr, Element src,
                                   boolean keepSrcElement) {
        Document doc = ptr.getOwnerDocument();
        Node parent = ptr.getParentNode();
        if (keepSrcElement) {
            Element replacement = (Element)doc.importNode(src, true);
            parent.replaceChild(replacement, ptr);
            return (lval == ptr) ? replacement :  lval;
        }

        Element replacement = doc.createElementNS(ptr.getNamespaceURI(), ptr.getLocalName());
        NodeList nl = src.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i)
            replacement.appendChild(doc.importNode(nl.item(i), true));
        NamedNodeMap attrs = src.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i) {
            Attr attr = (Attr)attrs.item(i);
            if (!attr.getName().startsWith("xmlns")) {
                replacement.setAttributeNodeNS((Attr)doc.importNode(attrs.item(i), true));
                // Case of qualified attribute values, we're forced to add corresponding namespace declaration manually
                int colonIdx = attr.getValue().indexOf(":");
                if (colonIdx > 0) {
                    String prefix = attr.getValue().substring(0, colonIdx);
                    String attrValNs = src.lookupPrefix(prefix);
                    if (attrValNs != null)
                        replacement.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:"+ prefix, attrValNs);
                }
            }
        }
        parent.replaceChild(replacement, ptr);
        DOMUtils.copyNSContext(ptr, replacement);

        return (lval == ptr) ? replacement :  lval;
    }
    
    /**
     * isInsert flag desginates this as an 'element' type insertion, which
     * requires insert the actual element value, rather than it's children
     * (madars.vitolins _at gmail.com - 2009.04.17 - moved from ASSIGN here)
     * @return
     * @throws FaultException
     */
    public Node replaceContent(Node lvalue, Node lvaluePtr, String rvalue)
            throws FaultException {
        Document d = lvaluePtr.getOwnerDocument();

        if (__log.isDebugEnabled()) {
            __log.debug("lvaluePtr type " + lvaluePtr.getNodeType());
            __log.debug("lvaluePtr " + DOMUtils.domToString(lvaluePtr));
            __log.debug("lvalue " + lvalue);
            __log.debug("rvalue " + rvalue);
        }

        switch (lvaluePtr.getNodeType()) {
            case Node.ELEMENT_NODE:

                // Remove all the children.
                while (lvaluePtr.hasChildNodes())
                    lvaluePtr.removeChild(lvaluePtr.getFirstChild());

                // Append a new text node.
                lvaluePtr.appendChild(d.createTextNode(rvalue));

                // If lvalue is a text, removing all lvaluePtr children had just removed it
                // so we need to rebuild it as a child of lvaluePtr
                if (lvalue instanceof Text)
                    lvalue = lvaluePtr.getFirstChild();
                break;

            case Node.TEXT_NODE:

                Node newval = d.createTextNode(rvalue);
                // Replace ourselves .
                lvaluePtr.getParentNode().replaceChild(newval, lvaluePtr);

                // A little kludge, let our caller know that the root element has changed.
                // (used for assignment to a simple typed variable)
                if (lvalue.getNodeType() == Node.ELEMENT_NODE) {
                    // No children, adding an empty text children to point to
                    if (lvalue.getFirstChild() == null) {
                        Text txt = lvalue.getOwnerDocument().createTextNode("");
                        lvalue.appendChild(txt);
                    }
                    if (lvalue.getFirstChild().getNodeType() == Node.TEXT_NODE)
                        lvalue = lvalue.getFirstChild();
                }
                if (lvalue.getNodeType() == Node.TEXT_NODE && ((Text) lvalue).getWholeText().equals(
                        ((Text) lvaluePtr).getWholeText()))
                    lvalue = lvaluePtr = newval;
                break;

            case Node.ATTRIBUTE_NODE:

                ((Attr) lvaluePtr).setValue(rvalue);
                break;

            default:
                // This could occur if the expression language selects something
                // like
                // a PI or a CDATA.
                String msg = __msgs.msgInvalidLValue();
                if (__log.isDebugEnabled())
                    __log.debug(lvaluePtr + ": " + msg);
                throw new FaultException(
                		getOActivity().getOwner().constants.qnSelectionFailure, msg);
        }

        return lvalue;
    }
    
    /**
     * madars.vitolins _at gmail.com - 2009.04.17 moved from ASSIGN here
     * @param data
     * @param part
     * @param expression
     * @param ec
     * @return
     * @throws FaultException
     */
    private Node evalQuery(Node data, OMessageVarType.Part part,
                           OExpression expression, EvaluationContext ec) throws FaultException {
        assert data != null;

        if (part != null) {
            QName partName = new QName(null, part.name);
            Node qualLVal = DOMUtils.findChildByName((Element) data, partName);
            if (part.type instanceof OElementVarType) {
                QName elName = ((OElementVarType) part.type).elementType;
                qualLVal = DOMUtils.findChildByName((Element) qualLVal, elName);
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
    
   
    
}
