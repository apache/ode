package org.apache.ode.test;

public class BasicActivities20Tests extends BPELTest {
	public void testHelloWorld2() throws Exception {
		go("target/test-classes/bpel/2.0/HelloWorld2");
	}

	public void testNegativeTargetNS1() throws Exception {
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
	
	public void testTimer() throws Exception {
	 go("target/test-classes/bpel/2.0/TestTimer");
	}


}
