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

import org.junit.Test;

public class StructuredActivities20Test extends BPELTestAbstract {
	@Test public void testFlowActivity1() throws Throwable {
        // Test Flow with XPath20
        go("/bpel/2.0/TestFlowActivity1");
    }
	@Test public void testFlowActivity2() throws Throwable {
        // Test Flow with XPath10
        go("/bpel/2.0/TestFlowActivity2");
    }
	@Test public void testFlowLinks() throws Throwable {
        // Test Flow with XPath10
        go("/bpel/2.0/TestFlowLinks");
    }
	@Test public void testForEach() throws Throwable {
        // Test Flow with XPath10
        go("/bpel/2.0/TestForEach");
    }
	@Test public void testPickOneWay() throws Throwable {
        // Test Flow with XPath10
        go("/bpel/2.0/TestPickOneWay");
    }
}
