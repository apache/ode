package org.apache.ode.test;

public class BasicActivities20Test extends BPELTest {
	public void testHelloWorld2() throws Throwable {
		go("target/test-classes/bpel/2.0/HelloWorld2");
	}

	public void testNegativeTargetNS1() throws Throwable {
		/**
		 * Test for an invalid targetNamespace has been entered into the WSDL.
		 * 
		 * See JIRA ODE-67
		 * 
		 * Test for a specific exception message.
		 * 
		 */

		go("target/test-classes/bpel/2.0/NegativeTargetNSTest1");
	}
	
	public void testTimer() throws Throwable {
	 go("target/test-classes/bpel/2.0/TestTimer");
	}


}
