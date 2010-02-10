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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit {@link TestSuite} for running a series of {@link StaticCheckTCase}s.
 */
public class StaticCheckSuite extends TestCase {

    /**
     * @return a test suite of tests that show compilation failures.
     * @throws Exception
     */
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTest(new StaticCheckTCase("NoRootActivity"));
        suite.addTest(new StaticCheckTCase("PortTypeMismatch"));
        suite.addTest(new StaticCheckTCase("UndeclaredPropertyAlias"));
        suite.addTest(new StaticCheckTCase("UnknownBpelFunction"));
        suite.addTest(new StaticCheckTCase("UndeclaredVariable"));
        suite.addTest(new StaticCheckTCase("DuplicateLinkTarget"));
        suite.addTest(new StaticCheckTCase("DuplicateLinkSource"));
        suite.addTest(new StaticCheckTCase("DuplicateLinkDecl"));
        suite.addTest(new StaticCheckTCase("LinkMissingSourceActivity"));
        suite.addTest(new StaticCheckTCase("LinkMissingTargetActivity"));
        suite.addTest(new StaticCheckTCase("DuplicateVariableDecl"));
        // We simply can't test the next one without using the BOM; both the parser
        // and schema validation would rule it out.
        //suite.addTest(new StaticCheckTest("CompensateNAtoContext"));
        return suite;
    }
}
