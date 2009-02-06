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

package org.apache.ode.bpel.compiler.v2.xpath20;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import net.sf.saxon.om.Name11Checker;
import net.sf.saxon.om.NamespaceConstant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.WrappedResolverException;
import org.apache.ode.bpel.compiler.XPathMessages;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.compiler.v2.CompilerContext;
import org.apache.ode.bpel.compiler.v2.ExpressionCompiler;
import org.apache.ode.bpel.compiler.v2.xpath10.XslCompilationErrorListener;
import org.apache.ode.bpel.rtrep.common.Constants;
import org.apache.ode.bpel.rtrep.v2.OExpression;
import org.apache.ode.bpel.rtrep.v2.OLValueExpression;
import org.apache.ode.bpel.rtrep.v2.xpath20.OXPath20ExpressionBPEL20;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.w3c.dom.Node;

/**
 * XPath compiler based on the SAXON implementation.
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class XPath20ExpressionCompilerImpl implements ExpressionCompiler {

    protected static final Log __log = LogFactory.getLog(XPath20ExpressionCompilerBPEL20.class);

    protected String _bpelNS;
    protected QName _qnLinkStatus;
    protected QName _qnVarProp;
    protected QName _qnVarData;
    protected QName _qnXslTransform;

    protected final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);
    protected Map<String, String> _properties = new HashMap<String, String>();
    protected CompilerContext _compilerContext;

    public XPath20ExpressionCompilerImpl(String bpelNS) {
        _bpelNS = bpelNS;
        _qnLinkStatus = new QName(_bpelNS, Constants.EXT_FUNCTION_GETLINKSTATUS);
        _qnVarProp = new QName(_bpelNS, Constants.EXT_FUNCTION_GETVARIABLEPROPERTY);
        _qnVarData = new QName(_bpelNS, Constants.EXT_FUNCTION_GETVARIABLEDATA);
        _qnXslTransform = new QName(_bpelNS, Constants.EXT_FUNCTION_DOXSLTRANSFORM);

        _properties.put("runtime-class", "org.apache.ode.bpel.rtrep.v2.xpath20.XPath20ExpressionRuntime");
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);
    }

    public void setCompilerContext(CompilerContext compilerContext) {
        _compilerContext = compilerContext;
        XslCompilationErrorListener xe = new XslCompilationErrorListener(compilerContext);
        XslTransformHandler.getInstance().setErrorListener(xe);
    }

    /**
     * @see org.apache.ode.bpel.compiler.v2.ExpressionCompiler#compileJoinCondition(java.lang.Object)
     */
    public OExpression compileJoinCondition(Object source) throws CompilationException {
        return _compile((Expression) source, true);
    }

    /**
     * @see org.apache.ode.bpel.compiler.v2.ExpressionCompiler#compile(java.lang.Object)
     */
    public OExpression compile(Object source) throws CompilationException {
        return _compile((Expression) source, false);
    }

    /**
     * @see org.apache.ode.bpel.compiler.v2.ExpressionCompiler#compileLValue(java.lang.Object)
     */
    public OLValueExpression compileLValue(Object source) throws CompilationException {
        return (OLValueExpression) _compile((Expression) source, false);
    }

    /**
     * @see org.apache.ode.bpel.compiler.v2.ExpressionCompiler#compile(java.lang.Object)
     */
    private OExpression _compile(org.apache.ode.bpel.compiler.bom.Expression xpath, boolean isJoinCondition)
            throws CompilationException {
        OXPath20ExpressionBPEL20 oexp = new OXPath20ExpressionBPEL20(_compilerContext.getOProcess(), _qnVarData,
                _qnVarProp, _qnLinkStatus, _qnXslTransform, isJoinCondition);
        oexp.namespaceCtx = xpath.getNamespaceContext();
        doJaxpCompile(oexp, xpath);
        return oexp;
    }

    private void doJaxpCompile(OXPath20ExpressionBPEL20 out, Expression source) throws CompilationException {
        String xpathStr;
        Node node = source.getExpression();
        if (node == null) {
            throw new IllegalStateException("XPath string and xpath node are both null");
        }
        if (node.getNodeType() != Node.TEXT_NODE) {
            throw new CompilationException(__msgs.errUnexpectedNodeTypeForXPath(DOMUtils.domToString(node)));
        }
        xpathStr = node.getNodeValue();
        xpathStr = xpathStr.trim();
        if (xpathStr.length() == 0) {
            throw new CompilationException(__msgs.warnXPath20Syntax(DOMUtils.domToString(node), "empty string"));
        }

        out.xpath = xpathStr;
        try {        	
            __log.debug("Compiling expression " + xpathStr);            
            System.setProperty(
            		"javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON,
            		"net.sf.saxon.xpath.XPathFactoryImpl");
            XPathFactory xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
            JaxpFunctionResolver funcResolver = new JaxpFunctionResolver(
                    _compilerContext, out, source.getNamespaceContext(), _bpelNS);
            JaxpVariableResolver varResolver = new JaxpVariableResolver(_compilerContext, out);
            XPath xpe = xpf.newXPath();
            xpe.setXPathFunctionResolver(funcResolver);
            xpe.setXPathVariableResolver(varResolver);
            xpe.setNamespaceContext(source.getNamespaceContext());
            XPathExpression expr = xpe.compile(xpathStr);
            // evaluate the expression so as to initialize the variables
            try { 
            	expr.evaluate(node); 
            } catch (XPathExpressionException xpee) { 
            	// swallow errors caused by uninitialized variable 
            }
            for (String varExpr : extractVariableExprs(xpathStr)) {
                expr = xpe.compile(varExpr);
            	try {
            		expr.evaluate(node);
            	} catch (XPathExpressionException xpee) {
                	// swallow errors caused by uninitialized variable 
            	}
            }
        } catch (XPathFactoryConfigurationException xpfce) {
            __log.debug(xpfce);
            __log.info("Couldn't validate properly expression " + xpathStr);
        } catch (XPathExpressionException e) {
            __log.debug(e);
            __log.info("Couldn't validate properly expression " + xpathStr);
        } catch (WrappedResolverException wre) {
            if (wre._compilationMsg != null)
                throw new CompilationException(wre._compilationMsg, wre);
            if (wre.getCause() instanceof CompilationException)
                throw (CompilationException) wre.getCause();
            throw wre;
        }
    }

    public Map<String, String> getProperties() {
        return _properties;
    }

    /**
     * Returns the list of variable references in the given XPath expression
     * that may not have been resolved properly, which is the case especially 
     * if the expression contains a function, which short circuited the evaluation.
     *  
     * @param xpathStr
     * @return list of variable expressions that may not have been resolved properly
     */
    private List<String> extractVariableExprs(String xpathStr) {    	
		ArrayList<String> variableExprs = new ArrayList<String>();
		int firstVariable = xpathStr.indexOf("$"), 
			lastVariable = xpathStr.lastIndexOf("$"),
			firstFunction = xpathStr.indexOf("("); 
		if ((firstVariable > 0 && // the xpath references a variable
				firstFunction > 0) || // the xpath contains a function
			(firstVariable < lastVariable)) { // the xpath references multiple variables  
			// most likely, the variable reference has not been resolved, so make that happen
			StringBuffer variableExpr = new StringBuffer();
			boolean quoted = false, doubleQuoted = false, variable = false;
			Name11Checker nameChecker = Name11Checker.getInstance();
			for (int index = 0; index < xpathStr.length(); index++) {
				char ch = xpathStr.charAt(index);
				if (ch == '\''){
					quoted = !quoted;
				}
				if (ch == '\"') {
					doubleQuoted = !doubleQuoted;
				}
				if (quoted || doubleQuoted){
					continue;
				}
				if (ch == '$') {
					variable = true;
					variableExpr.setLength(0);
					variableExpr.append(ch);
				} else {
					if (variable) {
						variableExpr.append(ch);
						// in the name is qualified, don't check if its a qname when we're at the ":" character
						if (ch == ':') {
							continue;
						}
						if (index == xpathStr.length() || 
								!nameChecker.isQName(variableExpr.substring(1))) {
							variable = false;
							variableExpr.setLength(variableExpr.length() - 1);
							variableExprs.add(variableExpr.toString());
						}
					}
				}
			}
		}
		return variableExprs;
	}

}
