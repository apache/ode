/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.ode.bpel.compiler.v2.xpath10.jaxp;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.XPathMessages;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.compiler.v2.CompilerContext;
import org.apache.ode.bpel.compiler.v2.ExpressionCompiler;
import org.apache.ode.bpel.rtrep.v2.xpath10.OXPath10Expression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Node;

/**
 * XPath compiler based on the default JAXP implementation. Supports both 2.0 and 1.1 BPEL.
 */
public abstract class JaxpXPath10ExpressionCompilerImpl implements ExpressionCompiler {
    /** Class-level logger. */
    private static final Log __log = LogFactory.getLog(JaxpXPath10ExpressionCompilerImpl.class);

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    protected CompilerContext _compilerContext;

    /** Namespace of the BPEL functions (for v2 to v1 compatibility) . */
    private String _bpelNsURI;

    protected QName _qnFnGetVariableData;

    protected QName _qnFnGetVariableProperty;

    protected QName _qnFnGetLinkStatus;

    protected Map<String, String> _properties = new HashMap<String, String>();

    /**
     * Construtor.
     * 
     * @param bpelNsURI
     *            the BPEL extension function namespace; varies depending on BPEL version.
     */
    public JaxpXPath10ExpressionCompilerImpl(String bpelNsURI) {
        _bpelNsURI = bpelNsURI;
        _qnFnGetVariableData = new QName(_bpelNsURI, "getVariableData");
        _qnFnGetVariableProperty = new QName(_bpelNsURI, "getVariableProperty");
        _qnFnGetLinkStatus = new QName(_bpelNsURI, "getLinkStatus");
        _properties.put("runtime-class", "org.apache.ode.bpel.rtrep.v2.xpath10.jaxp.JaxpXPath10ExpressionRuntime");
    }

    /**
     * @see org.apache.ode.bpel.compiler.v2.ExpressionCompiler#setCompilerContext(org.apache.ode.bpel.compiler.v2.CompilerContext)
     */
    public void setCompilerContext(CompilerContext compilerContext) {
        _compilerContext = compilerContext;
    }

    /**
     * @see org.apache.ode.bpel.compiler.v2.ExpressionCompiler#getProperties()
     */
    public Map<String, String> getProperties() {
        return _properties;
    }

    /**
     * Verifies validity of a xpath expression.
     */
    protected void doJaxpCompile(OXPath10Expression out, Expression source) throws CompilationException {
        String xpathStr;
        Node node = source.getExpression();
        if (node == null) {
            throw new IllegalStateException("XPath string and xpath node are both null");
        }

        xpathStr = node.getNodeValue();
        xpathStr = xpathStr.trim();
        if (xpathStr.length() == 0) {
            throw new CompilationException(__msgs.errXPathSyntax(xpathStr));
        }

        try {
            __log.debug("JAXP compile: xpath = " + xpathStr);
            // use default XPath implementation
            XPathFactory xpf = XPathFactory.newInstance();
            __log.debug("JAXP compile: XPathFactory impl = " + xpf.getClass());
            XPath xpath = xpf.newXPath();
            xpath.setXPathFunctionResolver(new JaxpFunctionResolver(_compilerContext, out, source
                .getNamespaceContext(), _bpelNsURI));
            xpath.setXPathVariableResolver(new JaxpVariableResolver(_compilerContext, out));
            xpath.setNamespaceContext(source.getNamespaceContext());
            XPathExpression xpe = xpath.compile(xpathStr);
            // dummy evaluation to resolve variables and functions (hopefully complete...)
            xpe.evaluate(DOMUtils.newDocument());
            out.xpath = xpathStr;
        } catch (XPathExpressionException e) {
            throw new CompilationException(__msgs.errUnexpectedCompilationError(e.getMessage()), e);
        }
    }
}
