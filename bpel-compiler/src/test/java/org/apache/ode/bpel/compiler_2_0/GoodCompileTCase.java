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
package org.apache.ode.bpel.compiler_2_0;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.BpelC;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompileListener;
import org.apache.ode.utils.StreamUtils;


/**
 * JUnit {@link TestCase} of the ODE BPEL compiler. This test case provides
 * is not very complex, it simply ensures that the given BPEL input compiles
 * succesfully. These test cases are intended to be run as part of a suite. 
 */
class GoodCompileTCase extends TestCase implements CompileListener {

    protected final Log __log = LogFactory.getLog(getClass());
    private BpelC _compiler;
    private ArrayList<CompilationMessage> _errors = new ArrayList<CompilationMessage>();
    private String _bpel;


    GoodCompileTCase(String bpel) {
        super(bpel);
        _bpel = bpel;
    }

    protected void setUp() throws Exception {
        super.setUp();
        _compiler = BpelC.newBpelCompiler();
        _compiler.setCompileListener(this);
        _compiler.setOutputStream(new ByteArrayOutputStream(StreamUtils.DEFAULT_BUFFER_SIZE));
        _errors.clear();
    }

    public void runTest() throws Exception {
        try {
        	File bpelFile = new File(getClass().getResource(_bpel).toURI().getPath());
        	_compiler.compile(bpelFile, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Compilation did not succeed.");
        }
    }

    public void onCompilationMessage(CompilationMessage compilationMessage) {
        _errors.add(compilationMessage);
        __log.debug(compilationMessage.toString());
    }

}
