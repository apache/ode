/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.compiler;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;

/**
 * XPath 1.0 expression compiler for BPEL v1.1.
 */
public class XPath10ExpressionCompilerBPEL11 extends XPath10ExpressionCompilerImpl {
  public XPath10ExpressionCompilerBPEL11() {
    super(Constants.BPEL11_NS);
  }
  
  /**
   * @see org.apache.ode.bpel.capi.ExpressionCompiler#compileJoinCondition(java.lang.Object)
   */
  public OExpression compileJoinCondition(Object source) throws CompilationException {
  	return compile(source);
  }
  
  public OLValueExpression compileLValue(Object source) throws CompilationException {
  	throw new UnsupportedOperationException("Not supported for bpel 1.1");
  }

  /**
   * @see org.apache.ode.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
   */
  public OExpression compile(Object source) throws CompilationException {
    Expression xpath = (Expression)source;
    OXPath10Expression oexp = new OXPath10Expression(
            _compilerContext.getOProcess(),
            _qnFnGetVariableData,
            _qnFnGetVariableProperty,
            _qnFnGetLinkStatus);
    oexp.namespaceCtx = xpath.getNamespaceContext();
    doJaxenCompile(oexp, xpath);
    return oexp;
  }
}
