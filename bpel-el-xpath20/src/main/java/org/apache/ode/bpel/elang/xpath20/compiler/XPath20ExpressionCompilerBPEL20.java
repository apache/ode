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

import net.sf.saxon.xpath.XPathEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.capi.CompilerContext;
import org.apache.ode.bpel.capi.ExpressionCompiler;
import org.apache.ode.bpel.elang.xpath10.compiler.XPathMessages;
import org.apache.ode.bpel.elang.xpath20.Constants;
import org.apache.ode.bpel.elang.xpath20.WrappedResolverException;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.elang.xpath20.runtime.XPath20ExpressionRuntime;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;


/**
 * XPath compiler based on the SAXON implementation.
 * @author mriou <mriou at apache dot org>
 */
public class XPath20ExpressionCompilerBPEL20 implements ExpressionCompiler {

    private static final Log __log = LogFactory.getLog(XPath20ExpressionCompilerBPEL20.class);

    private static final QName _qnLinkStatus = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETLINKSTATUS);
    private static final QName _qnVarProp = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETVARIABLEPROPRTY);
    private static final QName _qnVarData = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_GETVARIABLEDATA);
    private static final QName _qnXslTransform = new QName(Constants.BPEL20_NS, Constants.EXT_FUNCTION_DOXSLTRANSFORM);

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    private CompilerContext _compilerContext;

    private Map<String, String> _properties = new HashMap<String,String>();

    public XPath20ExpressionCompilerBPEL20() {
        super();
        _properties.put("runtime-class", XPath20ExpressionRuntime.class.getName());
    }

    public void setCompilerContext(CompilerContext compilerContext) {
        _compilerContext = compilerContext;
    }

    /**
     * @see org.apache.ode.bpel.capi.ExpressionCompiler#compileJoinCondition(java.lang.Object)
     */
    public OExpression compileJoinCondition(Object source) throws CompilationException {
        return _compile((Expression)source, true);
    }

    /**
     * @see org.apache.ode.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
     */
    public OExpression compile(Object source) throws CompilationException {
        return _compile((Expression)source, false);
    }
    /**
     * @see org.apache.ode.bpel.capi.ExpressionCompiler#compileLValue(java.lang.Object)
     */
    public OLValueExpression compileLValue(Object source) throws CompilationException {
        return (OLValueExpression)_compile((Expression)source, false);
    }

    /**
     * @see org.apache.ode.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
     */
    private OExpression _compile(Expression xpath, boolean isJoinCondition) throws CompilationException {
        OXPath20ExpressionBPEL20 oexp = new OXPath20ExpressionBPEL20(
                _compilerContext.getOProcess(),
                _qnVarData,
                _qnVarProp,
                _qnLinkStatus,
                _qnXslTransform,
                isJoinCondition);
        oexp.namespaceCtx = xpath.getNamespaceContext();
        doJaxpCompile(oexp, xpath);
        return oexp;
    }

    private void doJaxpCompile(OXPath20ExpressionBPEL20 out, Expression source)
            throws CompilationException {
        String xpathStr = source.getXPathString();
        if(xpathStr == null){
            Node node = source.getNode();
            if(node == null){
                throw new IllegalStateException("XPath string and xpath node are both null");
            }
            if(node.getNodeType() != Node.TEXT_NODE){
                throw new CompilationException(__msgs.errUnexpectedNodeTypeForXPath(DOMUtils.domToString(node)));
            }
            xpathStr = node.getNodeValue();
        }
        xpathStr = xpathStr.trim();

        out.xpath = xpathStr;
        try {
            __log.debug("Compiling expression " + xpathStr);
            XPathFactory xpf = new net.sf.saxon.xpath.XPathFactoryImpl();
            xpf.setXPathFunctionResolver(new JaxpFunctionResolver(_compilerContext, out, source.getNamespaceContext(), Constants.BPEL20_NS));
            xpf.setXPathVariableResolver(new JaxpVariableResolver(_compilerContext, out));
            XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();

            xpe.setNamespaceContext(source.getNamespaceContext());
            XPathExpression expr = xpe.compile(xpathStr);
            // Here we're "faking" an evaluation to parse properly variables and functions and
            // detect all possible mistakes. To do so we're using specific resolvers that always
            // return guessed appropriate values from variable types.
            expr.evaluate(DOMUtils.newDocument());
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new CompilationException(__msgs.warnXPath20Syntax(xpathStr, e.toString()), e);
        } catch (WrappedResolverException wre) {
            if (wre._compilationMsg != null) throw new CompilationException(wre._compilationMsg, wre);
            if (wre.getCause() instanceof CompilationException) throw (CompilationException)wre.getCause();
            throw wre;
        }

    }

    public Map<String, String> getProperties() {
        return _properties;
    }
}
