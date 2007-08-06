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

public class DataHandling20Test extends BPELTestAbstract {

    public void testXPathNamespace1() throws Throwable {
        go("/bpel/2.0/TestXPathNamespace1");
    }
    public void testXPathNamespace2() throws Throwable {
        go("/bpel/2.0/TestXPathNamespace2");
    }
    public void testSubTreeAssign() throws Throwable {
        go("/bpel/2.0/TestSubTreeAssign");
    }
    public void testAssignActivity1() throws Throwable {
        go("/bpel/2.0/TestAssignActivity1");
    }
    public void testAssignActivity2() throws Throwable {
        go("/bpel/2.0/TestAssignActivity2");
    }
    public void testAssignComplex() throws Throwable {
        go("/bpel/2.0/TestAssignComplex");
    }
    public void testSimpleTypeParts() throws Throwable {
        go("/bpel/2.0/TestSimpleTypeParts");
    }
    public void testSimpleVariableType() throws Throwable {
        go("/bpel/2.0/TestSimpleVariableType");
    }
    public void testXslTransform() throws Throwable {
        go("/bpel/2.0/TestXslTransform");
    }
	
}
