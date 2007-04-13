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

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10ExpressionBPEL20;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.xsl.XslTransformHandler;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;

/**
 * XPath 1.0 expression compiler for BPEL 2.0
 */
public class XPath10ExpressionCompilerBPEL20 extends XPath10ExpressionCompilerImpl {

    protected QName _qnDoXslTransform;

    public XPath10ExpressionCompilerBPEL20() {
        this(Namespaces.WSBPEL2_0_FINAL_EXEC);
    }

    public XPath10ExpressionCompilerBPEL20(String bpelNS) {
        super(bpelNS);
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);

        _qnDoXslTransform = new QName(bpelNS, "doXslTransform");
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compileJoinCondition(java.lang.Object)
     */
    public OExpression compileJoinCondition(Object source) throws CompilationException {
        return _compile((Expression)source, true);
    }

    @Override
    public void setCompilerContext(CompilerContext ctx) {
        super.setCompilerContext(ctx);
        XslCompilationErrorListener xe = new XslCompilationErrorListener(ctx);
        XslTransformHandler.getInstance().setErrorListener(xe);
    }
    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compile(java.lang.Object)
     */
    public OExpression compile(Object source) throws CompilationException {
        return _compile((Expression)source, false);
    }
    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compileLValue(java.lang.Object)
     */
    public OLValueExpression compileLValue(Object source) throws CompilationException {
        return (OLValueExpression)_compile((Expression)source, false);
    }

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
