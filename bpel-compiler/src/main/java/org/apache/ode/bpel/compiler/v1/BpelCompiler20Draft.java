/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.ode.bpel.compiler.v1;

import org.apache.ode.bpel.compiler.bom.*;
import org.apache.ode.bpel.compiler.v1.xpath10.XPath10ExpressionCompilerBPEL20Draft;
import org.apache.ode.bpel.compiler.v1.xpath20.XPath20ExpressionCompilerBPEL20Draft;
import org.apache.ode.bpel.compiler.v1.xpath10.jaxp.JaxpXPath10ExpressionCompilerBPEL20Draft;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL20Draft;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class BpelCompiler20Draft extends BpelCompilerImpl {

    public static final String OASIS_EXPLANG_XPATH_1_0 = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0";

    public static final String OASIS_EXPLANG_XPATH_2_0 = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath2.0";

    public static final String OASIS_EXPLANG_XQUERY_1_0 = "urn:oasis:names:tc:wsbpel:2.0:sublang:xquery1.0";

    public BpelCompiler20Draft() throws Exception {
        super((WSDLFactory4BPEL) WSDLFactoryBPEL20Draft.newInstance());

        registerActivityCompiler(EmptyActivity.class, new EmptyGenerator());
        registerActivityCompiler(CompensateScopeActivity.class, new CompensateGenerator());
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

        // try to instantiate Jaxen based XPath 1.0 expression language compiler.
        // if this fails (e.g. because Jaxen is not in classpath), use a pure JAXP based one as fallback.
        try {
            registerExpressionLanguage(OASIS_EXPLANG_XPATH_1_0, new XPath10ExpressionCompilerBPEL20Draft());
        } catch (Exception e) {
            __log
                .warn("Error loading Jaxen based XPath 1.0 Expression Language, falling back to Jaxp based implementation.");
            registerExpressionLanguage(OASIS_EXPLANG_XPATH_1_0, new JaxpXPath10ExpressionCompilerBPEL20Draft());
        } catch (NoClassDefFoundError e) {
            __log
                .warn("Error loading Jaxen based XPath 1.0 Expression Language, falling back to Jaxp based implementation.");
            registerExpressionLanguage(OASIS_EXPLANG_XPATH_1_0, new JaxpXPath10ExpressionCompilerBPEL20Draft());
        }
        try {
            registerExpressionLanguage(OASIS_EXPLANG_XPATH_2_0, new XPath20ExpressionCompilerBPEL20Draft());
        } catch (Exception e) {
            __log.error("Error loading XPath 2.0 Expression Language: it will not be available.");
        } catch (NoClassDefFoundError e) {
            __log.error("Error loading XPath 2.0 Expression Language: it will not be available.");
        }

        try {
            registerExpressionLanguage(OASIS_EXPLANG_XQUERY_1_0,
                "org.apache.ode.bpel.comipler.v2.xquery10.compiler.XQuery10ExpressionCompilerBPEL20Draft");
        } catch (Exception e) {
            __log.error("Error loading XQuery 1.0 Expression Language: it will not be available.");
        } catch (NoClassDefFoundError e) {
            __log.error("Error loading XQuery 1.0 Expression Language: it will not be available.");
        }
    }

    protected String getBpwsNamespace() {
        return Bpel20QNames.NS_WSBPEL2_0;
    }

    protected String getDefaultExpressionLanguage() {
        return OASIS_EXPLANG_XPATH_1_0;
    }

}
