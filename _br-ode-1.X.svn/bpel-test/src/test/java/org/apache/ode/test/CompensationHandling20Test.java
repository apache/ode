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

import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.MessageExchange;
import org.junit.Ignore;
import org.junit.Test;

public class CompensationHandling20Test extends BPELTestAbstract {

	@Test
	public void testCompensationHandlers() throws Throwable {
		go("/bpel/2.0/TestCompensationHandlers");
	}

	@Ignore("fix test bed for handling ASYNC mex") @Test public void testImplicitFaultHandler() throws Throwable {
		/*
		 * Failure report: Invocation#Invoke#1: Exception on future object.; got
		 * exception msg: Message exchange
		 * org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl$ResponseFuture@ab6dcb
		 * timed out when waiting for a response!
		 * junit.framework.AssertionFailedError: Failure report:
		 * Invocation#Invoke#1: Exception on future object.; got exception msg:
		 * Message exchange
		 * org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl$ResponseFuture@ab6dcb
		 * timed out when waiting for a response!
		 * 
		 * at
		 * org.apache.ode.test.BPELTestAbstract.checkFailure(BPELTestAbstract.java:278)
		 * at org.apache.ode.test.BPELTestAbstract.go(BPELTestAbstract.java:267)
		 * at
		 * org.apache.ode.test.CompensationHandling20Test.testImplicitFaultHandler(CompensationHandling20Test.java:45)
		 */
		deploy("/bpel/2.0/TestImplicitFaultHandler");
		Invocation inv = addInvoke("Invoke#1", new QName("http://ode/bpel/unit-test/testImplicitFaultHandler.wsdl",
						"testImplicitFaultHandlerService"), "request", 
						"<message><requestID>Start TestImplicitFaultHandler</requestID><requestText>Event TestImplicitFaultHandler</requestText><faultIndicator1>yes</faultIndicator1><faultIndicator2>no</faultIndicator2></message>",
						null);
		inv.expectedFinalStatus = MessageExchange.Status.FAULT;
		inv.expectedResponsePattern = Pattern.compile(".*Event TestFaultWithVariable1 -&gt; caught FaultMessage1 -&gt; Event TestFaultWithVariable1 -&gt; process complete.*");

		go();
	}

}
