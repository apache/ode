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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.bom.AssignActivity;
import org.apache.ode.bpel.compiler.bom.Bpel20QNames;
import org.apache.ode.bpel.compiler.bom.CompensateActivity;
import org.apache.ode.bpel.compiler.bom.CompensateScopeActivity;
import org.apache.ode.bpel.compiler.bom.EmptyActivity;
import org.apache.ode.bpel.compiler.bom.FlowActivity;
import org.apache.ode.bpel.compiler.bom.ForEachActivity;
import org.apache.ode.bpel.compiler.bom.IfActivity;
import org.apache.ode.bpel.compiler.bom.InvokeActivity;
import org.apache.ode.bpel.compiler.bom.PickActivity;
import org.apache.ode.bpel.compiler.bom.ReceiveActivity;
import org.apache.ode.bpel.compiler.bom.RepeatUntilActivity;
import org.apache.ode.bpel.compiler.bom.ReplyActivity;
import org.apache.ode.bpel.compiler.bom.RethrowActivity;
import org.apache.ode.bpel.compiler.bom.SequenceActivity;
import org.apache.ode.bpel.compiler.bom.TerminateActivity;
import org.apache.ode.bpel.compiler.bom.ThrowActivity;
import org.apache.ode.bpel.compiler.bom.WaitActivity;
import org.apache.ode.bpel.compiler.bom.WhileActivity;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL20;
import org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL20;

/**
 * OASIS BPEL V2.0 Compiler
 */
public class BpelCompiler20 extends BpelCompiler {

    public static final String OASIS_EXPLANG_XPATH_1_0 = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0";
    public static final String OASIS_EXPLANG_XPATH_2_0 = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath2.0";
    public static final String OASIS_EXPLANG_XQUERY_1_0 = "urn:oasis:names:tc:wsbpel:2.0:sublang:xquery1.0";

    public BpelCompiler20() throws Exception {
        super((WSDLFactory4BPEL) WSDLFactoryBPEL20.newInstance());

        registerActivityCompiler(EmptyActivity.class, new EmptyGenerator());
        registerActivityCompiler(CompensateScopeActivity.class, new CompensateScopeGenerator());
        registerActivityCompiler(CompensateActivity.class, new CompensateGenerator());
        registerActivityCompiler(FlowActivity.class, new FlowGenerator());
        registerActivityCompiler(SequenceActivity.class, new SequenceGenerator());
        registerActivityCompiler(AssignActivity.class, new AssignGenerator());
        registerActivityCompiler(ThrowActivity.class, new ThrowGenerator());
        registerActivityCompiler(WhileActivity.class, new WhileGenerator());
        registerActivityCompiler(RepeatUntilActivity.class, new RepeatUntilGenerator());
        registerActivityCompiler(IfActivity.class, new IfGenerator());
        registerActivityCompiler(PickActivity.class, new PickGenerator());
        registerActivityCompiler(ReplyActivity.class, new ReplyGenerator());
        registerActivityCompiler(ReceiveActivity.class, new ReceiveGenerator());
        registerActivityCompiler(InvokeActivity.class, new InvokeGenerator());
        registerActivityCompiler(WaitActivity.class, new WaitGenerator());
        registerActivityCompiler(TerminateActivity.class, new TerminateGenerator());
        registerActivityCompiler(RethrowActivity.class, new RethrowGenerator());
        registerActivityCompiler(ForEachActivity.class, new ForEachGenerator());

        registerExpressionLanguage(OASIS_EXPLANG_XPATH_1_0, new XPath10ExpressionCompilerBPEL20());
        
        try {
            registerExpressionLanguage(OASIS_EXPLANG_XPATH_2_0,
                    "org.apache.ode.bpel.elang.xpath20.compiler.XPath20ExpressionCompilerBPEL20");
        } catch (Exception e) {
            __log.error("Error loading XPath 2.0 Expression Language: it will not be available.");
        }
        
        try {
            registerExpressionLanguage(OASIS_EXPLANG_XQUERY_1_0,
                    "org.apache.ode.bpel.elang.xquery10.compiler.XQuery10ExpressionCompilerBPEL20");
        } catch (Exception e) {
            __log.error("Error loading XQuery 1.0 Expression Language: it will not be available.");
        }
    }

    protected String getBpwsNamespace() {
        return Bpel20QNames.NS_WSBPEL2_0_FINAL_EXEC;
    }

    protected String getDefaultExpressionLanguage() {
        return OASIS_EXPLANG_XPATH_1_0;
    }
}
