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

package org.apache.ode.bpel.elang.xpath20.runtime;

import net.sf.saxon.value.DateTimeValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.elang.xpath20.compiler.WrappedResolverException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.xsd.XSTypes;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;
import java.util.Calendar;

/**
 * @author mriou <mriou at apache dot org>
 */
public class JaxpVariableResolver implements XPathVariableResolver {

    private static final Log __log = LogFactory.getLog(JaxpVariableResolver.class);

    private EvaluationContext _ectx;
    private OXPath10ExpressionBPEL20 _oxpath;

    public JaxpVariableResolver(EvaluationContext ectx, OXPath10ExpressionBPEL20 oxpath) {
        _ectx = ectx;
        _oxpath = oxpath;
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
                throw new WrappedResolverException(e);
            }
        }else{
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
                Node variableNode = _ectx.readVariable(variable, part);
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
                return new SingletonNodeList(variableNode);
                
            }catch(FaultException e){
                throw new WrappedResolverException(e);
            }
        }
    }
    
    private Object getSimpleContent(Node simpleNode, QName type) {
        String text = simpleNode.getTextContent();
        try {
    		Object jobj = XSTypes.toJavaObject(type,text);
            // Saxon wants its own dateTime type and doesn't like Calendar or Date
            if (jobj instanceof Calendar) return new DateTimeValue((Calendar) jobj, true);
            else return jobj;
        } catch (Exception e) { }
        // Elegant way failed, trying brute force
    	try {
    		return Integer.valueOf(text);
    	} catch (NumberFormatException e) { }
    	try {
    		return Double.valueOf(text);
    	} catch (NumberFormatException e) { }
        // Remember: always a node set
        if (simpleNode.getParentNode() != null)
            return simpleNode.getParentNode().getChildNodes();
        else return text;
    }

    
    private static class SingletonNodeList implements NodeList {
        private Node _node;
        
        SingletonNodeList(Node node) {
            _node = node;
        }
        
        public Node item(int index) {
            if (index != 0)
                throw new IndexOutOfBoundsException(""+index);
            return _node;
        }

        public int getLength() {
            return 1;
        }
        
    }
}
