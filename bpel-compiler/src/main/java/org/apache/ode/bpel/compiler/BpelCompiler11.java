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
import org.apache.ode.bpel.compiler.bom.Bpel11QNames;
import org.apache.ode.bpel.compiler.bom.CompensateScopeActivity;
import org.apache.ode.bpel.compiler.bom.EmptyActivity;
import org.apache.ode.bpel.compiler.bom.FlowActivity;
import org.apache.ode.bpel.compiler.bom.InvokeActivity;
import org.apache.ode.bpel.compiler.bom.PickActivity;
import org.apache.ode.bpel.compiler.bom.ReceiveActivity;
import org.apache.ode.bpel.compiler.bom.ReplyActivity;
import org.apache.ode.bpel.compiler.bom.SequenceActivity;
import org.apache.ode.bpel.compiler.bom.SwitchActivity;
import org.apache.ode.bpel.compiler.bom.TerminateActivity;
import org.apache.ode.bpel.compiler.bom.ThrowActivity;
import org.apache.ode.bpel.compiler.bom.WaitActivity;
import org.apache.ode.bpel.compiler.bom.WhileActivity;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL11;

/**
 * BPEL v1.1 compiler.
 */
public class BpelCompiler11 extends BpelCompiler {

    /** URI for the XPath 1.0 expression language. */
    public static final String EXPLANG_XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public BpelCompiler11() throws Exception {
        super((WSDLFactory4BPEL) WSDLFactoryBPEL11.newInstance());

        registerActivityCompiler(EmptyActivity.class, new EmptyGenerator());
        registerActivityCompiler(CompensateScopeActivity.class, new CompensateGenerator());
        registerActivityCompiler(FlowActivity.class, new FlowGenerator());
        registerActivityCompiler(SequenceActivity.class, new SequenceGenerator());
        registerActivityCompiler(AssignActivity.class, new AssignGenerator());
        registerActivityCompiler(ThrowActivity.class, new ThrowGenerator());
        registerActivityCompiler(WhileActivity.class, new WhileGenerator());
        registerActivityCompiler(SwitchActivity.class, new SwitchGenerator());
        registerActivityCompiler(PickActivity.class, new PickGenerator());
        registerActivityCompiler(ReplyActivity.class, new ReplyGenerator());
        registerActivityCompiler(ReceiveActivity.class, new ReceiveGenerator());
        registerActivityCompiler(InvokeActivity.class, new InvokeGenerator());
        registerActivityCompiler(WaitActivity.class, new WaitGenerator());
        registerActivityCompiler(TerminateActivity.class, new TerminateGenerator());

        registerExpressionLanguage(EXPLANG_XPATH, "org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL11");
    }


    protected String getBpwsNamespace() {
        return Bpel11QNames.NS_BPEL4WS_2003_03;
    }

    protected String getDefaultExpressionLanguage() {
        return EXPLANG_XPATH;
    }

}
