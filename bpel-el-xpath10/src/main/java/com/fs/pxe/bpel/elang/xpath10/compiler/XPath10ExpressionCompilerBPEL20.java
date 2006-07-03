/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath10.compiler;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10Expression;
import com.fs.pxe.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import com.fs.pxe.bpel.o.OExpression;
import com.fs.pxe.bpel.o.OLValueExpression;

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
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compileJoinCondition(java.lang.Object)
   */
  public OExpression compileJoinCondition(Object source) throws CompilationException {
    return _compile((Expression)source, true);
  }

  /**
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
   */
  public OExpression compile(Object source) throws CompilationException {
    return _compile((Expression)source, false);
  }
  /**
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compileLValue(java.lang.Object)
   */
  public OLValueExpression compileLValue(Object source) throws CompilationException {
    return (OLValueExpression)_compile((Expression)source, false);
  }

  /**
   * @see com.fs.pxe.bpel.capi.ExpressionCompiler#compile(java.lang.Object)
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
