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
package org.apache.ode.bpel.compiler.v1.xpath10.jaxp;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.rtrep.v1.xpath10.OXPath10Expression;
import org.apache.ode.bpel.rtrep.v1.OExpression;
import org.apache.ode.bpel.rtrep.v1.OLValueExpression;
import org.apache.ode.utils.Namespaces;

/**
 * JAXP based XPath 1.0 expression compiler for BPEL v1.1.
 */
public class JaxpXPath10ExpressionCompilerBPEL11 extends JaxpXPath10ExpressionCompilerImpl {
  public JaxpXPath10ExpressionCompilerBPEL11() {
    super(Namespaces.BPEL11_NS);
  }
  
  /**
   * @see org.apache.ode.bpel.compiler.v1.ExpressionCompiler#compileJoinCondition(java.lang.Object)
   */
  public OExpression compileJoinCondition(Object source) throws CompilationException {
  	return compile(source);
  }
  
  public OLValueExpression compileLValue(Object source) throws CompilationException {
  	throw new UnsupportedOperationException("Not supported for bpel 1.1");
  }

  /**
   * @see org.apache.ode.bpel.compiler.v1.ExpressionCompiler#compile(java.lang.Object)
   */
  public OExpression compile(Object source) throws CompilationException {
    Expression xpath = (Expression)source;
    OXPath10Expression oexp = new OXPath10Expression(
            _compilerContext.getOProcess(),
            _qnFnGetVariableData,
            _qnFnGetVariableProperty,
            _qnFnGetLinkStatus);
    oexp.namespaceCtx = xpath.getNamespaceContext();
    doJaxpCompile(oexp, xpath);
    return oexp;
  }
}
