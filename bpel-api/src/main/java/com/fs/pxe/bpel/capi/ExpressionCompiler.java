/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.capi;

import com.fs.pxe.bpel.o.OExpression;
import com.fs.pxe.bpel.o.OLValueExpression;

import java.util.Map;

/**
 * Interface implemented by BPEL expression language compilers.
 */
public interface ExpressionCompiler {

  /**
   * Set the compiler context (for resolving variables and such).
   * @param compilerContext compiler context
   */
  void setCompilerContext(CompilerContext compilerContext);

  /**
   * Compile an expression into a {@link com.fs.pxe.bpel.o.OExpression} object.
   * @param source
   * @return
   */
  OExpression compile(Object source)
          throws CompilationException;
  
  /**
   * Compile an lvalue (the 'to' of an assignment) into a {@link com.fs.pxe.bpel.o.OLValueExpression} object.
   * @param source
   * @return
   * @throws CompilationException
   */
  OLValueExpression compileLValue(Object source)
  			 throws CompilationException;
  
  /**
   * Compile a join condition into a {@link com.fs.pxe.bpel.o.OExpression} object.
   * @param source
   * @return
   */
  OExpression compileJoinCondition(Object source)
          throws CompilationException;

  Map<String,String> getProperties();

}
