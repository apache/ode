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

package org.apache.ode.bpel.elang.xpath20.compiler;

import java.util.Date;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.elang.xpath10.compiler.XPathMessages;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * This is a mock implementation of the XPathVariableResolver for compilation. It
 * always returns an empty string which allows execution of XPath expressions even
 * if we don't have any values yet. This way we can easily rule out invalid variables
 * and isolate properly BPEL variables.
 * @author mriou <mriou at apache dot org>
 */
public class JaxpVariableResolver implements XPathVariableResolver {

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    private CompilerContext _cctx;
    private OXPath10ExpressionBPEL20 _oxpath;

    public JaxpVariableResolver(CompilerContext cctx, OXPath10ExpressionBPEL20 oxpath) {
        _cctx = cctx;
        _oxpath = oxpath;
    }

    public Object resolveVariable(QName variableName) {
        // Custom variables
        if ("ode".equals(variableName.getPrefix())
                || Namespaces.ODE_EXTENSION_NS.equals(variableName.getNamespaceURI())) {
            if ("pid".equals(variableName.getLocalPart()) || "processQName".equals(variableName.getLocalPart()))
                return "";
            if ("currentEventDateTime".equals(variableName.getLocalPart())) 
                return new Date(1L);
        }

        try {
            String name = variableName.getLocalPart();
            if(_oxpath.isJoinExpression) {
                // these resolve to links
                OLink olink = _cctx.resolveLink(name);
                _oxpath.links.put(name, olink);
                return Boolean.TRUE;
            } else {
                int dot = name.indexOf('.');
                if (dot != -1)
                    name = name.substring(0,dot);
                OScope.Variable var = _cctx.resolveVariable(name);
                _oxpath.vars.put(name, var);
                return extractValue(var, var.type);
            }
        } catch (CompilationException e) {
            throw new WrappedResolverException(e);
        }
    }

    private Object extractValue(OScope.Variable var, OVarType varType) {
        if (varType instanceof OXsdTypeVarType) {
            return generateFromType(((OXsdTypeVarType)varType).xsdType);
        } else if (varType instanceof OElementVarType) {
            return generateFromType(((OElementVarType)varType).elementType);
        } else if (varType instanceof OMessageVarType) {
            // MR That's an ugly hack but otherwise, xpath compilation doesn't work
            if (((OMessageVarType)varType).parts.size() == 0)
                throw new WrappedResolverException(__msgs.errExpressionMessageNoPart(var.name));
            return extractValue(var, ((OMessageVarType)varType).parts.values().iterator().next().type);
        }
        return "";
    }

    private Object generateFromType(QName typeName) {
        if (typeName.getNamespaceURI().equals(Namespaces.XML_SCHEMA)) {
            if (typeName.getLocalPart().equals("int") ||
                    typeName.getLocalPart().equals("integer") ||
                    typeName.getLocalPart().equals("short") ||
                    typeName.getLocalPart().equals("long") ||
                    typeName.getLocalPart().equals("byte") ||
                    typeName.getLocalPart().equals("float") ||
                    typeName.getLocalPart().equals("double") ||
                    typeName.getLocalPart().equals("nonPositiveInteger") ||
                    typeName.getLocalPart().equals("nonNegativeInteger") ||
                    typeName.getLocalPart().equals("negativeInteger") ||
                    typeName.getLocalPart().equals("unsignedLong") ||
                    typeName.getLocalPart().equals("unsignedInt") ||
                    typeName.getLocalPart().equals("unsignedShort") ||
                    typeName.getLocalPart().equals("unsignedByte"))
                return 0;
            if (typeName.getLocalPart().equals("boolean"))
                return Boolean.TRUE;
            if (typeName.getLocalPart().equals("string"))
                return "";
        }
        Document doc = DOMUtils.newDocument();
        doc.appendChild(doc.createElement("empty"));
        return doc.getDocumentElement();
    }
}
