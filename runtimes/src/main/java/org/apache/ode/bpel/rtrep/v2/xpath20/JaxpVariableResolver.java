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

package org.apache.ode.bpel.rtrep.v2.xpath20;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.StringValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.v2.EvaluationContext;
import org.apache.ode.bpel.rtrep.v2.OLink;
import org.apache.ode.bpel.rtrep.v2.OMessageVarType;
import org.apache.ode.bpel.rtrep.v2.OScope;
import org.apache.ode.bpel.rtrep.v2.OXsdTypeVarType;
import org.apache.ode.bpel.rtrep.v2.xpath10.OXPath10ExpressionBPEL20;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author mriou <mriou at apache dot org>
 */
public class JaxpVariableResolver implements XPathVariableResolver {

    private static final Log __log = LogFactory.getLog(JaxpVariableResolver.class);

    private EvaluationContext _ectx;
    private OXPath10ExpressionBPEL20 _oxpath;
    private Configuration _config;

    public JaxpVariableResolver(EvaluationContext ectx, OXPath10ExpressionBPEL20 oxpath, Configuration config) {
        _ectx = ectx;
        _oxpath = oxpath;
        _config = config;
    }

    public Object resolveVariable(QName variableName) {
        __log.debug("Resolving variable " + variableName);

        if(!(_oxpath instanceof OXPath10ExpressionBPEL20)){
            throw new IllegalStateException("XPath variables not supported for bpel 1.1");
        }

        // Custom variables
        if (variableName.getNamespaceURI().equals(Namespaces.ODE_EXTENSION_NS)) {
            if ("pid".equals(variableName.getLocalPart())) {
                return _ectx.getProcessId();
            }
        }

        OXPath10ExpressionBPEL20 expr = _oxpath;
        if(expr.isJoinExpression){
            OLink olink = _oxpath.links.get(variableName.getLocalPart());
            try {
                return _ectx.isLinkActive(olink) ? Boolean.TRUE : Boolean.FALSE;
            } catch (FaultException e) {
                throw new WrappedFaultException(e);
            }
        } else {
            String varName;
            String partName;
            int dotloc = variableName.getLocalPart().indexOf('.');
            if (dotloc == -1) {
                varName = variableName.getLocalPart();
                partName = null;
            } else {
                varName = variableName.getLocalPart().substring(0, dotloc);
                partName = variableName.getLocalPart().substring(dotloc + 1);
            }
            OScope.Variable variable = _oxpath.vars.get(varName);
            OMessageVarType.Part part = partName == null ? null : ((OMessageVarType)variable.type).parts.get(partName);

            try{
                final Node variableNode = _ectx.readVariable(variable, part);
                if (variableNode == null)
                    throw new FaultException(variable.getOwner().constants.qnSelectionFailure,
                            "Unknown variable " + variableName.getLocalPart());
                if (_ectx.narrowTypes()) {
                    if (variable.type instanceof OXsdTypeVarType && ((OXsdTypeVarType)variable.type).simple)
                        return getSimpleContent(variableNode,((OXsdTypeVarType)variable.type).xsdType);
                    if (part != null && part.type instanceof OXsdTypeVarType && ((OXsdTypeVarType)part.type).simple)
                        return getSimpleContent(variableNode,((OXsdTypeVarType)part.type).xsdType);
                }

                // Saxon expects a node list, this nodelist should contain exactly one item, the attribute
                // value
                // return new SingletonNodeList(variableNode);
                
                // Saxon used to expect a node list, but now a regular node will suffice.
                return variableNode;
            } catch(FaultException e){
                throw new WrappedFaultException(e);
            }
        }
    }
    
    private Object getSimpleContent(Node simpleNode, QName type) {
        Document doc = (simpleNode instanceof Document) 
        	? ((Document) simpleNode) 
        	: simpleNode.getOwnerDocument();
        String text = simpleNode.getTextContent();
        try {
            {
                int fp = _config.getNamePool().allocate("", type.getNamespaceURI(), type.getLocalPart());
                SchemaType type2 = _config.getSchemaType(fp);
                if (type2 == null || !type2.isAtomicType()) {
                    if (__log.isDebugEnabled()) {
                        __log.debug("converting " + type + " value " + text + " result: " + null);
                    }
                    return null;
                } else {
                    Object value = StringValue.convertStringToAtomicType(text, (AtomicType) type2 , null);
                    if (__log.isDebugEnabled()) {
                        __log.debug("converting " + type + " value " + text + " result: " + value);
                    }
                    return value;
                }
            }
        } catch (Exception e) {
	        // Elegant way failed, trying brute force 
	        try {
	    	    return Integer.valueOf(text);
	      	} catch (NumberFormatException nfe) { }
	      	try {
	      		return Double.valueOf(text);
	      	} catch (NumberFormatException nfe) { }
	      
	        // Remember: always a node set
	        if (simpleNode.getParentNode() != null)
	            return simpleNode.getParentNode().getChildNodes();
	        else {        	
	            return doc.createTextNode(text);
	        }
        }
    }
}
