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

public class BasicActivities20Test extends BPELTestAbstract {
	public void testHelloWorld2() throws Throwable {
		go("target/test-classes/bpel/2.0/HelloWorld2");
	}

	public void testNegativeTargetNS1() throws Throwable {
		/**
		 * Test for an invalid targetNamespace has been entered into the WSDL.
		 * See JIRA ODE-67
		 * Test for a specific exception message.
		 */
		go("target/test-classes/bpel/2.0/NegativeTargetNSTest1");
	}
	
	public void testTimer() throws Throwable {
	 go("target/test-classes/bpel/2.0/TestTimer");
	}

    public void testIf() throws Throwable {
        go("target/test-classes/bpel/2.0/TestIf");
    }

}
