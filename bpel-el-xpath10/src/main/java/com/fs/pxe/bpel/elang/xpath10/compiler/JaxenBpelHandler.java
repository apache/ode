/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath10.compiler;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.impl.nodes.ExpressionImpl;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.capi.CompilerContext;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10Expression;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import com.fs.pxe.bpel.o.*;
import com.fs.pxe.bpel.xsl.XslTransformHandler;
import com.fs.utils.NSContext;
import com.fs.utils.msg.MessageBundle;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;

import javax.xml.namespace.QName;
import java.util.List;


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
  public void endFunction()
                   throws JaxenException {
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
      try {
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
      } catch (CompilationException ce) {
        throw new CompilationExceptionWrapper(ce);
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
      // Create a virtual expression node.
      Expression vExpSrc = new ExpressionImpl(null);
      vExpSrc.setNamespaceContext(_nsContext);
      vExpSrc.setXPathString(locationstr);
      location = _cctx.compileExpr(vExpSrc);
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
      XslTransformHandler.getInstance().parseXSLSheet(xslSheet.uri, xslSheet.sheetBody,
                      new XslCompileUriResolver(_cctx, _out));
    } catch (Exception e) {
      throw new CompilationException(
          __msgs.errInvalidNumberOfArguments(xslUri));
    }

    _out.xslSheets.put(xslSheet.uri, xslSheet);
  }

  private String getLiteralFromExpression(Expr expr)
      throws CompilationException {
    expr = expr.simplify();

    if (expr instanceof LiteralExpr)
      return ((LiteralExpr)expr).getLiteral();

    throw new CompilationException(__msgs.errLiteralExpected(expr.getText()));
  }

}
