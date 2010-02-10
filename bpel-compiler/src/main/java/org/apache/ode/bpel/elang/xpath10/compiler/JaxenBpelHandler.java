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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;


/**
 * Verifies validity of bpel extensions for xpath expressions
 */
class JaxenBpelHandler extends JaxenHandler {
  private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

  private CompilerContext _cctx;
  private OXPath10Expression _out;
  private NSContext _nsContext;
  private String _bpelNsUril;

  JaxenBpelHandler(String bpelNsUri, OXPath10Expression out, NSContext nsContext, CompilerContext cctx) {
    _bpelNsUril = bpelNsUri;
    _cctx = cctx;
    _nsContext = nsContext;
    _out = out;

    assert nsContext != null;
    assert cctx != null;
    assert out != null;
  }

	public void variableReference(String prefix, String variableName)
			throws JaxenException {
		super.variableReference(prefix, variableName);

    // Custom variables
    if ("ode".equals(prefix)) {
      if ("pid".equals(variableName)) return;
    }

    if(_out instanceof OXPath10ExpressionBPEL20){
			OXPath10ExpressionBPEL20 out = (OXPath10ExpressionBPEL20)_out;
			try{
				if(out.isJoinExpression){
					// these resolve to links
					OLink olink = _cctx.resolveLink(variableName);
					_out.links.put(variableName, olink);
				}else{
					int dot = variableName.indexOf('.');
					if (dot != -1)
						variableName = variableName.substring(0,dot);
					OScope.Variable var = _cctx.resolveVariable(variableName);
					_out.vars.put(variableName, var);
				}
			}catch(CompilationException ce){
				throw new CompilationExceptionWrapper(ce);
			}
		}
	}
  
  public void endXPath() throws JaxenException {
    super.endXPath();
  }

  /**
   */
  public void endFunction() {
    super.endFunction();

    FunctionCallExpr c = (FunctionCallExpr)peekFrame()
                                             .getLast();

    String prefix = c.getPrefix();

    // empty string prefix should resolve to xpath namespace, NOT bpel
    if ((prefix == null) || "".equals(prefix)) {
      return;
    }

    String ns = _nsContext.getNamespaceURI(prefix);

    if (ns == null) {
      throw new CompilationException(
          __msgs.errUndeclaredFunctionPrefix(prefix,c.getFunctionName()));
    } else if (isBpelNamespace(ns)) {
        if (Constants.EXT_FUNCTION_GETVARIABLEDATA.equals(c.getFunctionName())) {
            compileGetVariableData(c);
        } else if (Constants.EXT_FUNCTION_GETVARIABLEPROPRTY.equals(c
                .getFunctionName())) {
            compileGetVariableProperty(c);
        } else if (Constants.EXT_FUNCTION_GETLINKSTATUS.equals(c.getFunctionName())) {
            compileGetLinkStatus(c);
        } else if (Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(c.getFunctionName())) {
            compileDoXslTransform(c);
        } else {
            throw new CompilationException(__msgs.errUnknownBpelFunction(c.getFunctionName()));
        }
    }
  }

  private boolean isBpelNamespace(String ns) {
    return ns.equals(_bpelNsUril);
  }

  private void compileGetLinkStatus(FunctionCallExpr c)
                           throws CompilationException {
    List params = c.getParameters();

    if (params.size() != 1) {
      throw  new CompilationException(__msgs.errInvalidNumberOfArguments(c.getFunctionName()));
    }

    String linkName = getLiteralFromExpression((Expr)params.get(0));

    OLink olink = _cctx.resolveLink(linkName);
    _out.links.put(linkName, olink);
  }

  /**
   * Compile a <code>bpws:getVariableData(...)</em> function call. Note that all arguments
   * to this call <em>must</em> be literal values. Therefore, we are able to "pre-compile"
   * all possible invocations of this call, and save ourselves the problem of compiling
   * query expressions at runtime.
   * @param c {@link FunctionCallExpr} for this invocation
   * @throws CompilationException
   */
  private void compileGetVariableData(FunctionCallExpr c)
                                     throws CompilationException {
    List params = c.getParameters();

    if (params.size() < 1 || params.size() > 3) {
      throw new CompilationException(
                                __msgs.errInvalidNumberOfArguments(c.getFunctionName()));

    }
    String varname = getLiteralFromExpression((Expr)params.get(0));
    String partname = params.size() > 1 ? getLiteralFromExpression((Expr)params.get(1)) : null;
    String locationstr = params.size() > 2 ? getLiteralFromExpression((Expr)params.get(2)) : null;

    OScope.Variable var = _cctx.resolveVariable(varname);
    OMessageVarType.Part part = partname != null ? _cctx.resolvePart(var,partname) : null;
    OExpression location = null;
    if (locationstr != null) {
      location = _cctx.compileExpr(locationstr,_nsContext);
    }

    _out.addGetVariableDataSig(varname, partname, locationstr,
            new OXPath10Expression.OSigGetVariableData(_cctx.getOProcess(),var, part,location));

  }

  private void compileGetVariableProperty(FunctionCallExpr c)
                                 throws CompilationException{
    List params = c.getParameters();

    if (params.size() != 2) {
      throw new CompilationException(
                          __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETVARIABLEPROPRTY));
    }

    String varName = getLiteralFromExpression((Expr)params.get(0));
    OScope.Variable v = _cctx.resolveVariable(varName);
    _out.vars.put(varName, v);

    String propName = getLiteralFromExpression((Expr)params.get(1));
    QName qname = _nsContext.derefQName(propName);

    if (qname == null)
      throw new CompilationException(
                                __msgs.errInvalidQName(propName));

    OProcess.OProperty property = _cctx.resolveProperty(qname);
    // Make sure we can...
    _cctx.resolvePropertyAlias(v, qname);

    _out.properties.put(propName, property);
    _out.vars.put(varName, v);
  }

  private void compileDoXslTransform(FunctionCallExpr c) throws CompilationException {
    List params = c.getParameters();
    if (params.size() < 2 || params.size() % 2 != 0) {
      throw new CompilationException(
          __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_DOXSLTRANSFORM));
    }

    String xslUri = getLiteralFromExpression((Expr)params.get(0));
    OXslSheet xslSheet = _cctx.compileXslt(xslUri);
    try {
      XslTransformHandler.getInstance().parseXSLSheet(_cctx.getOProcess().getQName(), xslSheet.uri, xslSheet.sheetBody,
                      new XslCompileUriResolver(_cctx, _out));
    } catch (Exception e) {
      throw new CompilationException(
          __msgs.errInvalidNumberOfArguments(xslUri));
    }

    _out.setXslSheet(xslSheet.uri, xslSheet);
  }

  private String getLiteralFromExpression(Expr expr)
      throws CompilationException {
    expr = expr.simplify();

    if (expr instanceof LiteralExpr)
      return ((LiteralExpr)expr).getLiteral();

    throw new CompilationException(__msgs.errLiteralExpected(expr.getText()));
  }

}
