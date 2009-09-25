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
package org.apache.ode.test;

import org.junit.Ignore;
import org.junit.Test;

public class DataHandling20Test extends BPELTestAbstract {

    @Test public void testComposeUrl() throws Throwable {
        go("/bpel/2.0/TestComposeUrl");
    }
    @Test public void testCombineUrl() throws Throwable {
        go("/bpel/2.0/TestCombineUrl");
    }
    @Test public void testExpandTemplate() throws Throwable {
        go("/bpel/2.0/TestExpandTemplate");
    }
    @Test public void testXPathNamespace1() throws Throwable {
        go("/bpel/2.0/TestXPathNamespace1");
    }
    @Test public void testXPathNamespace2() throws Throwable {
        go("/bpel/2.0/TestXPathNamespace2");
    }
    @Test public void testSubTreeAssign() throws Throwable {
        go("/bpel/2.0/TestSubTreeAssign");
    }
    @Test public void testAssignActivity1() throws Throwable {
        go("/bpel/2.0/TestAssignActivity1");
    }
    @Test public void testAssignActivity2() throws Throwable {
        go("/bpel/2.0/TestAssignActivity2");
    }
    @Test public void testAssignActivity3() throws Throwable {
        go("/bpel/2.0/TestAssignActivity3");
    }
    @Test public void testAssignComplex() throws Throwable {
        go("/bpel/2.0/TestAssignComplex");
    }
    @Test public void testSimpleTypeParts() throws Throwable {
        go("/bpel/2.0/TestSimpleTypeParts");
    }
    @Test public void testSimpleVariableType() throws Throwable {
        go("/bpel/2.0/TestSimpleVariableType");
    }
    @Test public void testXslTransform() throws Throwable {
        go("/bpel/2.0/TestXslTransform");
    }
    @Test public void testSplit() throws Throwable {
        go("/bpel/2.0/TestSplit");
    }
    @Test public void testCounter() throws Throwable {
        go("/bpel/2.0/TestCounter");
    }

    @Test
    public void testAssignDate() throws Throwable {
        go("/bpel/2.0/TestAssignDate");
    }
    
    @Test
    public void testAssignDate2() throws Throwable {
        go("/bpel/2.0/TestAssignDate2");
    }

    @Test
    public void testMsgDate() throws Throwable {
        go("/bpel/2.0/TestMsgDate");
    }
    @Test public void testDuration() throws Throwable {
        go("/bpel/2.0/TestDuration");
    }
    @Test public void testAssignMissingData() throws Throwable {
        go("/bpel/2.0/TestAssignMissingData");
    }
    @Test public void testXQueryExpression() throws Throwable {
        go("/bpel/2.0/TestXQueryExpression");
    }
    //madars.vitolins _at gmail.com  2009.04.11
    //Inline variable initialization test
    @Test public void testInlineVarInit() throws Throwable {
        go("/bpel/2.0/TestInlineVarInit");
    }
}
