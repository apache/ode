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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A series of BPEL 2.0  compilation test.
 */
public class GoodCompileTest extends TestCase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign5-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign6-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign7-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign8-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign9-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/AsyncProcess/AsyncProcess2.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/compensation/comp1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/compensation/comp2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow4-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow5-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow6-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow7-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/foreach/ForEach1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/foreach/ForEach2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/foreach/ForEach3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/if/If1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/if/If2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/if/If3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick4-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick5-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick6-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/rethrow/Rethrow1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/rethrow/Rethrow2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw4-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw5-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw6-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw7-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/wait/Wait1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/wait/Wait2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/while/While1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData2-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData3-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData4-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableProperty1-2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData2-xp2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData3-xp2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData4-xp2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableProperty1-xp2.0.bpel"));
        suite.addTest(new GoodCompileTCase("/2.0/good/xsd-import/helloworld-Server.bpel"));
        
        return suite;
    }

}
