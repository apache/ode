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
package org.apache.ode.bpel.elang.xpath10.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.ExpressionCompiler;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.utils.msg.MessageBundle;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;
import org.w3c.dom.Node;

/**
 * XPath compiler based on the JAXEN implementation. Supports both 2.0 and 1.1
 * BPEL.
 */
public abstract class XPath10ExpressionCompilerImpl implements ExpressionCompiler {

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    // private HashMap<String,Function> _extensionFunctions = new
    // HashMap<String,Function>();
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
     *            the BPEL extension function namespace; varies depending on
     *            BPEL version.
     */
    public XPath10ExpressionCompilerImpl(String bpelNsURI) {
        _bpelNsURI = bpelNsURI;
        _qnFnGetVariableData = new QName(_bpelNsURI, "getVariableData");
        _qnFnGetVariableProperty = new QName(_bpelNsURI, "getVariableProperty");
        _qnFnGetLinkStatus = new QName(_bpelNsURI, "getLinkStatus");
        _properties.put("runtime-class", "org.apache.ode.bpel.elang.xpath10.runtime.XPath10ExpressionRuntime");        
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#setCompilerContext(org.apache.ode.bpel.compiler.api.CompilerContext)
     */
    public void setCompilerContext(CompilerContext compilerContext) {
        _compilerContext = compilerContext;
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#getProperties()
     */
    public Map<String, String> getProperties() {
        return _properties;
    }

    // Dead code
    /*
     * private void registerExtensionFunction(String name, Class function) { try {
     * Function jaxenFunction = (Function)function.newInstance();
     * _extensionFunctions.put(name, jaxenFunction); } catch
     * (InstantiationException e) { throw new RuntimeException("unexpected error
     * creating extension function: " + name, e); } catch
     * (IllegalAccessException e) { throw new RuntimeException("unexpected error
     * creating extension function: " + name, e); } catch (ClassCastException e) {
     * throw new RuntimeException("expected extension function of type " +
     * Function.class.getName()); } }
     */

    /**
     * Verifies validity of a xpath expression.
     */
    protected void doJaxenCompile(OXPath10Expression out, Expression source) throws CompilationException {
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
            XPathReader reader = XPathReaderFactory.createReader();
            JaxenBpelHandler handler = new JaxenBpelHandler(_bpelNsURI, out, source.getNamespaceContext(),
                    _compilerContext);
            reader.setXPathHandler(handler);

            reader.parse(xpathStr);
            out.xpath = xpathStr;
        } catch (CompilationExceptionWrapper e) {
            CompilationException ce = e.getCompilationException();
            if (ce == null) {
                ce = new CompilationException(__msgs.errUnexpectedCompilationError(e.getMessage()), e);
            }
            throw ce;
        } catch (SAXPathException e) {
            throw new CompilationException(__msgs.errXPathSyntax(xpathStr));
        }
    }

}
