/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.compiler;

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;

import javax.xml.namespace.QName;

/**
 * XPath 1.0 expression compiler for BPEL 2.0
 */
public class XPath10ExpressionCompilerBPEL20 extends XPath10ExpressionCompilerImpl {

  protected QName _qnDoXslTransform;

  public XPath10ExpressionCompilerBPEL20() {
    super(Constants.BPEL20_NS);
    _qnDoXslTransform = new QName(Constants.BPEL20_NS, "doXslTransform");
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
    OXPath10Expression oexp = new OXPath10ExpressionBPEL20(
            _compilerContext.getOProcess(),
            _qnFnGetVariableData,
            _qnFnGetVariableProperty,
            _qnFnGetLinkStatus,
            _qnDoXslTransform,
            isJoinCondition);
    oexp.namespaceCtx = xpath.getNamespaceContext();
    doJaxenCompile(oexp, xpath);
    return oexp;
  }


}
