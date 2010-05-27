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
package org.apache.ode.bpel.compiler.api;

import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.bpel.o.OVarType;

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
   * Compile an expression into a {@link org.apache.ode.bpel.o.OExpression} object.
   * @param source
   * @return
   */
  OExpression compile(Object source)
          throws CompilationException;

  /**
   * Compile an lvalue (the 'to' of an assignment) into a {@link org.apache.ode.bpel.o.OLValueExpression} object.
   * @param source
   * @return
   * @throws CompilationException
   */
  OLValueExpression compileLValue(Object source)
             throws CompilationException;

  /**
   * Compile a join condition into a {@link org.apache.ode.bpel.o.OExpression} object.
   * @param source
   * @return
   */
  OExpression compileJoinCondition(Object source)
          throws CompilationException;

  Map<String,String> getProperties();

}
